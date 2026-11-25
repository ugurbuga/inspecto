package com.ugurbuga.inspecto.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.avg_duration
import com.ugurbuga.inspecto.generated.resources.errors
import com.ugurbuga.inspecto.generated.resources.success_2xx
import com.ugurbuga.inspecto.generated.resources.total_requests
import com.ugurbuga.inspecto.generated.resources.ic_dark_mode
import com.ugurbuga.inspecto.generated.resources.ic_delete
import com.ugurbuga.inspecto.generated.resources.ic_filter_list
import com.ugurbuga.inspecto.generated.resources.ic_light_mode
import com.ugurbuga.inspecto.generated.resources.ic_swap_vert
import com.ugurbuga.inspecto.generated.resources.ic_system_mode
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HeaderStatsTable(
    totalCount: Int,
    successCount: Int,
    errorCount: Int,
    avgLatency: Long,
    isDarkTheme: Boolean,
    themeMode: Boolean?,
    isFilterActive: Boolean = false,
    isSearchActive: Boolean = false,
    onOpenFilter: () -> Unit,
    onOpenSort: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlightActive = isFilterActive || isSearchActive

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left: Table layout with 4 colored stat rows
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(
                1.dp,
                if (isHighlightActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(Res.string.total_requests),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isHighlightActive) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                    Text(
                        "$totalCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(Res.string.success_2xx),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                        if (isHighlightActive) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                    Text(
                        "$successCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(Res.string.errors),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF4444)
                        )
                        if (isHighlightActive) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                    Text(
                        "$errorCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(Res.string.avg_duration),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                        if (isHighlightActive) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                    Text(
                        "${avgLatency}ms",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                }
            }
        }

        // Right: 2x2 Grid Square Action Icons
        val iconButtonBg = if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.primaryContainer
        val filterTint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7) // Blue
        val sortTint = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF059669) // Emerald / Teal
        val themeTint = when (themeMode) {
            null -> Color.Unspecified
            else -> if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706)
        }
        val clearTint = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFE11D48) // Rose / Red

        val themeIcon = when (themeMode) {
            null -> Res.drawable.ic_system_mode
            false -> Res.drawable.ic_light_mode
            true -> Res.drawable.ic_dark_mode
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Row 1: Theme & Clear (Top Row)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = onToggleTheme,
                    shape = RoundedCornerShape(4.dp),
                    color = iconButtonBg,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(themeIcon),
                            contentDescription = "Toggle Theme",
                            tint = themeTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    onClick = onOpenClear,
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDarkTheme) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, clearTint.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = "Clear Logs",
                            tint = clearTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            // Row 2: Filter & Sort (Bottom Row)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = onOpenFilter,
                    shape = RoundedCornerShape(4.dp),
                    color = iconButtonBg,
                    border = BorderStroke(
                        1.dp,
                        if (isFilterActive) filterTint.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_filter_list),
                            contentDescription = "Filter",
                            tint = filterTint,
                            modifier = Modifier.size(18.dp)
                        )
                        if (isFilterActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(3.dp)
                                    .size(6.dp)
                                    .background(filterTint, CircleShape)
                            )
                        }
                    }
                }
                Surface(
                    onClick = onOpenSort,
                    shape = RoundedCornerShape(4.dp),
                    color = iconButtonBg,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_swap_vert),
                            contentDescription = "Sort",
                            tint = sortTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Header Stats Table - Light")
@Composable
internal fun HeaderStatsTablePreview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            HeaderStatsTable(
                totalCount = 15,
                successCount = 12,
                errorCount = 3,
                avgLatency = 145L,
                isDarkTheme = false,
                themeMode = null,
                isFilterActive = true,
                isSearchActive = false,
                onOpenFilter = {},
                onOpenSort = {},
                onToggleTheme = {},
                onOpenClear = {}
            )
        }
    }
}
