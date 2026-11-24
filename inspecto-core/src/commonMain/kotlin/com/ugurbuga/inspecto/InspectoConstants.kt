package com.ugurbuga.inspecto

enum class InspectoFilterMode(val key: String) {
    AND("AND"),
    OR("OR");

    companion object {
        fun fromKey(key: String): InspectoFilterMode {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: AND
        }
    }
}

enum class InspectoQuickSearchQuery(val keyword: String) {
    SUCCESS("success"),
    ERROR("error"),
    ERR("err"),
    STATUS_2XX("2xx"),
    STATUS_3XX("3xx"),
    STATUS_4XX("4xx"),
    STATUS_5XX("5xx");

    companion object {
        fun match(query: String): InspectoQuickSearchQuery? {
            return entries.find { it.keyword.equals(query, ignoreCase = true) }
        }
    }
}

enum class InspectoStatusFilter(val value: String) {
    ALL("ALL"),
    SUCCESS("SUCCESS"),
    ERR("ERR"),
    STATUS_2XX("2XX"),
    STATUS_3XX("3XX"),
    STATUS_4XX("4XX"),
    STATUS_5XX("5XX");

    companion object {
        val keys = entries.map { it.value }
    }
}
