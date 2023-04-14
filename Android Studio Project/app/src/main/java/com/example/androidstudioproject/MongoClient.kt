
package com.example.androidstudioproject

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

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
}