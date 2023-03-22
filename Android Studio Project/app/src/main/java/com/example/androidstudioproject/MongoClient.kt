
package com.example.androidstudioproject

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class MongoClient {

    private val apiKey = "9qpyQhdqGAHnWLPlK1Cl9zYEVTsjmuAJy8yNDyj54M9AS0VP8ZLVA8VWrMz4DvMR"
    private val httpClient = OkHttpClient()

    private suspend fun makeAPIRequest(endpoint: String, headers: JSONObject, body: JSONObject) : JSONObject {

        val mediaType = "application/json".toMediaType()

        var requestBuilder = Request.Builder()
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
}