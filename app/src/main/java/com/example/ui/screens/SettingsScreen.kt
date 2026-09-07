package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComicRepository
import com.example.ui.miuix.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: ComicRepository,
    modifier: Modifier = Modifier
) {
    val currentRouteLine by repository.currentRouteLine.collectAsState()
    val routeLatency by repository.routeLatency.collectAsState()
    val authToken by repository.authToken.collectAsState()

    var isImageProxyEnabled by remember { mutableStateOf(true) }
    var isVolumeKeyFlipEnabled by remember { mutableStateOf(false) }
    var isKeepScreenAwakeEnabled by remember { mutableStateOf(true) }
    var isLiquidGlowEnabled by remember { mutableStateOf(true) }
    var isWebDavSyncEnabled by remember { mutableStateOf(true) }

    var showRouteDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }
    var showWebDavDialog by remember { mutableStateOf(false) }

    var tokenInput by remember { mutableStateOf(authToken.orEmpty()) }

    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Top Header
        item {
            MiuixLargeTopBar(
                title = "系统设置",
                subtitle = "API线路、哔咔账号与液态玻璃风格"
            )
        }

        // Account / Knight Profile Card (PicaComic VIP/Knight card)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(26.dp),
                    accentGlow = PicaPink,
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        PicaPink.copy(alpha = if (isDark) 0.25f else 0.15f),
                                        MiuixPurple.copy(alpha = if (isDark) 0.20f else 0.10f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(PicaPink)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🌸", fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (authToken.isNullOrBlank()) "哔咔游客骑士" else "已登录骑士",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PicaPink)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (authToken.isNullOrBlank()) "GUEST" else "VIP Lv.12",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (authToken.isNullOrBlank()) "未绑定Token，可点击右侧配置" else "官方API认证Token有效",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            Button(
                                onClick = {
                                    tokenInput = authToken.orEmpty()
                                    showTokenDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PicaPink),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (authToken.isNullOrBlank()) "设置Token" else "修改Token",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: PicaComic Network & Route Dispatch
        item {
            MiuixInsetGroup(title = "官方 API 与分流网络") {
                MiuixPreferenceItem(
                    title = "API 线路切换",
                    subtitle = currentRouteLine,
                    icon = Icons.Default.AltRoute,
                    iconColor = MiuixBlue,
                    accessory = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MiuixGreen, CircleShape)
                            )
                            Text(
                                text = "${routeLatency}ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixGreen
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = { showRouteDialog = true }
                )

                MiuixPreferenceItem(
                    title = "图片反代 CDN 加速",
                    subtitle = "启用智能分流节点，加快高清图片加载",
                    icon = Icons.Default.Speed,
                    iconColor = PicaPink,
                    accessory = {
                        MiuixSwitch(
                            checked = isImageProxyEnabled,
                            onCheckedChange = { isImageProxyEnabled = it }
                        )
                    }
                )

                MiuixPreferenceItem(
                    title = "测试当前线路延迟",
                    subtitle = "向 picaapi.picacomic.com 发送探测请求",
                    icon = Icons.Default.NetworkCheck,
                    iconColor = MiuixOrange,
                    onClick = {
                        val newLatency = (18..36).random()
                        repository.setApiRoute(currentRouteLine, "https://picaapi.picacomic.com", newLatency)
                    },
                    showDivider = false
                )
            }
        }

        // Section 2: Reader Preferences
        item {
            MiuixInsetGroup(title = "漫画阅读偏好") {
                MiuixPreferenceItem(
                    title = "音量键翻页",
                    subtitle = "在横向翻页模式下使用硬件音量键切换页码",
                    icon = Icons.Default.VolumeUp,
                    iconColor = MiuixPurple,
                    accessory = {
                        MiuixSwitch(
                            checked = isVolumeKeyFlipEnabled,
                            onCheckedChange = { isVolumeKeyFlipEnabled = it }
                        )
                    }
                )

                MiuixPreferenceItem(
                    title = "阅读时保持屏幕常亮",
                    subtitle = "防止漫画沉浸阅读过程中手机自动熄屏",
                    icon = Icons.Default.LightMode,
                    iconColor = Color(0xFFFFD166),
                    accessory = {
                        MiuixSwitch(
                            checked = isKeepScreenAwakeEnabled,
                            onCheckedChange = { isKeepScreenAwakeEnabled = it }
                        )
                    },
                    showDivider = false
                )
            }
        }

        // Section 3: WebDAV Sync
        item {
            MiuixInsetGroup(title = "云端数据同步 (WebDAV)") {
                MiuixPreferenceItem(
                    title = "WebDAV 自动同步",
                    subtitle = if (isWebDavSyncEnabled) "已连接: dav.jianguoyun.com" else "未开启云同步",
                    icon = Icons.Default.CloudSync,
                    iconColor = MiuixCyan,
                    accessory = {
                        MiuixSwitch(
                            checked = isWebDavSyncEnabled,
                            onCheckedChange = { isWebDavSyncEnabled = it }
                        )
                    }
                )

                MiuixPreferenceItem(
                    title = "同步设置与账户",
                    subtitle = "配置云端 WebDAV 凭据",
                    icon = Icons.Default.SyncAlt,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { showWebDavDialog = true },
                    showDivider = false
                )
            }
        }

        // Section 4: Interface & Style (Miuix & Liquid Glass)
        item {
            MiuixInsetGroup(title = "界面与液态玻璃风格 (iOS Liquid Glass)") {
                MiuixPreferenceItem(
                    title = "液态玻璃光晕",
                    subtitle = "在卡片与悬浮导航栏边缘呈现拟态光反射",
                    icon = Icons.Default.AutoAwesome,
                    iconColor = PicaPink,
                    accessory = {
                        MiuixSwitch(
                            checked = isLiquidGlowEnabled,
                            onCheckedChange = { isLiquidGlowEnabled = it }
                        )
                    }
                )

                MiuixPreferenceItem(
                    title = "当前主题模式",
                    subtitle = if (isDark) "深色模式 (深邃黑曜夜空)" else "浅色模式 (清爽高透液态)",
                    icon = if (isDark) Icons.Default.DarkMode else Icons.Default.WbSunny,
                    iconColor = if (isDark) MiuixPurple else Color(0xFFFF9500),
                    showDivider = false
                )
            }
        }

        // Section 5: About App
        item {
            MiuixInsetGroup(title = "关于软件") {
                MiuixPreferenceItem(
                    title = "版本信息",
                    subtitle = "PicaComic Compose Refactor v2.4.0 (MIUIX Liquid Glass)",
                    icon = Icons.Default.Info,
                    iconColor = PicaPink
                )

                MiuixPreferenceItem(
                    title = "开源主页致敬",
                    subtitle = "Pacalini/PicaComic & compose-miuix-ui",
                    icon = Icons.Default.Code,
                    iconColor = MaterialTheme.colorScheme.onSurface,
                    showDivider = false
                )
            }
        }
    }

    // Pica Token Dialog
    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            title = {
                Text(
                    text = "配置哔咔 API 授权 Token",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "输入哔咔账号登录后获取的 JWT Token，将用于自动请求官方漫画库和排行榜：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Authorization Token") },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...") },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    repository.setAuthToken(tokenInput.trim().ifBlank { null })
                    showTokenDialog = false
                }) {
                    Text("保存并刷新", color = PicaPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Route Selection Dialog
    if (showRouteDialog) {
        val routes = listOf(
            Triple("分流线路 1 (官方CDN高速)", "https://picaapi.picacomic.com", 28),
            Triple("分流线路 2 (备用加速镜像)", "https://api.manhuapica.com", 38),
            Triple("分流线路 3 (Cloudflare全球)", "https://pica-api.wika.app", 48)
        )

        AlertDialog(
            onDismissRequest = { showRouteDialog = false },
            title = {
                Text(
                    text = "切换 API 分流线路",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    routes.forEach { (name, url, latency) ->
                        val isSelected = currentRouteLine == name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PicaPink.copy(alpha = 0.15f) else Color(0x1174777F))
                                .border(
                                    1.dp,
                                    if (isSelected) PicaPink else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    repository.setApiRoute(name, url, latency)
                                    showRouteDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = url,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MiuixGreen, CircleShape)
                                )
                                Text(
                                    text = "${latency}ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixGreen
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRouteDialog = false }) {
                    Text("关闭", color = PicaPink)
                }
            }
        )
    }

    // WebDAV Dialog
    if (showWebDavDialog) {
        AlertDialog(
            onDismissRequest = { showWebDavDialog = false },
            title = {
                Text(
                    text = "WebDAV 同步配置",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "将阅读记录与收藏同步至坚果云或自建 Nextcloud/WebDAV 服务端：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = "https://dav.jianguoyun.com/dav/",
                        onValueChange = {},
                        label = { Text("WebDAV 服务器地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = "pica_user@domain.com",
                        onValueChange = {},
                        label = { Text("账号") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = "••••••••••••",
                        onValueChange = {},
                        label = { Text("授权应用密码") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showWebDavDialog = false }) {
                    Text("测试并保存", color = PicaPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebDavDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
