package io.github.ugurbuga.inspecto

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ugurbuga.inspecto.generated.resources.Res
import io.github.ugurbuga.inspecto.generated.resources.app_title
import io.github.ugurbuga.inspecto.generated.resources.no_network_calls_subtitle
import io.github.ugurbuga.inspecto.generated.resources.no_network_calls_title
import io.github.ugurbuga.inspecto.generated.resources.search_placeholder
import io.github.ugurbuga.inspecto.generated.resources.ic_close
import io.github.ugurbuga.inspecto.generated.resources.ic_search
import io.github.ugurbuga.inspecto.generated.resources.ic_inbox
import io.github.ugurbuga.inspecto.ui.bottomsheets.FilterBottomSheet
import io.github.ugurbuga.inspecto.ui.bottomsheets.SortBottomSheet
import io.github.ugurbuga.inspecto.ui.components.HeaderStatsTable
import io.github.ugurbuga.inspecto.ui.components.TransactionCard
import io.github.ugurbuga.inspecto.ui.components.noRippleClickable
import io.github.ugurbuga.inspecto.ui.detail.InspectoDetailScreen
import io.github.ugurbuga.inspecto.ui.theme.InspectoTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal enum class InspectoSortOption(val label: String) {
    DATE_DESC("Date (Newest first)"),
    DATE_ASC("Date (Oldest first)"),
    SIZE_DESC("Size (Largest first)"),
    SIZE_ASC("Size (Smallest first)"),
    DURATION_DESC("Duration (Longest first)"),
    DURATION_ASC("Duration (Shortest first)")
}

private fun entryMatchesFilter(entry: InspectoLogEntry, filter: String): Boolean {
    return when (filter.uppercase()) {
        "ALL" -> true
        "2XX" -> entry.statusCode in 200..299
        "3XX" -> entry.statusCode in 300..399
        "4XX" -> entry.statusCode in 400..499
        "5XX" -> entry.statusCode in 500..599
        "ERR" -> entry.isError
        "SUCCESS" -> entry.isSuccess
        else -> {
            entry.method.equals(filter, ignoreCase = true) ||
                    entry.tag.contains(filter, ignoreCase = true)
        }
    }
}

@Composable
internal fun InspectoScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    forceDarkTheme: Boolean? = null
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkThemeOverride by remember { mutableStateOf<Boolean?>(forceDarkTheme) }
    val isDark = isDarkThemeOverride ?: systemDark

    InspectoTheme(isDarkTheme = isDark) {
        InspectoScreenContent(
            onDismiss = onDismiss,
            isDarkTheme = isDark,
            onToggleTheme = { isDarkThemeOverride = !isDark },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectoScreenContent(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by Inspecto.logs.collectAsStateWithLifecycle()
    var selectedEntry by remember { mutableStateOf<InspectoLogEntry?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var filterMode by remember { mutableStateOf("AND") }
    var sortOption by remember { mutableStateOf(InspectoSortOption.DATE_DESC) }

    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }
    var isSortBottomSheetOpen by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    BackHandler(enabled = selectedEntry != null) {
        selectedEntry = null
    }

    val filteredLogs = remember(logs, searchQuery, selectedFilters, filterMode) {
        logs.filter { entry ->
            val matchesFilter = if (selectedFilters.isEmpty() || "ALL" in selectedFilters) {
                true
            } else {
                if (filterMode == "AND") {
                    selectedFilters.all { filter -> entryMatchesFilter(entry, filter) }
                } else {
                    selectedFilters.any { filter -> entryMatchesFilter(entry, filter) }
                }
            }

            val query = searchQuery.trim().lowercase()
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                when (query) {
                    "success" -> entry.isSuccess
                    "error", "err" -> entry.isError
                    "2xx" -> entry.statusCode in 200..299
                    else -> {
                        entry.url.contains(query, ignoreCase = true) ||
                                entry.method.contains(query, ignoreCase = true) ||
                                entry.statusCode.toString().contains(query) ||
                                entry.tag.contains(query, ignoreCase = true) ||
                                (entry.requestBody?.contains(query, ignoreCase = true) == true) ||
                                (entry.responseBody?.contains(query, ignoreCase = true) == true)
                    }
                }
            }

            matchesFilter && matchesSearch
        }
    }

    val sortedLogs = remember(filteredLogs, sortOption) {
        when (sortOption) {
            InspectoSortOption.DATE_DESC -> filteredLogs.sortedByDescending { it.timestampMs }
            InspectoSortOption.DATE_ASC -> filteredLogs.sortedBy { it.timestampMs }
            InspectoSortOption.SIZE_DESC -> filteredLogs.sortedByDescending { it.responseSizeByte }
            InspectoSortOption.SIZE_ASC -> filteredLogs.sortedBy { it.responseSizeByte }
            InspectoSortOption.DURATION_DESC -> filteredLogs.sortedByDescending { it.durationMs }
            InspectoSortOption.DURATION_ASC -> filteredLogs.sortedBy { it.durationMs }
        }
    }

    val isFilterActive = selectedFilters.isNotEmpty() && "ALL" !in selectedFilters
    val displayLogs = if (isFilterActive || searchQuery.isNotBlank()) filteredLogs else logs

    val totalCount = displayLogs.size
    val successCount = displayLogs.count { it.isSuccess }
    val errorCount = displayLogs.count { it.isError }
    val avgLatency = if (displayLogs.isNotEmpty()) displayLogs.map { it.durationMs }.average().toLong() else 0L

    if (isFilterBottomSheetOpen) {
        FilterBottomSheet(
            selectedFilters = selectedFilters,
            filterMode = filterMode,
            isDarkTheme = isDarkTheme,
            onApply = { filters, mode ->
                selectedFilters = filters
                filterMode = mode
            },
            onDismiss = { isFilterBottomSheetOpen = false }
        )
    }

    if (isSortBottomSheetOpen) {
        SortBottomSheet(
            sortOption = sortOption,
            isDarkTheme = isDarkTheme,
            onApply = { option -> sortOption = option },
            onDismiss = { isSortBottomSheetOpen = false }
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .noRippleClickable {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
        color = MaterialTheme.colorScheme.surface
    ) {
        if (selectedEntry != null) {
            InspectoDetailScreen(
                entry = selectedEntry!!,
                onBackClick = { selectedEntry = null },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_close),
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        title = {
                            Text(
                                text = stringResource(Res.string.app_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    HeaderStatsTable(
                        totalCount = totalCount,
                        successCount = successCount,
                        errorCount = errorCount,
                        avgLatency = avgLatency,
                        isDarkTheme = isDarkTheme,
                        isFilterActive = isFilterActive,
                        onOpenFilter = { isFilterBottomSheetOpen = true },
                        onOpenSort = { isSortBottomSheetOpen = true },
                        onToggleTheme = onToggleTheme
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        placeholder = {
                            Text(
                                stringResource(Res.string.search_placeholder),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_search),
                                contentDescription = "Search",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_close),
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp)
                    )

                    if (sortedLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_inbox),
                                    contentDescription = "No Calls",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(Res.string.no_network_calls_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(Res.string.no_network_calls_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        },
                                        onDrag = { _, _ -> }
                                    )
                                }
                        ) {
                            items(sortedLogs, key = { it.id }) { entry ->
                                TransactionCard(
                                    entry = entry,
                                    onClick = { selectedEntry = entry }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Inspecto List - Light")
@Composable
internal fun InspectoListLightPreview() {
    LaunchedEffect(Unit) {
        Inspecto.clear()
        Inspecto.addLog(
            url = "https://api.example.com/v1/users",
            method = "GET",
            statusCode = 200,
            durationMs = 120L,
            requestHeaders = mapOf("Authorization" to "Bearer token123"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[{\"id\": 1, \"name\": \"John Doe\"}]",
            tag = "Ktor"
        )
    }
    InspectoScreen(onDismiss = {})
}

@Preview(name = "Inspecto List - Dark")
@Composable
internal fun InspectoListDarkPreview() {
    LaunchedEffect(Unit) {
        Inspecto.clear()
        Inspecto.addLog(
            url = "https://api.example.com/v1/users",
            method = "GET",
            statusCode = 200,
            durationMs = 120L,
            requestHeaders = mapOf("Authorization" to "Bearer token123"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[{\"id\": 1, \"name\": \"John Doe\"}]",
            tag = "Ktor"
        )
    }
    InspectoScreen(onDismiss = {}, forceDarkTheme = true)
}
