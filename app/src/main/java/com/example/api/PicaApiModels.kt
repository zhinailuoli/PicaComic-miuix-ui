package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicaResponse<T>(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "message") val message: String = "",
    @Json(name = "data") val data: T? = null
)

@JsonClass(generateAdapter = true)
data class CategoriesData(
    @Json(name = "categories") val categories: List<PicaCategoryResponse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PicaCategoryResponse(
    @Json(name = "_id") val _id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "thumb") val thumb: PicaThumbResponse? = null,
    @Json(name = "isWeb") val isWeb: Boolean = false,
    @Json(name = "active") val active: Boolean = true,
    @Json(name = "link") val link: String? = null
)

@JsonClass(generateAdapter = true)
data class PicaThumbResponse(
    @Json(name = "originalName") val originalName: String? = null,
    @Json(name = "path") val path: String = "",
    @Json(name = "fileServer") val fileServer: String = "https://img.picaacg.com"
) {
    fun toImageUrl(): String {
        if (path.isBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = fileServer.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return if (cleanPath.startsWith("static/")) "$base/$cleanPath" else "$base/static/$cleanPath"
    }
}

@JsonClass(generateAdapter = true)
data class ComicsPageData(
    @Json(name = "comics") val comics: ComicsListContainer? = null
)

@JsonClass(generateAdapter = true)
data class ComicsListContainer(
    @Json(name = "docs") val docs: List<PicaComicDoc> = emptyList(),
    @Json(name = "total") val total: Int = 0,
    @Json(name = "limit") val limit: Int = 20,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "pages") val pages: Int = 1
)

@JsonClass(generateAdapter = true)
data class RandomComicsData(
    @Json(name = "comics") val comics: List<PicaComicDoc> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LeaderboardData(
    @Json(name = "comics") val comics: List<PicaComicDoc> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ComicDetailData(
    @Json(name = "comic") val comic: PicaComicDoc? = null
)

@JsonClass(generateAdapter = true)
data class EpsData(
    @Json(name = "eps") val eps: EpsContainer? = null
)

@JsonClass(generateAdapter = true)
data class EpsContainer(
    @Json(name = "docs") val docs: List<PicaEpDoc> = emptyList(),
    @Json(name = "total") val total: Int = 0,
    @Json(name = "limit") val limit: Int = 40,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "pages") val pages: Int = 1
)

@JsonClass(generateAdapter = true)
data class PicaEpDoc(
    @Json(name = "_id") val _id: String = "",
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "order") val order: Int = 1,
    @Json(name = "updated_at") val updated_at: String = ""
)

@JsonClass(generateAdapter = true)
data class PagesData(
    @Json(name = "pages") val pages: PagesContainer? = null
)

@JsonClass(generateAdapter = true)
data class PagesContainer(
    @Json(name = "docs") val docs: List<PicaPageDoc> = emptyList(),
    @Json(name = "total") val total: Int = 0,
    @Json(name = "limit") val limit: Int = 40,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "pages") val pages: Int = 1
)

@JsonClass(generateAdapter = true)
data class PicaPageDoc(
    @Json(name = "_id") val _id: String = "",
    @Json(name = "media") val media: PicaThumbResponse? = null,
    @Json(name = "id") val id: String = ""
)

@JsonClass(generateAdapter = true)
data class PicaComicDoc(
    @Json(name = "_id") val _id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "author") val author: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "chineseTeam") val chineseTeam: String? = null,
    @Json(name = "categories") val categories: List<String>? = null,
    @Json(name = "tags") val tags: List<String>? = null,
    @Json(name = "pagesCount") val pagesCount: Int? = null,
    @Json(name = "epsCount") val epsCount: Int? = null,
    @Json(name = "finished") val finished: Boolean? = null,
    @Json(name = "totalViews") val totalViews: Long? = null,
    @Json(name = "totalLikes") val totalLikes: Long? = null,
    @Json(name = "likesCount") val likesCount: Long? = null,
    @Json(name = "commentsCount") val commentsCount: Long? = null,
    @Json(name = "thumb") val thumb: PicaThumbResponse? = null,
    @Json(name = "updated_at") val updated_at: String? = null,
    @Json(name = "created_at") val created_at: String? = null
)

@JsonClass(generateAdapter = true)
data class SignInResponse(
    @Json(name = "token") val token: String = ""
)
