package com.ugurbuga.inspecto.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.InspectoLogEntry
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.avg_duration
import com.ugurbuga.inspecto.generated.resources.copy_button_description
import com.ugurbuga.inspecto.generated.resources.copy_curl
import com.ugurbuga.inspecto.generated.resources.detail_host
import com.ugurbuga.inspecto.generated.resources.detail_path
import com.ugurbuga.inspecto.generated.resources.detail_response_size
import com.ugurbuga.inspecto.generated.resources.detail_status_code
import com.ugurbuga.inspecto.generated.resources.detail_tag
import com.ugurbuga.inspecto.generated.resources.detail_timestamp
import com.ugurbuga.inspecto.generated.resources.detail_url
import com.ugurbuga.inspecto.generated.resources.overview_details
import com.ugurbuga.inspecto.generated.resources.ic_content_copy
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OverviewTabContent(
    entry: InspectoLogEntry,
    onCopyCurl: () -> Unit,
    onCopyUrl: () -> Unit,
    onCopyOverviewDetails: () -> Unit,
    onToast: (String) -> Unit = {}
) {
    @Suppress("DEPRECATION")
    val formattedTimestamp = runCatching {
        if (entry.timestampMs <= 0L) return@runCatching ""
        val instant = Instant.fromEpochMilliseconds(entry.timestampMs)
        val tz = runCatching { TimeZone.currentSystemDefault() }.getOrDefault(TimeZone.UTC)
        val dt = instant.toLocalDateTime(tz)
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = dt.monthNumber.toString().padStart(2, '0')
        val year = dt.year.toString()
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        val second = dt.second.toString().padStart(2, '0')
        "$day.$month.$year $hour:$minute:$second"
    }.getOrDefault(entry.timestampMs.toString())

    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.detail_url),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onCopyUrl,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_content_copy),
                                contentDescription = stringResource(Res.string.copy_button_description),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text(
                        text = entry.url,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overview Details Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.overview_details),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onCopyOverviewDetails,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_content_copy),
                                contentDescription = stringResource(Res.string.copy_button_description),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DetailRow(stringResource(Res.string.detail_host), entry.host, onToast)
                    DetailRow(stringResource(Res.string.detail_path), entry.path, onToast)
                    DetailRow(stringResource(Res.string.detail_status_code), "${entry.statusCode}", onToast)
                    DetailRow(stringResource(Res.string.avg_duration), "${entry.durationMs} ms", onToast)
                    DetailRow(stringResource(Res.string.detail_response_size), entry.formattedSize, onToast)
                    if (entry.tag.isNotBlank()) {
                        DetailRow(stringResource(Res.string.detail_tag), entry.tag, onToast)
                    }
                    DetailRow(stringResource(Res.string.detail_timestamp), formattedTimestamp, onToast)
                }
            }

            // cURL Button placed below Overview Details section as a Half-Width button
            Button(
                onClick = onCopyCurl,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_content_copy),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(Res.string.copy_curl),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(name = "Overview Tab Content - Light")
@Composable
internal fun OverviewTabContentPreview() {
    val sampleEntry = InspectoLogEntry(
        id = "1",
        url = "https://api.example.com/v1/users",
        method = "GET",
        statusCode = 200,
        durationMs = 120L,
        responseSizeByte = 1042L,
        tag = "Ktor"
    )
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            OverviewTabContent(
                entry = sampleEntry,
                onCopyCurl = {},
                onCopyUrl = {},
                onCopyOverviewDetails = {},
                onToast = {}
            )
        }
    }
}
