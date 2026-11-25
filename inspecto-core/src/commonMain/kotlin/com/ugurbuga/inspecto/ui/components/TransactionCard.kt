package com.ugurbuga.inspecto.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.InspectoLogEntry
import com.ugurbuga.inspecto.InspectoMethod
import com.ugurbuga.inspecto.InspectoTag
import com.ugurbuga.inspecto.ui.theme.InspectoColors
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant

@Composable
internal fun TransactionCard(
    entry: InspectoLogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val methodBg = when (entry.method.uppercase()) {
        InspectoMethod.GET -> InspectoColors.MethodGet
        InspectoMethod.POST -> InspectoColors.MethodPost
        InspectoMethod.PUT -> InspectoColors.MethodPut
        InspectoMethod.DELETE -> InspectoColors.MethodDelete
        InspectoMethod.PATCH -> InspectoColors.MethodPatch
        InspectoMethod.OPTIONS -> InspectoColors.MethodOptions
        InspectoMethod.HEAD -> InspectoColors.MethodHead
        else -> MaterialTheme.colorScheme.secondary
    }

    val statusBg = when {
        entry.statusCode in 200..299 -> InspectoColors.StatusSuccess2xx
        entry.statusCode in 300..399 -> InspectoColors.StatusRedirect3xx
        else -> InspectoColors.StatusError4xx5xx
    }

    val host = entry.host
    val path = entry.path

    val timeStr = runCatching {
        formatTimestamp(entry.timestampMs)
    }.getOrDefault("")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left: Combined Method + Status Code Badge Block with increased height, border & divider
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .width(62.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(methodBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.method,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    // Divider line separating Method box and Status Code box
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${entry.statusCode}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                }
            }

            // Center: Path, BaseUrl, Footer Metadata (Time, Duration, Size, Tag)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Row 1: Path
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.StartEllipsis
                )

                // Row 2: Host / BaseUrl (2-part display)
                if (host.isNotBlank()) {
                    Text(
                        text = "🔒 $host",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Row 3: Timestamp, Duration, Size, Tag (Single line, 11.sp font, tight spacing)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val metadataStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    if (timeStr.isNotBlank()) {
                        Text(
                            text = timeStr,
                            style = metadataStyle,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(text = "•", style = metadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${entry.durationMs}ms",
                        style = metadataStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(text = "•", style = metadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = entry.formattedSize,
                        style = metadataStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (entry.tag.isNotBlank() && entry.tag != InspectoTag.DEFAULT) {
                        Text(text = "•", style = metadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val tagColor = when {
                            entry.tag.contains(InspectoTag.KTOR, ignoreCase = true) -> InspectoColors.TagKtor
                            entry.tag.contains(InspectoTag.OKHTTP, ignoreCase = true) -> InspectoColors.TagOkHttp
                            entry.tag.contains(InspectoTag.GRAPHQL, ignoreCase = true) -> InspectoColors.TagGraphQL
                            entry.tag.contains("NSURLSession", ignoreCase = true) -> InspectoColors.TagNSURLSession
                            entry.tag.equals(InspectoTag.MANUAL, ignoreCase = true) -> InspectoColors.TagManual
                            else -> InspectoColors.TagNSURLSession
                        }
                        Text(
                            text = entry.tag,
                            style = metadataStyle,
                            fontWeight = FontWeight.Bold,
                            color = tagColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestampMs: Long): String {
    val dateTime = Instant
        .fromEpochMilliseconds(timestampMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return "${dateTime.hour.toString().padStart(2, '0')}:" +
            "${dateTime.minute.toString().padStart(2, '0')}:" +
            dateTime.second.toString().padStart(2, '0')
}

@Preview(name = "GET 200 (Success)")
@Composable
internal fun TransactionCardGet200Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "1",
                    url = "https://api.example.com/v1/users/v1/users/v1/users/v1/users/v1/users",
                    method = InspectoMethod.GET,
                    statusCode = 200,
                    durationMs = 120L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 1042L,
                    tag = InspectoTag.KTOR
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "POST 201 (Success)")
@Composable
internal fun TransactionCardPost201Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "2",
                    url = "https://api.example.com/v1/posts",
                    method = InspectoMethod.POST,
                    statusCode = 201,
                    durationMs = 240L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 512L,
                    tag = InspectoTag.OKHTTP
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "PUT 304 (Redirect)")
@Composable
internal fun TransactionCardPut304Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "3",
                    url = "https://api.example.com/v1/profile",
                    method = InspectoMethod.PUT,
                    statusCode = 304,
                    durationMs = 85L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 0L,
                    tag = InspectoTag.GRAPHQL
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "DELETE 400 (Error)")
@Composable
internal fun TransactionCardDelete400Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "4",
                    url = "https://api.example.com/v1/items/99",
                    method = InspectoMethod.DELETE,
                    statusCode = 400,
                    durationMs = 95L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 128L,
                    tag = InspectoTag.MANUAL
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "PATCH 500 (Error)")
@Composable
internal fun TransactionCardPatch500Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "5",
                    url = "https://api.example.com/v1/settings",
                    method = InspectoMethod.PATCH,
                    statusCode = 500,
                    durationMs = 450L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 256L,
                    tag = InspectoTag.KTOR
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "OPTIONS 401 (Error)")
@Composable
internal fun TransactionCardOptions401Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "6",
                    url = "https://api.example.com/v1/auth",
                    method = InspectoMethod.OPTIONS,
                    statusCode = 401,
                    durationMs = 50L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 64L,
                    tag = InspectoTag.OKHTTP
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "HEAD 404 (Error)")
@Composable
internal fun TransactionCardHead404Preview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(
                entry = InspectoLogEntry(
                    id = "7",
                    url = "https://api.example.com/v1/missing",
                    method = InspectoMethod.HEAD,
                    statusCode = 404,
                    durationMs = 30L,
                    timestampMs = 1710000000000L,
                    responseSizeByte = 0L,
                    tag = InspectoTag.MANUAL
                ),
                onClick = {}
            )
        }
    }
}
