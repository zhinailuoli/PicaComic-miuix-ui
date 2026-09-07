package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComicRepository
import com.example.model.ComicItem
import com.example.model.ReadingRecord
import com.example.ui.miuix.*
import com.example.ui.theme.*

@Composable
fun BookshelfScreen(
    repository: ComicRepository,
    onComicClick: (ComicItem) -> Unit,
    onReadComic: (comicId: String, chapterOrder: Int, page: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 收藏, 1: 历史, 2: 下载
    val favoritesIds by repository.favorites.collectAsState()
    val historyRecords by repository.history.collectAsState()
    val allComics = remember(favoritesIds, historyRecords) { repository.getAllCachedComics() }

    val favoriteComics = remember(favoritesIds, allComics) {
        allComics.filter { favoritesIds.contains(it.id) }
    }

    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Top Large Header
        item {
            MiuixLargeTopBar(
                title = "我的书架",
                subtitle = "管理收藏、追更记录与离线漫画",
                actions = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x331C2235) else Color(0x1F000000))
                            .clickable { /* Cloud sync */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "WebDAV同步",
                            tint = PicaPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }

        // MIUIX Segmented Control
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                MiuixSegmentedControl(
                    items = listOf("我的收藏 (${favoriteComics.size})", "阅读历史", "离线缓存"),
                    selectedIndex = selectedTab,
                    onItemSelected = { selectedTab = it }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // FAVORITES TAB
                if (favoriteComics.isEmpty()) {
                    item {
                        EmptyBookshelfState(
                            title = "暂无收藏漫画",
                            subtitle = "快去“探索”发现心仪的精彩作品吧！"
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "收藏列表 (${favoriteComics.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 10.dp)
                        )
                    }

                    items(favoriteComics.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ComicGridItem(comic = pair[0], onClick = { onComicClick(pair[0]) })
                            }

                            if (pair.size > 1) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ComicGridItem(comic = pair[1], onClick = { onComicClick(pair[1]) })
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            1 -> {
                // HISTORY TAB
                if (historyRecords.isEmpty()) {
                    item {
                        EmptyBookshelfState(
                            title = "暂无阅读历史",
                            subtitle = "开始阅读漫画后，历史记录将自动保存在此"
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "阅读足迹",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 10.dp)
                        )
                    }

                    items(historyRecords) { record ->
                        HistoryRecordCard(
                            record = record,
                            onClick = {
                                val comic = repository.getComicById(record.comicId)
                                if (comic != null) onComicClick(comic)
                            },
                            onResumeRead = {
                                onReadComic(record.comicId, record.lastChapterOrder, record.currentPage)
                            }
                        )
                    }
                }
            }

            2 -> {
                // DOWNLOADS TAB
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Storage Card
                        LiquidGlassCard(
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "离线缓存空间",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "已占用 386 MB",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PicaPink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Progress bar
                                LinearProgressIndicator(
                                    progress = { 0.18f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PicaPink,
                                    trackColor = if (isDark) Color(0x331C2235) else Color(0x1F000000)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "已下载 3 部漫画 · 12 个章节",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "设备可用 42.8 GB",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "已下载内容",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        MiuixInsetGroup {
                            allComics.take(3).forEach { comic ->
                                MiuixPreferenceItem(
                                    title = comic.title,
                                    subtitle = "已下载 第 01~04 话 · 128 MB",
                                    icon = Icons.Default.FileDownloadDone,
                                    iconColor = MiuixGreen,
                                    accessory = {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(PicaPink.copy(alpha = 0.15f))
                                                .clickable {
                                                    onReadComic(comic.id, 1, 1)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "阅读",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = PicaPink
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: ReadingRecord,
    onClick: () -> Unit,
    onResumeRead: () -> Unit
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(22.dp),
        elevation = 4.dp,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComicCoverImage(
                coverUrl = record.comicCover,
                contentDescription = record.comicTitle,
                modifier = Modifier.size(68.dp),
                cornerRadius = 14.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.comicTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = record.lastChapterTitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = PicaPink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                val progress = (record.currentPage.toFloat() / record.totalPages.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PicaPink,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    Text(
                        text = "${record.currentPage}/${record.totalPages}P",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Resume Reading Capsule Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PicaPink, MiuixPurple)
                        )
                    )
                    .clickable(onClick = onResumeRead)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "继续",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyBookshelfState(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PicaPink.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = null,
                    tint = PicaPink,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
