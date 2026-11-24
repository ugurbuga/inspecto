package com.ugurbuga.inspecto.ui.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.InspectoFilterMode
import com.ugurbuga.inspecto.InspectoStatusFilter
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.apply_filters
import com.ugurbuga.inspecto.generated.resources.combination_mode
import com.ugurbuga.inspecto.generated.resources.filter_requests_title
import com.ugurbuga.inspecto.generated.resources.mode_and_desc
import com.ugurbuga.inspecto.generated.resources.mode_or_desc
import com.ugurbuga.inspecto.generated.resources.reset
import com.ugurbuga.inspecto.generated.resources.select_chips
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import com.ugurbuga.inspecto.ui.theme.InspectoTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FilterBottomSheet(
    selectedFilters: Set<String>,
    filterMode: String,
    isDarkTheme: Boolean,
    onApply: (selectedFilters: Set<String>, filterMode: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(selectedFilters) }
    var tempMode by remember { mutableStateOf(filterMode) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val logs by Inspecto.logs.collectAsStateWithLifecycle()
    val dynamicMethods = remember(logs) { logs.map { it.method.uppercase() }.distinct().sorted() }
    val dynamicTags = remember(logs) { logs.map { it.tag }.filter { it.isNotBlank() && it != "HTTP" }.distinct().sorted() }

    val hasSuccess = remember(logs) { logs.any { it.isSuccess } }
    val hasError = remember(logs) { logs.any { it.isError } }

    val availableFilters = remember(dynamicMethods, dynamicTags, hasSuccess, hasError) {
        mutableListOf(InspectoStatusFilter.ALL.value).apply {
            if (hasSuccess) add(InspectoStatusFilter.SUCCESS.value)
            if (hasError) add(InspectoStatusFilter.ERR.value)
            addAll(listOf(InspectoStatusFilter.STATUS_2XX.value, InspectoStatusFilter.STATUS_3XX.value, InspectoStatusFilter.STATUS_4XX.value, InspectoStatusFilter.STATUS_5XX.value))
            addAll(dynamicMethods)
            addAll(dynamicTags)
        }.distinct()
    }

    InspectoTheme(isDarkTheme = isDarkTheme) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 2.dp, bottom = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.filter_requests_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { tempFilters = emptySet() }) {
                        Text(stringResource(Res.string.reset), style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Combination Mode Card
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(Res.string.combination_mode),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InspectoFilterMode.entries.forEach { mode ->
                            val selected = tempMode == mode.key
                            Surface(
                                onClick = { tempMode = mode.key },
                                shape = RoundedCornerShape(4.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (mode == InspectoFilterMode.AND) stringResource(Res.string.mode_and_desc) else stringResource(Res.string.mode_or_desc),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Chips Section (Static + Dynamic Methods & Tags)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(Res.string.select_chips),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableFilters.forEach { filter ->
                            val isSelected = if (filter == "ALL") tempFilters.isEmpty() || "ALL" in tempFilters else filter in tempFilters
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    tempFilters = if (filter == "ALL") {
                                        emptySet()
                                    } else {
                                        val mutable = tempFilters.toMutableSet()
                                        mutable.remove("ALL")
                                        if (filter in mutable) mutable.remove(filter) else mutable.add(filter)
                                        mutable
                                    }
                                },
                                label = {
                                    Text(
                                        filter,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onApply(tempFilters, tempMode)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(stringResource(Res.string.apply_filters), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Preview(name = "Filter Bottom Sheet - Light")
@Composable
internal fun FilterBottomSheetPreview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            FilterBottomSheet(
                selectedFilters = setOf("GET", "2XX"),
                filterMode = "AND",
                isDarkTheme = false,
                onApply = { _, _ -> },
                onDismiss = {}
            )
        }
    }
}
