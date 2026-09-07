package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ComicRepository
import com.example.model.ComicItem
import com.example.model.ComicPage
import com.example.ui.miuix.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ReaderMode {
    VERTICAL_SCROLL, // Webtoon continuous scroll (条漫)
    HORIZONTAL_FLIP // Horizontal paginated flip (日漫翻页)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    comic: ComicItem,
    initialChapterOrder: Int,
    initialPage: Int,
    repository: ComicRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentChapterOrder by remember { mutableIntStateOf(initialChapterOrder) }
    var readerMode by remember { mutableStateOf(ReaderMode.VERTICAL_SCROLL) }
    var isHudVisible by remember { mutableStateOf(true) }
    var isEyeProtectionMode by remember { mutableStateOf(false) }

    var pages by remember { mutableStateOf<List<ComicPage>>(emptyList()) }
    var isLoadingPages by remember { mutableStateOf(true) }

    LaunchedEffect(comic.id, currentChapterOrder) {
        isLoadingPages = true
        val result = repository.getPagesForChapter(comic.id, currentChapterOrder)
        pages = if (result.isNotEmpty()) result else {
            // Fallback placeholder pages for viewing if API chapter images are loading
            (1..12).map { idx ->
                ComicPage(
                    pageNumber = idx,
                    totalPages = 12,
                    imageUrl = comic.coverUrl,
                    title = "第 $currentChapterOrder 话 - 第 $idx 页"
                )
            }
        }
        isLoadingPages = false
    }

    val totalPages = pages.size.coerceAtLeast(1)
    var currentPageIndex by remember { mutableIntStateOf(initialPage.coerceIn(1, totalPages)) }

    val coroutineScope = rememberCoroutineScope()
    val verticalListState = rememberLazyListState(initialFirstVisibleItemIndex = (initialPage - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = (initialPage - 1).coerceIn(0, (totalPages - 1).coerceAtLeast(0)),
        pageCount = { totalPages }
    )

    // Sync page position
    LaunchedEffect(verticalListState.firstVisibleItemIndex) {
        if (readerMode == ReaderMode.VERTICAL_SCROLL && pages.isNotEmpty()) {
            currentPageIndex = (verticalListState.firstVisibleItemIndex + 1).coerceIn(1, pages.size)
            repository.recordReading(comic.id, "第 $currentChapterOrder 话", currentChapterOrder, currentPageIndex, pages.size)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (readerMode == ReaderMode.HORIZONTAL_FLIP && pages.isNotEmpty()) {
            currentPageIndex = (pagerState.currentPage + 1).coerceIn(1, pages.size)
            repository.recordReading(comic.id, "第 $currentChapterOrder 话", currentChapterOrder, currentPageIndex, pages.size)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Reader Content with Center Tap Area to toggle HUD
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isHudVisible = !isHudVisible
                }
        ) {
            if (isLoadingPages) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = PicaPink)
                        Text(
                            text = "正在从API加载第 $currentChapterOrder 话页面...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            } else if (readerMode == ReaderMode.VERTICAL_SCROLL) {
                LazyColumn(
                    state = verticalListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(pages) { _, page ->
                        ReaderPageItem(
                            page = page,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(560.dp)
                        )
                    }
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIdx ->
                    val page = pages.getOrNull(pageIdx)
                    if (page != null) {
                        ReaderPageItem(
                            page = page,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Eye Protection Tint Filter
        if (isEyeProtectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33FFB300))
            )
        }

        // Top Floating Glass HUD
        AnimatedVisibility(
            visible = isHudVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "退出阅读",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column {
                                Text(
                                    text = comic.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "第 $currentChapterOrder 话 · $currentPageIndex / $totalPages",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = PicaPink
                                )
                            }
                        }

                        // Right actions (Eye protection toggle, Mode Switcher)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { isEyeProtectionMode = !isEyeProtectionMode },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "护眼模式",
                                    tint = if (isEyeProtectionMode) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Mode Switcher Capsule
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PicaPink.copy(alpha = 0.18f))
                                    .clickable {
                                        readerMode = if (readerMode == ReaderMode.VERTICAL_SCROLL) ReaderMode.HORIZONTAL_FLIP else ReaderMode.VERTICAL_SCROLL
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (readerMode == ReaderMode.VERTICAL_SCROLL) "条漫" else "日漫",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PicaPink
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Floating Glass HUD
        AnimatedVisibility(
            visible = isHudVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(28.dp),
                    elevation = 12.dp,
                    accentGlow = PicaPink,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Slider Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$currentPageIndex",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Slider(
                                value = currentPageIndex.toFloat(),
                                onValueChange = { targetPage ->
                                    val pageInt = targetPage.toInt()
                                    currentPageIndex = pageInt
                                    coroutineScope.launch {
                                        if (readerMode == ReaderMode.VERTICAL_SCROLL) {
                                            verticalListState.scrollToItem((pageInt - 1).coerceAtLeast(0))
                                        } else {
                                            pagerState.scrollToPage((pageInt - 1).coerceAtLeast(0))
                                        }
                                    }
                                },
                                valueRange = 1f..totalPages.toFloat(),
                                steps = (totalPages - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = PicaPink,
                                    activeTrackColor = PicaPink,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "$totalPages",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Chapter Previous / Next row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    if (currentChapterOrder > 1) {
                                        currentChapterOrder--
                                        currentPageIndex = 1
                                    }
                                },
                                enabled = currentChapterOrder > 1
                            ) {
                                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("上一话")
                            }

                            Text(
                                text = "第 $currentChapterOrder 话",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = PicaPink
                            )

                            TextButton(
                                onClick = {
                                    if (currentChapterOrder < comic.chaptersCount) {
                                        currentChapterOrder++
                                        currentPageIndex = 1
                                    }
                                },
                                enabled = currentChapterOrder < comic.chaptersCount
                            ) {
                                Text("下一话")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReaderPageItem(
    page: ComicPage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (page.imageUrl.isNotBlank()) {
            AsyncImage(
                model = page.imageUrl,
                contentDescription = page.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Stylized Comic placeholder frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E2235), Color(0xFF121420))
                        )
                    )
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = PicaPink,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "哔咔漫画官方API页面",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
