package com.ugurbuga.inspecto.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.InspectoJsonSyntaxHighlighter
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.empty_body
import com.ugurbuga.inspecto.generated.resources.formatting_payload
import com.ugurbuga.inspecto.generated.resources.ic_add
import com.ugurbuga.inspecto.generated.resources.ic_close
import com.ugurbuga.inspecto.generated.resources.ic_content_copy
import com.ugurbuga.inspecto.generated.resources.ic_keyboard_arrow_down
import com.ugurbuga.inspecto.generated.resources.ic_keyboard_arrow_up
import com.ugurbuga.inspecto.generated.resources.ic_remove
import com.ugurbuga.inspecto.generated.resources.ic_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.ugurbuga.inspecto.ui.components.noRippleClickable
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun HeadersAndBodyTabContent(
    sectionTitle: String,
    queryParams: Map<String, String>,
    headers: Map<String, String>,
    rawBody: String,
    isDarkTheme: Boolean,
    onCopyHeaders: () -> Unit,
    onCopyQueryParams: () -> Unit,
    onCopyBody: () -> Unit,
    onToast: (String) -> Unit
) {
    var isHeadersExpanded by remember { mutableStateOf(false) }
    var isQueryParamsExpanded by remember { mutableStateOf(false) }
    var formattedLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(true) }

    // Font size zoom state (Request 1)
    var bodyFontSize by remember { mutableStateOf(13.sp) }

    // In-page search state (Request 2)
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentMatchIndex by remember { mutableStateOf(0) }

    LaunchedEffect(rawBody) {
        isProcessing = true
        withContext(Dispatchers.Default) {
            formattedLines = Inspecto.formatJson(rawBody).lines()
            isProcessing = false
        }
    }

    val matchingLineIndices = remember(formattedLines, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            formattedLines.indices.filter { idx ->
                formattedLines[idx].contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val bodyListState = rememberLazyListState()

    LaunchedEffect(searchQuery) {
        currentMatchIndex = 0
    }

    LaunchedEffect(isSearchOpen) {
        if (isSearchOpen) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(currentMatchIndex, matchingLineIndices) {
        val targetIndex = matchingLineIndices.getOrNull(currentMatchIndex)
        if (targetIndex != null) {
            bodyListState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (queryParams.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { isQueryParamsExpanded = !isQueryParamsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Query Parameters (${queryParams.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onCopyQueryParams,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_content_copy),
                                contentDescription = "Copy Query Parameters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(if (isQueryParamsExpanded) Res.drawable.ic_keyboard_arrow_up else Res.drawable.ic_keyboard_arrow_down),
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isQueryParamsExpanded) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            queryParams.forEach { (key, value) ->
                                DetailRow(key, value, onToast = onToast)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }

        if (headers.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { isHeadersExpanded = !isHeadersExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$sectionTitle Headers (${headers.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onCopyHeaders,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_content_copy),
                                contentDescription = "Copy Headers",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(if (isHeadersExpanded) Res.drawable.ic_keyboard_arrow_up else Res.drawable.ic_keyboard_arrow_down),
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isHeadersExpanded) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            headers.forEach { (key, value) ->
                                DetailRow(key, value, onToast = onToast)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$sectionTitle Body",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search toggle button
                    IconButton(
                        onClick = { isSearchOpen = !isSearchOpen },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = "Search in body",
                            tint = if (isSearchOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Font Size - Button
                    IconButton(
                        onClick = {
                            bodyFontSize = ((bodyFontSize.value - 1f).coerceAtLeast(10f)).sp
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_remove),
                            contentDescription = "Decrease Font Size",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = bodyFontSize.value.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Font Size + Button
                    IconButton(
                        onClick = {
                            bodyFontSize = ((bodyFontSize.value + 1f).coerceAtMost(24f)).sp
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = "Increase Font Size",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (rawBody.isNotBlank()) {
                        IconButton(onClick = onCopyBody, modifier = Modifier.size(28.dp)) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_content_copy),
                                contentDescription = "Copy $sectionTitle Body",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // In-Page Search Toolbar
            if (isSearchOpen) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search in text...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(4.dp)
                        )

                        val matchCount = matchingLineIndices.size
                        val matchLabel =
                            if (matchCount > 0) "${currentMatchIndex + 1}/$matchCount" else "0/0"

                        Text(
                            text = matchLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Up Arrow
                        IconButton(
                            onClick = {
                                if (matchCount > 0) {
                                    currentMatchIndex =
                                        (currentMatchIndex - 1 + matchCount) % matchCount
                                }
                            },
                            enabled = matchCount > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_keyboard_arrow_up),
                                contentDescription = "Previous Match",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Down Arrow
                        IconButton(
                            onClick = {
                                if (matchCount > 0) {
                                    currentMatchIndex = (currentMatchIndex + 1) % matchCount
                                }
                            },
                            enabled = matchCount > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_keyboard_arrow_down),
                                contentDescription = "Next Match",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close Search
                        IconButton(
                            onClick = {
                                isSearchOpen = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = "Close Search",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    when {
                        rawBody.isBlank() -> {
                            Text(
                                text = stringResource(Res.string.empty_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        isProcessing -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(Res.string.formatting_payload),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            SelectionContainer {
                                LazyColumn(
                                    state = bodyListState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 500.dp)
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
                                    items(
                                        count = formattedLines.size,
                                        key = { index -> index }
                                    ) { index ->
                                        val line = formattedLines[index]
                                        val highlightedLine = remember(line, isDarkTheme) {
                                            InspectoJsonSyntaxHighlighter.highlightJsonLine(
                                                line,
                                                isDarkTheme
                                            )
                                        }

                                        val matchCount = matchingLineIndices.size
                                        val isMatchedLine = index in matchingLineIndices
                                        val isActiveMatch =
                                            matchCount > 0 && matchingLineIndices.getOrNull(
                                                currentMatchIndex
                                            ) == index

                                        val lineBg = when {
                                            isActiveMatch -> if (isDarkTheme) Color(0xFF78350F) else Color(
                                                0xFFFEF08A
                                            )

                                            isMatchedLine -> if (isDarkTheme) Color(0xFF334155) else Color(
                                                0xFFFEF9C3
                                            )

                                            else -> Color.Transparent
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(lineBg, RoundedCornerShape(2.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = highlightedLine,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = bodyFontSize
                                                ),
                                                lineHeight = (bodyFontSize.value * 1.4f).sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Headers And Body Tab Content - Light")
@Composable
internal fun HeadersAndBodyTabContentPreview() {
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            HeadersAndBodyTabContent(
                sectionTitle = "Response",
                queryParams = emptyMap(),
                headers = mapOf("Content-Type" to "application/json"),
                rawBody = "[\n  {\n    \"id\": 1,\n    \"name\": \"John Doe\"\n  }\n]",
                isDarkTheme = false,
                onCopyHeaders = {},
                onCopyQueryParams = {},
                onCopyBody = {},
                onToast = {}
            )
        }
    }
}
