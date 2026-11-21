package io.github.ugurbuga.inspecto

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

internal object InspectoJsonSyntaxHighlighter {

    val KeyColor = Color(0xFF0284C7)        // Cyan / Primary Blue
    val StringColor = Color(0xFF10B981)     // Green / Emerald
    val NumberColor = Color(0xFFF59E0B)     // Orange / Amber
    val BooleanColor = Color(0xFF8B5CF6)    // Purple / Violet
    val NullColor = Color(0xFFEF4444)       // Red / Crimson
    val PunctuationColor = Color(0xFF94A3B8)// Neutral Muted Gray

    fun highlightJson(
        jsonString: String,
        isDarkTheme: Boolean = true
    ): AnnotatedString {
        if (jsonString.isBlank()) return AnnotatedString("")
        return highlightJsonLine(jsonString, isDarkTheme)
    }

    fun highlightJsonLine(
        lineString: String,
        isDarkTheme: Boolean = true
    ): AnnotatedString {
        if (lineString.isEmpty()) return AnnotatedString("")

        val keyColor = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
        val stringColor = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF059669)
        val numberColor = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706)
        val booleanColor = if (isDarkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED)
        val nullColor = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFDC2626)
        val punctuationColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

        return buildAnnotatedString {
            val length = lineString.length
            var i = 0

            while (i < length) {
                val char = lineString[i]

                when {
                    // String or Key
                    char == '"' -> {
                        val start = i
                        i++
                        while (i < length && (lineString[i] != '"' || lineString[i - 1] == '\\')) {
                            i++
                        }
                        if (i < length) i++ // include closing quote

                        val strToken = lineString.substring(start, i)

                        // Check if this string is a JSON key (followed by optional whitespace and ':')
                        var lookAhead = i
                        while (lookAhead < length && lineString[lookAhead].isWhitespace()) {
                            lookAhead++
                        }
                        val isKey = lookAhead < length && lineString[lookAhead] == ':'

                        if (isKey) {
                            append(
                                AnnotatedString(
                                    text = strToken,
                                    spanStyle = SpanStyle(color = keyColor, fontWeight = FontWeight.Bold)
                                )
                            )
                        } else {
                            append(
                                AnnotatedString(
                                    text = strToken,
                                    spanStyle = SpanStyle(color = stringColor)
                                )
                            )
                        }
                    }

                    // Number (digits, minus, dot)
                    char.isDigit() || (char == '-' && i + 1 < length && lineString[i + 1].isDigit()) -> {
                        val start = i
                        if (char == '-') i++
                        while (i < length && (lineString[i].isDigit() || lineString[i] == '.' || lineString[i] == 'e' || lineString[i] == 'E' || lineString[i] == '+' || lineString[i] == '-')) {
                            i++
                        }
                        val numToken = lineString.substring(start, i)
                        append(
                            AnnotatedString(
                                text = numToken,
                                spanStyle = SpanStyle(color = numberColor, fontWeight = FontWeight.Bold)
                            )
                        )
                    }

                    // Boolean true/false
                    lineString.startsWith("true", i) -> {
                        append(
                            AnnotatedString(
                                text = "true",
                                spanStyle = SpanStyle(color = booleanColor, fontWeight = FontWeight.Bold)
                            )
                        )
                        i += 4
                    }
                    lineString.startsWith("false", i) -> {
                        append(
                            AnnotatedString(
                                text = "false",
                                spanStyle = SpanStyle(color = booleanColor, fontWeight = FontWeight.Bold)
                            )
                        )
                        i += 5
                    }

                    // Null
                    lineString.startsWith("null", i) -> {
                        append(
                            AnnotatedString(
                                text = "null",
                                spanStyle = SpanStyle(color = nullColor, fontWeight = FontWeight.Bold)
                            )
                        )
                        i += 4
                    }

                    // Punctuation braces and brackets
                    char == '{' || char == '}' || char == '[' || char == ']' || char == ':' || char == ',' -> {
                        append(
                            AnnotatedString(
                                text = char.toString(),
                                spanStyle = SpanStyle(color = punctuationColor, fontWeight = FontWeight.Medium)
                            )
                        )
                        i++
                    }

                    // Whitespace or other text
                    else -> {
                        append(char.toString())
                        i++
                    }
                }
            }
        }
    }
}
