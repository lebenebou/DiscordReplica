package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SignUp : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var imgShowHidePassword: ImageView
    //firebase auth
    private var isPasswordShown = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide() // to hide actionbar

        // Testing MongoDB for find one
        GlobalScope.launch(Dispatchers.IO) {

            val apiKey = "9qpyQhdqGAHnWLPlK1Cl9zYEVTsjmuAJy8yNDyj54M9AS0VP8ZLVA8VWrMz4DvMR"
            val databaseName = "DiscordReplica"
            val collectionName = "UserInformation"

            val data = JSONObject().put("username","Youfi")
            println(findOne(apiKey, databaseName, collectionName, JSONObject().put("username", "Youssef")))
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
    fun onLoginClick(view: View) {
        // Handle the click event here, e.g. launch the login activity
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    private fun findOne(apiKey: String, databaseName: String, collectionName: String, filter: JSONObject): JSONObject {

        println(filter.toString())
        val client = OkHttpClient()
        val url = "https://eu-central-1.aws.data.mongodb-api.com/app/data-wzbfu/endpoint/data/v1/action/findOne"

        val mediaType = "application/json".toMediaType()
        val body = """
                {
                    "collection": "$collectionName",
                    "database": "$databaseName",
                    "dataSource": "Cluster1",
                    "filter": ${filter.toString()}
                }
            """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .method("POST", body.toRequestBody(mediaType))
            .addHeader("Content-Type", "application/json")
            .addHeader("Access-Control-Request-Headers", "*")
            .addHeader("api-key", apiKey)
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body?.string() ?: ""
        return JSONObject(responseBody)
    }
}
