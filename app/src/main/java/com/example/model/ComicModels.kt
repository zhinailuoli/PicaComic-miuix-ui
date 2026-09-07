package com.example.model

import com.example.api.PicaCategoryResponse
import com.example.api.PicaComicDoc
import com.example.api.PicaEpDoc

sealed interface UiResource<out T> {
    data object Loading : UiResource<Nothing>
    data class Success<T>(val data: T) : UiResource<T>
    data class Error(val message: String, val canRetry: Boolean = true) : UiResource<Nothing>
}

data class ComicItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val author: String,
    val coverUrl: String,
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val rating: Float = 4.8f,
    val views: String = "0",
    val likes: String = "0",
    val chaptersCount: Int = 1,
    val pagesCount: Int = 0,
    val updateTime: String = "",
    val description: String = "",
    val isFinished: Boolean = false,
    val chineseTeam: String = "",
    val isFavorite: Boolean = false
)

data class Chapter(
    val id: String,
    val comicId: String,
    val order: Int,
    val title: String,
    val pageCount: Int,
    val releaseDate: String
)

data class ComicPage(
    val pageNumber: Int,
    val totalPages: Int,
    val imageUrl: String,
    val title: String
)

data class Comment(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val level: Int,
    val content: String,
    val timeAgo: String,
    val likes: Int
)

data class ReadingRecord(
    val comicId: String,
    val comicTitle: String,
    val comicCover: String,
    val lastChapterTitle: String,
    val lastChapterOrder: Int,
    val currentPage: Int,
    val totalPages: Int,
    val lastReadTimestamp: Long
)

data class CategoryItem(
    val id: String,
    val name: String,
    val count: String = "哔咔精选",
    val iconEmoji: String = "🌸",
    val description: String = "",
    val sampleCover: String = "",
    val isWeb: Boolean = false,
    val link: String? = null
)

// Extensions to convert Pica API responses to UI models
fun PicaComicDoc.toComicItem(): ComicItem {
    val cover = thumb?.toImageUrl().orEmpty()
    val viewsStr = totalViews?.let {
        if (it >= 10000) "%.1f万".format(it / 10000.0) else it.toString()
    } ?: "0"
    val likesStr = (totalLikes ?: likesCount)?.let {
        if (it >= 10000) "%.1f万".format(it / 10000.0) else it.toString()
    } ?: "0"

    return ComicItem(
        id = _id,
        title = title.ifBlank { "未命名漫画" },
        subtitle = chineseTeam.orEmpty(),
        author = author.orEmpty().ifBlank { "佚名" },
        coverUrl = cover,
        tags = tags ?: emptyList(),
        categories = categories ?: emptyList(),
        rating = 4.8f,
        views = viewsStr,
        likes = likesStr,
        chaptersCount = epsCount ?: 1,
        pagesCount = pagesCount ?: 0,
        updateTime = updated_at?.take(10) ?: "近期收录",
        description = description.orEmpty(),
        isFinished = finished ?: false,
        chineseTeam = chineseTeam.orEmpty()
    )
}

fun PicaCategoryResponse.toCategoryItem(): CategoryItem {
    val cover = thumb?.toImageUrl().orEmpty()
    val emoji = when {
        title.contains("生肉") -> "🥩"
        title.contains("全彩") -> "🎨"
        title.contains("汉化") -> "🇨🇳"
        title.contains("短篇") -> "⚡"
        title.contains("日漫") -> "🌸"
        title.contains("韩漫") -> "🇰🇷"
        title.contains("美漫") -> "🦸"
        title.contains("推荐") -> "🌟"
        title.contains("同人") -> "✨"
        title.contains("大家") -> "🔥"
        title.contains("官方") -> "👑"
        title.contains("骑士") -> "🛡️"
        title.contains("Cosplay") || title.contains("cos") -> "🎭"
        else -> "📚"
    }
    return CategoryItem(
        id = _id,
        name = title,
        count = "哔咔漫画",
        iconEmoji = emoji,
        description = description ?: "探索 $title 分类的精彩内容",
        sampleCover = cover,
        isWeb = isWeb,
        link = link
    )
}

fun PicaEpDoc.toChapter(comicId: String): Chapter {
    return Chapter(
        id = _id.ifBlank { id.ifBlank { "${comicId}_ep_$order" } },
        comicId = comicId,
        order = order,
        title = title.ifBlank { "第 $order 话" },
        pageCount = 24,
        releaseDate = updated_at.take(10).ifBlank { "近期更新" }
    )
}
