package com.ugurbuga.inspecto

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.app_title
import com.ugurbuga.inspecto.generated.resources.ic_close
import com.ugurbuga.inspecto.generated.resources.ic_inbox
import com.ugurbuga.inspecto.generated.resources.ic_search
import com.ugurbuga.inspecto.generated.resources.no_network_calls_subtitle
import com.ugurbuga.inspecto.generated.resources.no_network_calls_title
import com.ugurbuga.inspecto.generated.resources.search_placeholder
import com.ugurbuga.inspecto.ui.bottomsheets.ClearConfirmationBottomSheet
import com.ugurbuga.inspecto.ui.bottomsheets.FilterBottomSheet
import com.ugurbuga.inspecto.ui.bottomsheets.SortBottomSheet
import com.ugurbuga.inspecto.ui.components.HeaderStatsTable
import com.ugurbuga.inspecto.ui.components.TransactionCard
import com.ugurbuga.inspecto.ui.components.noRippleClickable
import com.ugurbuga.inspecto.ui.detail.InspectoDetailScreen
import com.ugurbuga.inspecto.ui.theme.InspectoTheme
import com.ugurbuga.inspecto.ui.theme.getInspectoFontFamily
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
        InspectoStatusFilter.ALL.value -> true
        InspectoStatusFilter.STATUS_2XX.value -> entry.statusCode in 200..299
        InspectoStatusFilter.STATUS_3XX.value -> entry.statusCode in 300..399
        InspectoStatusFilter.STATUS_4XX.value -> entry.statusCode in 400..499
        InspectoStatusFilter.STATUS_5XX.value -> entry.statusCode in 500..599
        InspectoStatusFilter.ERR.value -> entry.isError
        InspectoStatusFilter.SUCCESS.value -> entry.isSuccess
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
    fontFamily: FontFamily? = null
) {
    val systemDark = isSystemInDarkTheme()
    var themeMode by remember { mutableStateOf<Boolean?>(null) }
    val isDark = themeMode ?: systemDark

    InspectoTheme(isDarkTheme = isDark, fontFamily = fontFamily ?: getInspectoFontFamily()) {
        InspectoScreenContent(
            onDismiss = onDismiss,
            isDarkTheme = isDark,
            themeMode = themeMode,
            onToggleTheme = {
                themeMode = when (themeMode) {
                    null -> false
                    false -> true
                    true -> null
                }
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectoScreenContent(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    themeMode: Boolean?,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by Inspecto.logs.collectAsStateWithLifecycle()
    var selectedEntry by remember { mutableStateOf<InspectoLogEntry?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var filterMode by remember { mutableStateOf(InspectoFilterMode.AND.key) }
    var sortOption by remember { mutableStateOf(InspectoSortOption.DATE_DESC) }

    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }
    var isSortBottomSheetOpen by remember { mutableStateOf(false) }
    var isClearConfirmationOpen by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    BackHandler(enabled = true) {
        if (selectedEntry != null) {
            selectedEntry = null
        } else {
            onDismiss()
        }
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
                when (InspectoQuickSearchQuery.match(query)) {
                    InspectoQuickSearchQuery.SUCCESS -> entry.isSuccess
                    InspectoQuickSearchQuery.ERROR, InspectoQuickSearchQuery.ERR -> entry.isError
                    InspectoQuickSearchQuery.STATUS_2XX -> entry.statusCode in 200..299
                    InspectoQuickSearchQuery.STATUS_3XX -> entry.statusCode in 300..399
                    InspectoQuickSearchQuery.STATUS_4XX -> entry.statusCode in 400..499
                    InspectoQuickSearchQuery.STATUS_5XX -> entry.statusCode in 500..599
                    null -> {
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

    if (isClearConfirmationOpen) {
        ClearConfirmationBottomSheet(
            isDarkTheme = isDarkTheme,
            onConfirm = { Inspecto.clear() },
            onDismiss = { isClearConfirmationOpen = false }
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
                        themeMode = themeMode,
                        isFilterActive = isFilterActive,
                        isSearchActive = searchQuery.isNotBlank(),
                        onOpenFilter = { isFilterBottomSheetOpen = true },
                        onOpenSort = { isSortBottomSheetOpen = true },
                        onToggleTheme = onToggleTheme,
                        onOpenClear = { isClearConfirmationOpen = true }
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
    InspectoScreen(onDismiss = {})
}
