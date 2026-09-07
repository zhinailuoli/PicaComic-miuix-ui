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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComicRepository
import com.example.model.CategoryItem
import com.example.model.ComicItem
import com.example.model.UiResource
import com.example.ui.miuix.*
import com.example.ui.theme.*

@Composable
fun CategoriesScreen(
    repository: ComicRepository,
    onComicClick: (ComicItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoriesState by repository.categoriesState.collectAsState()
    val categoryComicsMap by repository.categoryComicsState.collectAsState()

    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val isDark = isSystemInDarkTheme()

    val popularTags = remember {
        listOf("全部", "热血", "奇幻", "冒险", "全彩", "精选", "汉化", "日漫", "韩漫", "同人", "纯爱", "恋爱", "悬疑")
    }

    // When a category is clicked, automatically load its comics from the API
    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { cat ->
            repository.loadCategoryComics(cat.name)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // 1. Top Header
        item {
            MiuixLargeTopBar(
                title = if (selectedCategory != null) selectedCategory!!.name else "分类探索",
                subtitle = if (selectedCategory != null) "分类下的实时收录漫画" else "哔咔漫画官方分类 · API自动同步",
                actions = {
                    if (selectedCategory != null) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x331C2235) else Color(0x1F000000))
                                .clickable { selectedCategory = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回分类列表",
                                tint = PicaPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x331C2235) else Color(0x1F000000))
                                .clickable { repository.loadCategories() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新分类",
                                tint = PicaPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }

        // 2. Tag Filter Chips Carousel (When on top categories view)
        if (selectedCategory == null) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularTags) { tag ->
                        val isSelected = (selectedTag == tag) || (selectedTag == null && tag == "全部")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(PicaPink.copy(alpha = 0.35f), MiuixPurple.copy(alpha = 0.25f))
                                                )
                                            )
                                            .border(1.dp, PicaPink, RoundedCornerShape(16.dp))
                                    } else {
                                        Modifier
                                            .background(if (isDark) Color(0x331C2235) else Color(0x99FFFFFF))
                                            .border(
                                                0.8.dp,
                                                Color.White.copy(alpha = if (isDark) 0.15f else 0.6f),
                                                RoundedCornerShape(16.dp)
                                            )
                                    }
                                )
                                .clickable {
                                    selectedTag = if (tag == "全部") null else tag
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) PicaPink else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Detail View (if category selected)
        if (selectedCategory != null) {
            val catName = selectedCategory!!.name
            val catState = categoryComicsMap[catName] ?: UiResource.Loading

            when (catState) {
                is UiResource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = PicaPink, modifier = Modifier.size(32.dp))
                                Text(
                                    text = "正在从API拉取 $catName 分类漫画...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                is UiResource.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            LiquidGlassCard(
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = catState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Button(
                                        onClick = { repository.loadCategoryComics(catName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PicaPink),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text("重新拉取")
                                    }
                                }
                            }
                        }
                    }
                }

                is UiResource.Success -> {
                    val comics = catState.data
                    if (comics.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "该分类暂未查询到漫画",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(comics.chunked(2)) { pair ->
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
        } else {
            // 4. Categories Overview Grid (Auto-fetched from API)
            when (val state = categoriesState) {
                is UiResource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = PicaPink, modifier = Modifier.size(36.dp))
                                Text(
                                    text = "正在拉取哔咔分类列表...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                is UiResource.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            LiquidGlassCard(
                                shape = RoundedCornerShape(22.dp),
                                accentGlow = Color(0xFFFF5252),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFFF5252)
                                    )
                                    Button(
                                        onClick = { repository.loadCategories() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PicaPink),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("重试获取分类")
                                    }
                                }
                            }
                        }
                    }
                }

                is UiResource.Success -> {
                    val rawCategories = state.data
                    val filteredCategories = if (selectedTag == null) rawCategories
                        else rawCategories.filter { it.name.contains(selectedTag!!) || it.description.contains(selectedTag!!) }

                    item {
                        Text(
                            text = "官方精选分类 (${filteredCategories.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(filteredCategories.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryBentoCard(
                                    category = pair[0],
                                    onClick = { selectedCategory = pair[0] }
                                )
                            }

                            if (pair.size > 1) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CategoryBentoCard(
                                        category = pair[1],
                                        onClick = { selectedCategory = pair[1] }
                                    )
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
fun CategoryBentoCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (category.sampleCover.isNotBlank()) {
                ComicCoverImage(
                    coverUrl = category.sampleCover,
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 24.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    PicaPink.copy(alpha = if (isDark) 0.3f else 0.15f),
                                    MiuixPurple.copy(alpha = if (isDark) 0.25f else 0.1f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PicaPink.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.iconEmoji, fontSize = 18.sp)
                }

                // Bottom Title & Count
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (category.sampleCover.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category.count,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = if (category.sampleCover.isNotBlank()) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
