package com.example.androidstudioproject

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class SignUp : AppCompatActivity() {

    private val httpClient = OkHttpClient()
    private val apiKey = "9qpyQhdqGAHnWLPlK1Cl9zYEVTsjmuAJy8yNDyj54M9AS0VP8ZLVA8VWrMz4DvMR"

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var imgShowHidePassword: ImageView

    private var isPasswordShown = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide() // to hide actionbar

        GlobalScope.launch(Dispatchers.IO){

            val user = JSONObject().put("username", "YoussefTest")

            // Testing CRUD operations
            println(insertOne("Users", user))
            println(findOne("Users", user))
            println(updateOne("Users", user, JSONObject().put("username", "Lebenebou")))
            println(deleteOne("Users", JSONObject().put("username", "Lebenebou")))
        }

        edtName=findViewById(R.id.edt_name)
        edtEmail=findViewById(R.id.edt_email)
        edtPassword=findViewById(R.id.edt_password)
        btnSignUp=findViewById(R.id.btnSignUp)
        imgShowHidePassword=findViewById(R.id.imgShowHidePassword)

        // Set up password strength requirements
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
        val passwordError = "Password must contain at least 8 characters, including 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character."

        btnSignUp.setOnClickListener{
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            // Check if password meets requirements
            if (!passwordPattern.matches(password)) {
                edtPassword.error = passwordError
                return@setOnClickListener
            }
        }

        imgShowHidePassword.setOnClickListener {
            isPasswordShown = !isPasswordShown
            if (isPasswordShown) {
                edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgShowHidePassword.setImageResource(R.drawable.ic_baseline_visibility_24)
            } else {
                edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imgShowHidePassword.setImageResource(R.drawable.ic_baseline_visibility_24)
            }
            edtPassword.setSelection(edtPassword.text.length)
        }
    }

    private fun makeAPIRequest(endpoint: String, headers: JSONObject, body: JSONObject) : JSONObject {

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
    private fun insertOne(collectionName: String, document: JSONObject) : JSONObject {

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
    private fun findOne(collectionName: String, filter: JSONObject) : JSONObject {

        // returns {document:null} if no matches
        return makeAPIRequest(

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
    }
    private fun deleteOne(collectionName: String, filter: JSONObject) : JSONObject {

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
    private fun updateOne(collectionName: String, filter: JSONObject, updates: JSONObject) : JSONObject {

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
