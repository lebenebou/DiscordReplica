
package com.example.androidstudioproject

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class MongoClient {

    private val apiKey = GlobalVars.MongoAPIKey
    private val httpClient = OkHttpClient()
    private suspend fun makeAPIRequest(endpoint: String, headers: JSONObject, body: JSONObject) : JSONObject {

        val mediaType = "application/json".toMediaType()

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(body.toString().toByteArray(StandardCharsets.UTF_8).toRequestBody(mediaType))

        // add headers from JSONObject
        headers.keys().forEach { key ->
            requestBuilder.addHeader(key, headers.getString(key))
        }

        val request = requestBuilder.build()
        val response = httpClient.newCall(request).execute()

        val responseBody = response.body?.string() ?: ""
        return JSONObject(responseBody)
    }

    suspend fun insertOne(collectionName: String, document: JSONObject) : JSONObject {

        return makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/insertOne",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("apiKey", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("document", document)
        )
    }
    suspend fun findOne(collectionName: String, filter: JSONObject) : JSONObject {

        // returns empty json if no matches
        val jsonResponse = makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/findOne",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("Access-Control-Request-Headers", "*")
                .put("api-key", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)
        )

        if(jsonResponse.toString().length < 18) return JSONObject()
        return jsonResponse.getJSONObject("document")
    }
    suspend fun findMultiple(collectionName: String, filter: JSONObject) : JSONArray {

        // returns empty json if no matches
        val jsonResponse = makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/find",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("Access-Control-Request-Headers", "*")
                .put("api-key", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)
        )

        if(jsonResponse.toString().length < 18) return JSONArray()
        return jsonResponse.getJSONArray("documents")
    }
    suspend fun deleteOne(collectionName: String, filter: JSONObject) : JSONObject {

        return makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/deleteOne",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("apiKey", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)
        )
    }
    suspend fun updateOne(collectionName: String, filter: JSONObject, updates: JSONObject) : JSONObject {

        return makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/updateOne",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("apiKey", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)
                .put("update", JSONObject()
                    .put("\$set", updates)
                )
        )
    }

    suspend fun addToArray(collectionName: String, filter: JSONObject, arrayName: String, element: Any) :JSONObject {

        return makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/updateOne",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("apiKey", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)
                .put("update", JSONObject()
                    .put("\$addToSet", JSONObject().put(arrayName, element))
                )
        )
    }
    suspend fun removeFromArray(collectionName: String, filter: JSONObject, arrayName: String, element: Any) :JSONObject {

        return makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/updateOne",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("apiKey", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)
                .put("update", JSONObject()
                    .put("\$pull", JSONObject().put(arrayName, element))
                )
        )
    }

    suspend fun addToMessages(roomCode: String, newMessage: JSONObject) :JSONObject {

        return addToArray("Rooms", JSONObject().put("code", roomCode),"messages", newMessage)
    }
    suspend fun addToActiveUsers(roomCode: String, username: String) : JSONObject {

        return addToArray("Rooms", JSONObject().put("code", roomCode), "active_users", username)
    }
    suspend fun removeFromActiveUsers(roomCode: String, username: String) : JSONObject {

        return removeFromArray("Rooms", JSONObject().put("code", roomCode), "active_users", username)
    }
    suspend fun getSearchResults(collectionName: String, attributeName: String, searchQuery: String) : JSONArray {

        val filter = JSONObject().apply {
            put(attributeName, JSONObject().put("\$regex", "(?i).*$searchQuery.*"))
            // this is a regex for anything that has the query as a substring
        }

        return makeAPIRequest(

            endpoint = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/find",

            headers = JSONObject()
                .put("content-type", "application/json")
                .put("apiKey", apiKey),

            body = JSONObject()
                .put("dataSource", "Cluster1")
                .put("database", "DiscordReplica")
                .put("collection", collectionName)
                .put("filter", filter)

        ).getJSONArray("documents")
    }

    fun encrypt(word: String): String {

        val bytes = word.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
    fun decrypt(encodedStr: String): String {

        val decoded = Base64.decode(encodedStr, Base64.DEFAULT)
        return String(decoded)
    }

    // Voice record encoding
    fun compressList(list: MutableList<Short>): String {
        // Convert the list to a delta-encoded byte array
        val deltaEncoded = deltaEncode(list)
        // Compress the byte array using GZIP
        val compressed = compress(deltaEncoded)
        // Encode the compressed byte array as a base64 string
        return Base64.encodeToString(compressed, Base64.DEFAULT)
        // Truncate the string to a maximum length of 1.2 million characters
    }

    private fun compress(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzip -> gzip.write(data) }
        return baos.toByteArray()
    }

    private fun deltaEncode(arr: MutableList<Short>): ByteArray {
        val result = IntArray(arr.size)
        var prev: Short = 0
        for (i in arr.indices) {
            result[i] = arr[i].toInt() - prev.toInt()
            prev = arr[i]
        }
        val byteBuffer = ByteBuffer.allocate(result.size * 4)
        byteBuffer.asIntBuffer().put(result)
        return byteBuffer.array()
    }


    fun decompressString(compressedString: String): MutableList<Short> {
        // Decode the base64 string to a compressed byte array
        val compressed = Base64.decode(compressedString, Base64.DEFAULT)
        // Decompress the byte array using GZIP
        val deltaEncoded = decompress(compressed)
        // Convert the delta-encoded byte array to a list of shorts
        return deltaDecode(deltaEncoded)
    }

    private fun decompress(compressed: ByteArray): ByteArray {
        val bais = ByteArrayInputStream(compressed)
        return GZIPInputStream(bais).use { it.readBytes() }
    }

    private fun deltaDecode(data: ByteArray): MutableList<Short> {
        val byteBuffer = ByteBuffer.wrap(data)
        val result = mutableListOf<Short>()
        var prev: Short = 0
        for (i in 0 until byteBuffer.remaining() / 4) {
            val value = byteBuffer.getInt()
            result.add((prev + value).toShort())
            prev = (prev + value).toShort()
        }
        return result
    }

    fun estimateSize(sequence: String): Int {

        val stringLengthInBytes = sequence.toByteArray(Charsets.UTF_8).size
        return stringLengthInBytes / 1024
    }
}