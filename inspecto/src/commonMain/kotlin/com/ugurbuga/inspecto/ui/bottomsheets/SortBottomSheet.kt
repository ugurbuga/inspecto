package com.ugurbuga.inspecto.ui.bottomsheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.InspectoSortOption
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.apply_sort
import com.ugurbuga.inspecto.generated.resources.sort_requests_title
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import com.ugurbuga.inspecto.ui.theme.InspectoTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SortBottomSheet(
    sortOption: InspectoSortOption,
    isDarkTheme: Boolean,
    onApply: (InspectoSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSort by remember { mutableStateOf(sortOption) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    .padding(start = 24.dp, end = 24.dp, top = 2.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(Res.string.sort_requests_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(4.dp))

                InspectoSortOption.entries.forEach { option ->
                    val selected = tempSort == option
                    Surface(
                        onClick = { tempSort = option },
                        shape = RoundedCornerShape(4.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = selected,
                                onClick = { tempSort = option },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onApply(tempSort)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(stringResource(Res.string.apply_sort), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(name = "Sort Bottom Sheet - Light")
@Composable
internal fun SortBottomSheetPreview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            SortBottomSheet(
                sortOption = InspectoSortOption.DATE_DESC,
                isDarkTheme = false,
                onApply = {},
                onDismiss = {}
            )
        }
    }
}
