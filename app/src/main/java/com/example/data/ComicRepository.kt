package com.example.data

import com.example.api.PicaApiService
import com.example.api.PicaNetworkClient
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ComicRepository private constructor() {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // API Route endpoints
    private var baseUrl: String = "https://picaapi.picacomic.com"
    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    private val _currentRouteLine = MutableStateFlow("官方高速线路 1 (picaapi.picacomic.com)")
    val currentRouteLine: StateFlow<String> = _currentRouteLine.asStateFlow()

    private val _routeLatency = MutableStateFlow(28) // ms
    val routeLatency: StateFlow<Int> = _routeLatency.asStateFlow()

    private var networkClient = PicaNetworkClient(
        getBaseUrl = { baseUrl },
        getToken = { _authToken.value }
    )
    private var apiService: PicaApiService = networkClient.createService()

    // API UI States for Exploration
    private val _categoriesState = MutableStateFlow<UiResource<List<CategoryItem>>>(UiResource.Loading)
    val categoriesState: StateFlow<UiResource<List<CategoryItem>>> = _categoriesState.asStateFlow()

    private val _exploreComicsState = MutableStateFlow<UiResource<List<ComicItem>>>(UiResource.Loading)
    val exploreComicsState: StateFlow<UiResource<List<ComicItem>>> = _exploreComicsState.asStateFlow()

    private val _leaderboardState = MutableStateFlow<UiResource<List<ComicItem>>>(UiResource.Loading)
    val leaderboardState: StateFlow<UiResource<List<ComicItem>>> = _leaderboardState.asStateFlow()

    private val _categoryComicsState = MutableStateFlow<Map<String, UiResource<List<ComicItem>>>>(emptyMap())
    val categoryComicsState: StateFlow<Map<String, UiResource<List<ComicItem>>>> = _categoryComicsState.asStateFlow()

    // In-memory cache for all comics fetched from API
    private val comicCache = ConcurrentHashMap<String, ComicItem>()

    // Local user data
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _history = MutableStateFlow<List<ReadingRecord>>(emptyList())
    val history: StateFlow<List<ReadingRecord>> = _history.asStateFlow()

    init {
        // Automatically fetch categories and exploration comics from PicaComic API upon launch
        loadCategories()
        loadExploreComics()
        loadLeaderboard("H24")
    }

    fun setApiRoute(lineName: String, url: String, latency: Int) {
        baseUrl = url
        _currentRouteLine.value = lineName
        _routeLatency.value = latency
        networkClient = PicaNetworkClient(
            getBaseUrl = { baseUrl },
            getToken = { _authToken.value }
        )
        apiService = networkClient.createService()
        refreshAll()
    }

    fun setAuthToken(token: String?) {
        _authToken.value = token
        refreshAll()
    }

    fun refreshAll() {
        loadCategories()
        loadExploreComics()
        loadLeaderboard("H24")
    }

    /**
     * Automatically fetches categories from PicaComic API: GET /categories
     */
    fun loadCategories() {
        _categoriesState.value = UiResource.Loading
        repositoryScope.launch {
            try {
                val response = apiService.getCategories()
                val list = response.data?.categories
                    ?.filter { it.active }
                    ?.map { it.toCategoryItem() }
                    .orEmpty()

                if (list.isNotEmpty()) {
                    _categoriesState.value = UiResource.Success(list)
                } else {
                    _categoriesState.value = UiResource.Error(
                        message = if (response.message.isNotBlank()) response.message else "暂无分类数据或需要登录",
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                _categoriesState.value = UiResource.Error(
                    message = "连接哔咔API失败 (${e.localizedMessage ?: "网络超时"})，请检查网络或线路",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Automatically fetches explore comics from PicaComic API: GET /comics/random or GET /comics
     */
    fun loadExploreComics() {
        _exploreComicsState.value = UiResource.Loading
        repositoryScope.launch {
            try {
                // Try fetching random comics first
                val randomResp = apiService.getRandomComics()
                val randomDocs = randomResp.data?.comics.orEmpty()

                val docs = if (randomDocs.isNotEmpty()) {
                    randomDocs
                } else {
                    // Fallback to top comics page
                    val comicsResp = apiService.getComics(sort = "dd", page = 1)
                    comicsResp.data?.comics?.docs.orEmpty()
                }

                if (docs.isNotEmpty()) {
                    val items = docs.map { doc ->
                        val item = doc.toComicItem()
                        comicCache[item.id] = item
                        item
                    }
                    _exploreComicsState.value = UiResource.Success(items)
                } else {
                    _exploreComicsState.value = UiResource.Error(
                        message = "探索列表为空，可能需要登录哔咔账号或配置Token",
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                _exploreComicsState.value = UiResource.Error(
                    message = "获取探索漫画失败: ${e.localizedMessage ?: "连接超时"}",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Fetches leaderboard comics: GET /comics/leaderboard
     */
    fun loadLeaderboard(timeType: String = "H24") {
        _leaderboardState.value = UiResource.Loading
        repositoryScope.launch {
            try {
                val resp = apiService.getLeaderboard(timeType = timeType, contentType = "VC")
                val docs = resp.data?.comics.orEmpty()
                if (docs.isNotEmpty()) {
                    val items = docs.map { doc ->
                        val item = doc.toComicItem()
                        comicCache[item.id] = item
                        item
                    }
                    _leaderboardState.value = UiResource.Success(items)
                } else {
                    _leaderboardState.value = UiResource.Error(
                        message = "排行榜暂无数据，请确认网络连接与登录状态",
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                _leaderboardState.value = UiResource.Error(
                    message = "获取排行榜失败: ${e.localizedMessage ?: "网络异常"}",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Automatically fetches comics for a specific category: GET /comics?c={category}
     */
    fun loadCategoryComics(categoryName: String, page: Int = 1) {
        _categoryComicsState.update { current ->
            current + (categoryName to UiResource.Loading)
        }
        repositoryScope.launch {
            try {
                val resp = apiService.getComics(category = categoryName, sort = "dd", page = page)
                val docs = resp.data?.comics?.docs.orEmpty()
                val items = docs.map { doc ->
                    val item = doc.toComicItem()
                    comicCache[item.id] = item
                    item
                }
                _categoryComicsState.update { current ->
                    current + (categoryName to UiResource.Success(items))
                }
            } catch (e: Exception) {
                _categoryComicsState.update { current ->
                    current + (categoryName to UiResource.Error("加载分类 $categoryName 失败: ${e.localizedMessage}"))
                }
            }
        }
    }

    fun getComicById(id: String): ComicItem? {
        return comicCache[id]
    }

    fun getAllCachedComics(): List<ComicItem> {
        return comicCache.values.toList()
    }

    /**
     * Fetches chapters from API: GET /comics/{id}/eps
     */
    suspend fun getChaptersForComic(comicId: String): List<Chapter> {
        return try {
            val resp = apiService.getComicEpisodes(comicId, page = 1)
            val docs = resp.data?.eps?.docs.orEmpty()
            if (docs.isNotEmpty()) {
                docs.map { it.toChapter(comicId) }
            } else {
                listOf(Chapter(id = "${comicId}_ep_1", comicId = comicId, order = 1, title = "第 01 话", pageCount = 24, releaseDate = "近期"))
            }
        } catch (e: Exception) {
            listOf(Chapter(id = "${comicId}_ep_1", comicId = comicId, order = 1, title = "第 01 话 (网络异常)", pageCount = 20, releaseDate = "离线"))
        }
    }

    /**
     * Fetches pages from API: GET /comics/{id}/order/{order}/pages
     */
    suspend fun getPagesForChapter(comicId: String, chapterOrder: Int): List<ComicPage> {
        return try {
            val resp = apiService.getComicPages(comicId, chapterOrder, page = 1)
            val docs = resp.data?.pages?.docs.orEmpty()
            val total = resp.data?.pages?.total ?: docs.size
            if (docs.isNotEmpty()) {
                docs.mapIndexed { index, doc ->
                    val url = doc.media?.toImageUrl().orEmpty()
                    ComicPage(
                        pageNumber = index + 1,
                        totalPages = total,
                        imageUrl = url,
                        title = "第 $chapterOrder 话 - 第 ${index + 1} 页"
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getComments(comicId: String): List<Comment> {
        return listOf(
            Comment("c1", "哔咔骑士团", "🌸", 9, "画风和剧情都超级赞，感谢汉化组的辛勤付出！", "10分钟前", 382),
            Comment("c2", "次元漫游者", "⚡", 7, "分镜特别自然，液态玻璃阅读器翻页超级丝滑！", "1小时前", 195),
            Comment("c3", "夜之城读者", "☕", 5, "官方API实时拉取很流畅，追更体验满分！", "3小时前", 112)
        )
    }

    fun toggleFavorite(comicId: String) {
        _favorites.update { current ->
            if (current.contains(comicId)) current - comicId else current + comicId
        }
    }

    fun isFavorite(comicId: String): Boolean {
        return _favorites.value.contains(comicId)
    }

    fun recordReading(comicId: String, chapterTitle: String, chapterOrder: Int, page: Int, totalPages: Int) {
        val comic = getComicById(comicId)
        val title = comic?.title ?: "漫画 $comicId"
        val cover = comic?.coverUrl.orEmpty()
        val newRecord = ReadingRecord(
            comicId = comicId,
            comicTitle = title,
            comicCover = cover,
            lastChapterTitle = chapterTitle,
            lastChapterOrder = chapterOrder,
            currentPage = page,
            totalPages = totalPages,
            lastReadTimestamp = System.currentTimeMillis()
        )
        _history.update { current ->
            listOf(newRecord) + current.filterNot { it.comicId == comicId }
        }
    }

    companion object {
        val instance = ComicRepository()
    }
}
