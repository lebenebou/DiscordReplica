package com.example.androidstudioproject

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import org.json.JSONObject

class Login : AppCompatActivity() {

    private lateinit var identityInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var imgShowHidePassword: ImageView
    private lateinit var signUpText: TextView

    private var isPasswordShown = false
    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide() // to hide actionbar

        signUpText = findViewById(R.id.signUpText)
        signUpText.setOnClickListener{
            startActivity(Intent(this, SignUp::class.java))
        }

        identityInput = findViewById(R.id.identityInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)

        // Login button is pressed
        loginButton.setOnClickListener {

            val userInput = JSONObject()
                .put("identity_input", identityInput.text.toString().lowercase())
                .put("password", passwordInput.text.toString())

            // simulate loading screen here...
            startLoadingMode()
            // start new thread to fetch from database
            GlobalScope.launch(Dispatchers.IO){

                val usernameResult = databaseClient.findOne("Users", JSONObject().put("username", userInput.getString("identity_input")))
                val emailResult = databaseClient.findOne("Users", JSONObject().put("email", userInput.getString("identity_input")))

                // wait for thread to finish, then handleLogin with the received info
                withContext(Dispatchers.Main){
                    handleLogin(userInput, usernameResult, emailResult)
                }
            }
        }

        imgShowHidePassword = findViewById(R.id.imgShowHidePassword)

        imgShowHidePassword.setOnClickListener {
            isPasswordShown = !isPasswordShown
            if (isPasswordShown) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgShowHidePassword.setImageResource(R.drawable.ic_baseline_visibility_24)
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                imgShowHidePassword.setImageResource(R.drawable.ic_baseline_visibility_24)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }
    }
    private fun handleLogin(userInput: JSONObject, usernameResult: JSONObject, emailResult: JSONObject){

        // findOne returned null for both username and email
        if(usernameResult.length()==0 && emailResult.length()==0){
            showMessageBox("Invalid username or E-mail.")
            return
        }

        // either username or email is not null, extract the not null result
        val validResult = if(usernameResult.length() > 0) usernameResult else emailResult

        // check if password matches the not null result
        if(userInput.getString("password") != validResult.getString("password")){
            showMessageBox("Incorrect password.")
            return
        }

        // switch to homepage screen
        startActivity(Intent(this, HomePage::class.java))
    }
    private fun showMessageBox(message: String) {

        // Shows message with OK button
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> endLoadingMode() }

        val alert = builder.create()
        alert.show()
    }
    private fun startLoadingMode(){

        identityInput.isEnabled = false
        passwordInput.isEnabled = false

        loginButton.isEnabled = false
        signUpText.isEnabled = false
    }
    private fun endLoadingMode(){

        identityInput.isEnabled = true
        passwordInput.isEnabled = true

        loginButton.isEnabled = true
        signUpText.isEnabled = true
    }
}