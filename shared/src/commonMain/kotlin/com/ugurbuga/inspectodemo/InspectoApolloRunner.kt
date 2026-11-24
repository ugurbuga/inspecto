package com.ugurbuga.inspectodemo

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.ObjectType
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.apollo.exception.ApolloHttpException
import com.ugurbuga.inspecto.InspectoApolloInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class GetCharactersQuery(val page: Int = 1) : Query<GetCharactersQuery.Data> {
    override fun id(): String = "GetCharacters"
    override fun document(): String =
        "query GetCharacters(\$page: Int) { characters(page: \$page) { info { pages count next } results { id name status species image } } }"

    override fun name(): String = "GetCharacters"

    override fun serializeVariables(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        withDefaultValues: Boolean
    ) {
        writer.name("page")
        writer.value(page)
    }

    override fun rootField(): CompiledField = CompiledField.Builder(
        name = "data",
        type = ObjectType.Builder(name = "Query").build()
    ).build()

    override fun adapter(): Adapter<Data> = object : Adapter<Data> {
        override fun fromJson(
            reader: JsonReader,
            customScalarAdapters: CustomScalarAdapters
        ): Data {
            val map = readJsonObject(reader)
            val charactersMap = map["characters"] ?: map
            return Data(charactersMap)
        }

        override fun toJson(
            writer: JsonWriter,
            customScalarAdapters: CustomScalarAdapters,
            value: Data
        ) {
            writer.name("characters")
            writeAnyJson(writer, value.characters)
        }
    }

    data class Data(val characters: Any?) : Query.Data
}

class GetCharacterQuery(val characterId: String = "1") : Query<GetCharacterQuery.Data> {
    override fun id(): String = "GetCharacter"
    override fun document(): String =
        "query GetCharacter(\$characterId: ID!) { character(id: \$characterId) { id name status species image gender origin { name } location { name } } }"

    override fun name(): String = "GetCharacter"

    override fun serializeVariables(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        withDefaultValues: Boolean
    ) {
        writer.name("characterId")
        writer.value(characterId)
    }

    override fun rootField(): CompiledField = CompiledField.Builder(
        name = "data",
        type = ObjectType.Builder(name = "Query").build()
    ).build()

    override fun adapter(): Adapter<Data> = object : Adapter<Data> {
        override fun fromJson(
            reader: JsonReader,
            customScalarAdapters: CustomScalarAdapters
        ): Data {
            val map = readJsonObject(reader)
            val characterMap = map["character"] ?: map
            return Data(characterMap)
        }

        override fun toJson(
            writer: JsonWriter,
            customScalarAdapters: CustomScalarAdapters,
            value: Data
        ) {
            writer.name("character")
            writeAnyJson(writer, value.character)
        }
    }

    data class Data(val character: Any?) : Query.Data
}

class GetEpisodesQuery(val page: Int = 1) : Query<GetEpisodesQuery.Data> {
    override fun id(): String = "GetEpisodes"
    override fun document(): String =
        "query GetEpisodes(\$page: Int) { episodes(page: \$page) { info { pages count next } results { id name episode air_date } } }"

    override fun name(): String = "GetEpisodes"

    override fun serializeVariables(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        withDefaultValues: Boolean
    ) {
        writer.name("page")
        writer.value(page)
    }

    override fun rootField(): CompiledField = CompiledField.Builder(
        name = "data",
        type = ObjectType.Builder(name = "Query").build()
    ).build()

    override fun adapter(): Adapter<Data> = object : Adapter<Data> {
        override fun fromJson(
            reader: JsonReader,
            customScalarAdapters: CustomScalarAdapters
        ): Data {
            val map = readJsonObject(reader)
            val episodesMap = map["episodes"] ?: map
            return Data(episodesMap)
        }

        override fun toJson(
            writer: JsonWriter,
            customScalarAdapters: CustomScalarAdapters,
            value: Data
        ) {
            writer.name("episodes")
            writeAnyJson(writer, value.episodes)
        }
    }

    data class Data(val episodes: Any?) : Query.Data
}

private fun writeAnyJson(writer: JsonWriter, value: Any?) {
    when (value) {
        null -> writer.nullValue()
        is String -> writer.value(value)
        is Boolean -> writer.value(value)
        is Int -> writer.value(value)
        is Long -> writer.value(value)
        is Double -> writer.value(value)
        is Number -> writer.value(value.toDouble())
        is Map<*, *> -> {
            writer.beginObject()
            value.forEach { (k, v) ->
                if (k != null) {
                    writer.name(k.toString())
                    writeAnyJson(writer, v)
                }
            }
            writer.endObject()
        }

        is List<*> -> {
            writer.beginArray()
            value.forEach { writeAnyJson(writer, it) }
            writer.endArray()
        }

        else -> writer.value(value.toString())
    }
}

private fun readJsonObject(reader: JsonReader): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    try {
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            map[name] = readJsonValue(reader)
        }
        reader.endObject()
    } catch (_: Throwable) {
    }
    return map
}

private fun readJsonValue(reader: JsonReader): Any? {
    return try {
        when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NUMBER -> reader.nextNumber()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NULL -> {
                reader.nextNull()
                null
            }

            JsonReader.Token.BEGIN_OBJECT -> readJsonObject(reader)
            JsonReader.Token.BEGIN_ARRAY -> readJsonArray(reader)
            else -> {
                reader.skipValue()
                null
            }
        }
    } catch (_: Throwable) {
        null
    }
}

private fun readJsonArray(reader: JsonReader): List<Any?> {
    val list = mutableListOf<Any?>()
    try {
        reader.beginArray()
        while (reader.hasNext()) {
            list.add(readJsonValue(reader))
        }
        reader.endArray()
    } catch (_: Throwable) {
    }
    return list
}

object InspectoApolloRunner {
    private const val RICK_AND_MORTY_URL = "https://rickandmortyapi.com/graphql"
    private const val MAX_RATE_LIMIT_RETRIES = 3
    private const val MAX_RETRY_DELAY_MS = 10_000L
    private var queryCount = 0

    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl(RICK_AND_MORTY_URL)
            .addInterceptor(InspectoApolloInterceptor(RICK_AND_MORTY_URL))
            .build()
    }

    private suspend fun <D : Operation.Data> ApolloCall<D>.executeRetryingRateLimit(): ApolloResponse<D> {
        var attempt = 0
        while (true) {
            val response = execute()
            val rateLimited = response.exception?.let { findRateLimit(it) } ?: return response
            if (attempt == MAX_RATE_LIMIT_RETRIES) return response
            val retryAfterSeconds = rateLimited.headers
                .firstOrNull { it.name.equals("Retry-After", ignoreCase = true) }
                ?.value?.trim()?.toLongOrNull()
            delay(
                retryAfterSeconds?.let { (it * 1000).coerceIn(500, MAX_RETRY_DELAY_MS) }
                    ?: (1000L shl attempt)
            )
            attempt++
        }
    }

    private fun findRateLimit(throwable: Throwable): ApolloHttpException? {
        if (throwable is ApolloHttpException && throwable.statusCode == 429) return throwable
        throwable.suppressedExceptions.forEach { suppressed -> findRateLimit(suppressed)?.let { return it } }
        return throwable.cause?.let { findRateLimit(it) }
    }

    suspend fun getCharacters(page: Int = 1): String = withContext(Dispatchers.Default) {
        val query = GetCharactersQuery(page)
        val response = apolloClient.query(query).executeRetryingRateLimit()
        if (response.hasErrors()) {
            "Errors: ${response.errors?.joinToString { it.message }}"
        } else {
            "Success! Received GetCharactersQuery (Page $page) via ApolloClient."
        }
    }

    suspend fun getEpisodes(page: Int = 1): String = withContext(Dispatchers.Default) {
        val query = GetEpisodesQuery(page)
        val response = apolloClient.query(query).executeRetryingRateLimit()
        if (response.hasErrors()) {
            "Errors: ${response.errors?.joinToString { it.message }}"
        } else {
            "Success! Received GetEpisodesQuery (Page $page) via ApolloClient."
        }
    }

    suspend fun executeQuery(): String = withContext(Dispatchers.Default) {
        if (queryCount++ % 2 == 0) {
            getCharacters(1)
        } else {
            getEpisodes(1)
        }
    }
}
