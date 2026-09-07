package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.ComicRepository
import com.example.model.ComicItem
import com.example.ui.miuix.*
import com.example.ui.screens.*

sealed interface PicaDestination {
    data object MainTabs : PicaDestination
    data class ComicDetail(val comic: ComicItem) : PicaDestination
    data class Reader(val comic: ComicItem, val chapterOrder: Int, val page: Int) : PicaDestination
}

@Composable
fun PicaApp(
    repository: ComicRepository = remember { ComicRepository.instance },
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableIntStateOf(0) }
    var destination by remember { mutableStateOf<PicaDestination>(PicaDestination.MainTabs) }

    // Intercept back button
    BackHandler(enabled = destination !is PicaDestination.MainTabs) {
        when (val dest = destination) {
            is PicaDestination.Reader -> {
                destination = PicaDestination.ComicDetail(dest.comic)
            }
            is PicaDestination.ComicDetail -> {
                destination = PicaDestination.MainTabs
            }
            else -> {}
        }
    }

    val navItems = remember {
        listOf(
            NavItem(
                title = "探索",
                selectedIcon = Icons.Default.Explore,
                unselectedIcon = Icons.Outlined.Explore
            ),
            NavItem(
                title = "分类",
                selectedIcon = Icons.Default.Category,
                unselectedIcon = Icons.Outlined.Category
            ),
            NavItem(
                title = "书架",
                selectedIcon = Icons.Default.CollectionsBookmark,
                unselectedIcon = Icons.Outlined.CollectionsBookmark,
                badgeCount = 3 // New updates badge
            ),
            NavItem(
                title = "设置",
                selectedIcon = Icons.Default.Settings,
                unselectedIcon = Icons.Outlined.Settings
            )
        )
    }

    LiquidCanvasBackground(modifier = modifier) {
        // Destination Switcher
        when (val currentDest = destination) {
            is PicaDestination.MainTabs -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Content Page
                    when (currentTab) {
                        0 -> HomeScreen(
                            repository = repository,
                            onComicClick = { comic ->
                                destination = PicaDestination.ComicDetail(comic)
                            }
                        )
                        1 -> CategoriesScreen(
                            repository = repository,
                            onComicClick = { comic ->
                                destination = PicaDestination.ComicDetail(comic)
                            }
                        )
                        2 -> BookshelfScreen(
                            repository = repository,
                            onComicClick = { comic ->
                                destination = PicaDestination.ComicDetail(comic)
                            },
                            onReadComic = { comicId, chapterOrder, page ->
                                val comic = repository.getComicById(comicId)
                                if (comic != null) {
                                    destination = PicaDestination.Reader(comic, chapterOrder, page)
                                }
                            }
                        )
                        3 -> SettingsScreen(
                            repository = repository
                        )
                    }

                    // Floating Liquid Navigation Bar (悬浮液态导航栏)
                    LiquidFloatingNavBar(
                        items = navItems,
                        selectedIndex = currentTab,
                        onItemSelected = { currentTab = it },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            }

            is PicaDestination.ComicDetail -> {
                ComicDetailScreen(
                    comic = currentDest.comic,
                    repository = repository,
                    onBack = { destination = PicaDestination.MainTabs },
                    onReadChapter = { chapterOrder ->
                        destination = PicaDestination.Reader(currentDest.comic, chapterOrder, 1)
                    }
                )
            }

            is PicaDestination.Reader -> {
                ReaderScreen(
                    comic = currentDest.comic,
                    initialChapterOrder = currentDest.chapterOrder,
                    initialPage = currentDest.page,
                    repository = repository,
                    onBack = { destination = PicaDestination.ComicDetail(currentDest.comic) }
                )
            }
        }
    }
}
