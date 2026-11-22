package com.ugurbuga.inspecto

enum class InspectoFilterMode(val key: String) {
    AND("AND"),
    OR("OR")
}

object InspectoConstants {
    const val FILTER_ALL = "ALL"
    const val FILTER_2XX = "2XX"
    const val FILTER_3XX = "3XX"
    const val FILTER_4XX = "4XX"
    const val FILTER_5XX = "5XX"

    const val MODE_AND = "AND"
    const val MODE_OR = "OR"
}
