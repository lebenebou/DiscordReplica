package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class Login : AppCompatActivity() {
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var imgShowHidePassword: ImageView
    private lateinit var signupText: TextView


    private var isPasswordShown = false

    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()//to hide actionbar

        signupText = findViewById(R.id.text_signup)
        signupText.setOnClickListener{
            startActivity(Intent(this, SignUp::class.java))
        }

        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnLogin = findViewById(R.id.btnLogin)

        imgShowHidePassword = findViewById(R.id.imgShowHidePassword)

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

        // Login button is pressed
        btnLogin.setOnClickListener {

            val userInput = JSONObject()
                .put("first_input", edtEmail.text.toString())
                .put("password", edtPassword.text.toString())

            GlobalScope.launch(Dispatchers.IO){

                val usernameResult = databaseClient.findOne("Users", JSONObject().put("username", userInput.getString("first_input")))
                val emailResult = databaseClient.findOne("Users", JSONObject().put("email", userInput.getString("first_input")))

                withContext(Dispatchers.Main){

                    handleLogin(userInput, usernameResult, emailResult)
                }
            }
        }
    }

    private fun handleLogin(userInput: JSONObject, usernameResult: JSONObject, emailResult: JSONObject){

        if(usernameResult.length()==0 && emailResult.length()==0){
            Toast.makeText(this, "Invalid username or email", Toast.LENGTH_SHORT).show()
            return
        }

        val validResult = if(usernameResult.length() > 0) usernameResult else emailResult

        if(userInput.getString("password") != validResult.getString("password")){
            Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            return
        }

        startActivity(Intent(this, HomePage::class.java))
    }
}