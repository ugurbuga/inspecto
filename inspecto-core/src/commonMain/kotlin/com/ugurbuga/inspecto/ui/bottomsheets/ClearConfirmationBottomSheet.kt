package com.ugurbuga.inspecto.ui.bottomsheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.clear_all_logs
import com.ugurbuga.inspecto.generated.resources.clear_logs_confirmation_message
import com.ugurbuga.inspecto.generated.resources.cancel
import com.ugurbuga.inspecto.generated.resources.clear
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import com.ugurbuga.inspecto.ui.theme.InspectoTheme
import com.ugurbuga.inspecto.ui.theme.LocalInspectoFontFamily
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClearConfirmationBottomSheet(
    isDarkTheme: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fontFamily = LocalInspectoFontFamily.current
    val clearButtonColor = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFE11D48)

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
                    .padding(start = 24.dp, end = 24.dp, top = 2.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.clear_all_logs),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.clear_logs_confirmation_message),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(Res.string.cancel),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = fontFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = clearButtonColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(Res.string.clear),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = fontFamily),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Clear Confirmation - Light")
@Composable
internal fun ClearConfirmationBottomSheetLightPreview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            ClearConfirmationBottomSheet(
                isDarkTheme = false,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Clear Confirmation - Dark")
@Composable
internal fun ClearConfirmationBottomSheetDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            ClearConfirmationBottomSheet(
                isDarkTheme = true,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
