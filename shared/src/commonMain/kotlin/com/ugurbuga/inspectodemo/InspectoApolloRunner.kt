package io.github.ugurbuga.inspectodemo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.ObjectType
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import io.github.ugurbuga.inspecto.InspectoApolloInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCharactersQuery : Query<GetCharactersQuery.Data> {
    override fun id(): String = "GetCharacters"
    override fun document(): String = "query GetCharacters { characters(page: 1) { results { id name status species image } } }"
    override fun name(): String = "GetCharacters"

    override fun serializeVariables(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        withDefaultValues: Boolean
    ) {}

    override fun rootField(): CompiledField = CompiledField.Builder(
        name = "data",
        type = ObjectType.Builder(name = "Query").build()
    ).build()

    override fun adapter(): Adapter<Data> = object : Adapter<Data> {
        override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Data {
            val map = reader.readAny() as? Map<*, *>
            val charactersMap = map?.get("characters") ?: map
            return Data(charactersMap)
        }

        override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Data) {}
    }

    data class Data(val characters: Any?) : Query.Data
}

private fun JsonReader.readAny(): Any? {
    return when (peek()) {
        JsonReader.Token.BEGIN_OBJECT -> {
            beginObject()
            val map = mutableMapOf<String, Any?>()
            while (hasNext()) {
                map[nextName()] = readAny()
            }
            endObject()
            map
        }
        JsonReader.Token.BEGIN_ARRAY -> {
            beginArray()
            val list = mutableListOf<Any?>()
            while (hasNext()) {
                list.add(readAny())
            }
            endArray()
            list
        }
        JsonReader.Token.BOOLEAN -> nextBoolean()
        JsonReader.Token.NUMBER -> nextNumber()
        JsonReader.Token.STRING -> nextString()
        JsonReader.Token.NULL -> {
            nextNull()
            null
        }
        else -> {
            skipValue()
            null
        }
    }
}

object InspectoApolloRunner {
    private const val RICK_AND_MORTY_URL = "https://rickandmortyapi.com/graphql"

    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl(RICK_AND_MORTY_URL)
            .addInterceptor(InspectoApolloInterceptor(RICK_AND_MORTY_URL))
            .build()
    }

    suspend fun executeQuery(): String = withContext(Dispatchers.Default) {
        val response = apolloClient.query(GetCharactersQuery()).execute()
        if (response.hasErrors()) {
            "Errors: ${response.errors?.joinToString { it.message }}"
        } else {
            "Success! Received character list via ApolloClient."
        }
    }
}
