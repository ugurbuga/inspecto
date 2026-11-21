package com.ugurbuga.inspectodemo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.InspectoApp
import com.ugurbuga.inspectodemo.shared.generated.resources.Res
import com.ugurbuga.inspectodemo.shared.generated.resources.outfit
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    LaunchedEffect(Unit) {
        Inspecto.requestNotificationPermission()
    }

    val outfitFont = FontFamily(Font(Res.font.outfit))
    InspectoApp(fontFamily = outfitFont) {
        var isLoading by remember { mutableStateOf(false) }

        InspectoDemoContent(
            isLoading = isLoading,
            onUsersClick = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoKtorRunner.fetchUsers()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onPostsGetClick = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoKtorRunner.fetchPosts()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onPostsPostClick = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoKtorRunner.submitPost()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onCommentsClick = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoKtorRunner.fetchComments()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onErr400Click = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoKtorRunner.triggerBadRequest400()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onErr500Click = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoKtorRunner.triggerServerError500()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onOkHttpClick = {
                isLoading = true
                performOkHttpTestRequest("https://jsonplaceholder.typicode.com/posts") { _ ->
                    isLoading = false
                }
            },
            onNSURLSessionClick = {
                isLoading = true
                performNSURLSessionTestRequest("https://jsonplaceholder.typicode.com/posts") { _ ->
                    isLoading = false
                }
            },
            onApolloClick = {
                isLoading = true
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    try {
                        InspectoApolloRunner.executeQuery()
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            },
            onOpenInspecto = { Inspecto.launch() },
            onManualLogClick = {
                InspectoManualLogRunner.triggerManualLog()
            },
            onRequestNotificationPermission = {
                Inspecto.requestNotificationPermission {}
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InspectoDemoContent(
    isLoading: Boolean = false,
    onUsersClick: () -> Unit = {},
    onPostsGetClick: () -> Unit = {},
    onPostsPostClick: () -> Unit = {},
    onCommentsClick: () -> Unit = {},
    onErr400Click: () -> Unit = {},
    onErr500Click: () -> Unit = {},
    onOkHttpClick: () -> Unit = {},
    onNSURLSessionClick: () -> Unit = {},
    onApolloClick: () -> Unit = {},
    onOpenInspecto: () -> Unit = {},
    onManualLogClick: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {}
) {
    val logs by Inspecto.logs.collectAsStateWithLifecycle()
    val totalCount = logs.size

    var prevCount by remember { mutableStateOf(totalCount) }
    val pulseScale = remember { Animatable(1f) }
    val pulseBorderAlpha = remember { Animatable(0f) }

    LaunchedEffect(totalCount) {
        if (totalCount != prevCount) {
            prevCount = totalCount
            pulseScale.animateTo(1.35f, animationSpec = tween(120))
            pulseScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            pulseBorderAlpha.animateTo(1f, animationSpec = tween(100))
            pulseBorderAlpha.animateTo(0f, animationSpec = tween(400))
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Inspecto Network Inspector", fontWeight = FontWeight.Bold)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // HERO CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Inspecto Dashboard",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Captured Network Traffic",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(
                                        width = (2 * pulseBorderAlpha.value).dp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    ),
                                    modifier = Modifier.scale(pulseScale.value)
                                ) {
                                    Text(
                                        text = "$totalCount Calls",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenInspecto,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Launch Inspecto", fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = onRequestNotificationPermission
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notification Settings",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isLoading) 1f else 0f)
                    )

                    // SECTION 1: KTOR CALLS
                    CategoryCard(
                        title = "Ktor HTTP Client",
                        icon = Icons.Default.Http,
                        iconColor = Color(0xFF2563EB)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DemoActionButton(
                                text = "GET Users",
                                color = Color(0xFF2563EB),
                                enabled = !isLoading,
                                onClick = onUsersClick,
                                modifier = Modifier.weight(1f)
                            )
                            DemoActionButton(
                                text = "GET Posts",
                                color = Color(0xFF0EA5E9),
                                enabled = !isLoading,
                                onClick = onPostsGetClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DemoActionButton(
                                text = "POST Submit",
                                color = Color(0xFF10B981),
                                enabled = !isLoading,
                                onClick = onPostsPostClick,
                                modifier = Modifier.weight(1f)
                            )
                            DemoActionButton(
                                text = "GET Comments",
                                color = Color(0xFF8B5CF6),
                                enabled = !isLoading,
                                onClick = onCommentsClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // SECTION 2: ERROR SIMULATION
                    CategoryCard(
                        title = "Error Triggers & Edge Cases",
                        icon = Icons.Default.BugReport,
                        iconColor = Color(0xFFE11D48)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DemoActionButton(
                                text = "Trigger 400 Error",
                                color = Color(0xFFE11D48),
                                enabled = !isLoading,
                                onClick = onErr400Click,
                                modifier = Modifier.weight(1f)
                            )
                            DemoActionButton(
                                text = "Trigger 500 Error",
                                color = Color(0xFF9333EA),
                                enabled = !isLoading,
                                onClick = onErr500Click,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // SECTION 3: OTHER STACKS (OkHttp, NSURLSession, Apollo)
                    CategoryCard(
                        title = "Other Network Stacks",
                        icon = Icons.Default.Code,
                        iconColor = Color(0xFFF59E0B)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isOkHttpSupported) {
                                DemoActionButton(
                                    text = "OkHttp / Retrofit Request",
                                    color = Color(0xFFF59E0B),
                                    enabled = !isLoading,
                                    onClick = onOkHttpClick,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (isNSURLSessionSupported) {
                                DemoActionButton(
                                    text = "iOS NSURLSession Request",
                                    color = Color(0xFFF59E0B),
                                    enabled = !isLoading,
                                    onClick = onNSURLSessionClick,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            DemoActionButton(
                                text = "Apollo GraphQL Query",
                                color = Color(0xFFEC4899),
                                enabled = !isLoading,
                                onClick = onApolloClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // SECTION 4: UTILITY & MANUAL LOGS
                    CategoryCard(
                        title = "Manual Logging & Tools",
                        icon = Icons.Default.Speed,
                        iconColor = Color(0xFF64748B)
                    ) {
                        OutlinedButton(
                            onClick = onManualLogClick,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Trigger Custom Manual Log", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val adjustedIconColor = when (iconColor) {
        Color(0xFF2563EB) -> if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
        Color(0xFFE11D48) -> if (isDark) Color(0xFFF87171) else Color(0xFFBE123C)
        Color(0xFFF59E0B) -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
        Color(0xFF64748B) -> if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)
        else -> if (isDark) iconColor.copy(red = 1f, green = 1f, blue = 1f) else iconColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = adjustedIconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun DemoActionButton(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(name = "Inspecto Demo - Light")
@Composable
fun InspectoDemoLightPreview() {
    InspectoDemoContent()
}

@Preview(name = "Inspecto Demo - Dark")
@Composable
fun InspectoDemoDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface {
            InspectoDemoContent()
        }
    }
}
