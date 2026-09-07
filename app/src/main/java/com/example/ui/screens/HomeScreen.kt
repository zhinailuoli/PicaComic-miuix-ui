package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ComicRepository
import com.example.model.ComicItem
import com.example.model.UiResource
import com.example.ui.miuix.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: ComicRepository,
    onComicClick: (ComicItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRouteLine by repository.currentRouteLine.collectAsState()
    val routeLatency by repository.routeLatency.collectAsState()

    val exploreState by repository.exploreComicsState.collectAsState()
    val leaderboardState by repository.leaderboardState.collectAsState()

    var selectedExploreTab by remember { mutableIntStateOf(0) } // 0: 热门探索, 1: 24h榜, 2: 7d周榜, 3: 30d月榜
    var searchQuery by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()

    // Determine current comic list based on tab
    val currentComicsList = remember(selectedExploreTab, exploreState, leaderboardState) {
        when (selectedExploreTab) {
            0 -> (exploreState as? UiResource.Success)?.data.orEmpty()
            else -> (leaderboardState as? UiResource.Success)?.data.orEmpty()
        }
    }

    val currentUiState = remember(selectedExploreTab, exploreState, leaderboardState) {
        when (selectedExploreTab) {
            0 -> exploreState
            else -> leaderboardState
        }
    }

    val displayedComics = remember(searchQuery, currentComicsList) {
        if (searchQuery.isBlank()) {
            currentComicsList
        } else {
            currentComicsList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) } ||
                it.categories.any { cat -> cat.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val featuredComic = remember(currentComicsList) {
        currentComicsList.firstOrNull()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // 1. Top Header with PicaComic Title & API Route status capsule
        item {
            MiuixLargeTopBar(
                title = "PicaComic",
                subtitle = "探索无限精彩漫画次元 · API实时获取",
                actions = {
                    // API Status Capsule (Tap to refresh)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        PicaPink.copy(alpha = 0.25f),
                                        MiuixPurple.copy(alpha = 0.15f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                PicaPink.copy(alpha = 0.6f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (selectedExploreTab == 0) {
                                    repository.loadExploreComics()
                                } else {
                                    val tt = when (selectedExploreTab) {
                                        1 -> "H24"
                                        2 -> "D7"
                                        else -> "D30"
                                    }
                                    repository.loadLeaderboard(tt)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "⚡", fontSize = 12.sp)
                            Text(
                                text = "${routeLatency}ms",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PicaPink
                            )
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新API",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            )
        }

        // 2. Liquid Glass Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (isDark) Color(0x4D1A1F30) else Color(0xCCFFFFFF))
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (isDark) 0.4f else 0.9f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = PicaPink,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索漫画名、作者、标签或类别...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(PicaPink),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清空",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Exploration Segmented Filter Tabs (探索分类标签)
        item {
            val tabs = listOf("🌟 热门推荐", "🔥 24小时榜", "📅 7天周榜", "🏆 30天月榜")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.indices.toList()) { idx ->
                    val isSelected = selectedExploreTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    PicaPink.copy(alpha = 0.35f),
                                                    MiuixPurple.copy(alpha = 0.25f)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            PicaPink,
                                            RoundedCornerShape(18.dp)
                                        )
                                } else {
                                    Modifier
                                        .background(if (isDark) Color(0x331C2235) else Color(0x99FFFFFF))
                                        .border(
                                            0.8.dp,
                                            Color.White.copy(alpha = if (isDark) 0.15f else 0.6f),
                                            RoundedCornerShape(18.dp)
                                        )
                                }
                            )
                            .clickable {
                                selectedExploreTab = idx
                                when (idx) {
                                    0 -> repository.loadExploreComics()
                                    1 -> repository.loadLeaderboard("H24")
                                    2 -> repository.loadLeaderboard("D7")
                                    3 -> repository.loadLeaderboard("D30")
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tabs[idx],
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) PicaPink else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 4. API State Handling (Loading, Error, Success)
        when (val state = currentUiState) {
            is UiResource.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidGlassCard(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = PicaPink,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "正在连接哔咔API拉取探索内容...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "节点: $currentRouteLine",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            is UiResource.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        LiquidGlassCard(
                            shape = RoundedCornerShape(24.dp),
                            accentGlow = Color(0xFFFF5252),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "API 连接提示",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF5252)
                                    )
                                }

                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "提示：哔咔漫画官方服务器需配置有效Token或在设置中切换网络线路。",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            if (selectedExploreTab == 0) repository.loadExploreComics()
                                            else repository.loadLeaderboard()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PicaPink),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("重新获取")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is UiResource.Success -> {
                // 5. Featured Hero Comic Banner (Liquid Glass Hero)
                if (searchQuery.isBlank() && featuredComic != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            FeaturedHeroCard(
                                comic = featuredComic,
                                onClick = { onComicClick(featuredComic) }
                            )
                        }
                    }
                }

                // 6. Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "搜索结果 (${displayedComics.size})" else "探索精选 (${displayedComics.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "自动同步",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = PicaPink
                        )
                    }
                }

                // Empty state
                if (displayedComics.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无匹配的漫画内容",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // 7. Bento Grid Comics Display (2 Columns)
                    items(displayedComics.chunked(2)) { pair ->
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
        }
    }
}

@Composable
fun FeaturedHeroCard(
    comic: ComicItem,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        shape = RoundedCornerShape(28.dp),
        elevation = 10.dp,
        accentGlow = PicaPink,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            ComicCoverImage(
                coverUrl = comic.coverUrl,
                contentDescription = comic.title,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 28.dp
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Info Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PicaPink)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🌸 哔咔今日探索精选",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = comic.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = comic.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(text = "·", color = Color.White.copy(alpha = 0.5f))
                    Text(
                        text = "${comic.views} 浏览",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD166)
                    )
                    Text(text = "·", color = Color.White.copy(alpha = 0.5f))
                    Text(
                        text = "${comic.likes} 爱心",
                        style = MaterialTheme.typography.bodySmall,
                        color = PicaPinkLight
                    )
                }
            }
        }
    }
}

@Composable
fun ComicGridItem(
    comic: ComicItem,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(22.dp),
        elevation = 6.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                ComicCoverImage(
                    coverUrl = comic.coverUrl,
                    contentDescription = comic.title,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 22.dp
                )

                // Chapter count pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xB3000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${comic.chaptersCount} 话",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = comic.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = comic.author,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val displayTags = (comic.categories + comic.tags).take(2)
                    displayTags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PicaPink.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = PicaPink
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComicCoverImage(
    coverUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0x221C2235)),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
