package com.example.androidstudioproject

import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.AlphabeticIndex.Record
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class Login : AppCompatActivity() {

    private lateinit var identityInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var imgShowHidePassword: ImageView
    private lateinit var signUpText: TextView
    private lateinit var rememberMeCheckBox : CheckBox

    private var isPasswordShown = false
    private val databaseClient = MongoClient()

    // Shared Preferences Object
    private lateinit var sharedPreferences: SharedPreferences

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
        rememberMeCheckBox = findViewById(R.id.checkBox)

        // Login button is pressed
        loginButton.setOnClickListener {

            val userInput = JSONObject()
                .put("identity_input", identityInput.text.toString().lowercase().trim())
                .put("password", passwordInput.text.toString().trim())

            if (userInput.get("identity_input")=="") return@setOnClickListener showMessageBox("Empty Input","Please provide a username or E-mail.")
            if (userInput.get("password")=="") return@setOnClickListener showMessageBox("Empty Input","Please provide a password.")

            // start new thread to fetch from database
            GlobalScope.launch(Dispatchers.IO){

                runOnUiThread{ startLoadingMode() }

                try {
                    val usernameResult = databaseClient.findOne("Users", JSONObject().put("username", userInput.getString("identity_input")))
                    val emailResult = databaseClient.findOne("Users", JSONObject().put("email", userInput.getString("identity_input")))
                    runOnUiThread{ handleLogin(userInput, usernameResult, emailResult) }
                }
                catch (e: Exception){
                    runOnUiThread{ showMessageBox("Connection Failure", "Please make sure you have an active internet connection.")}
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
            showMessageBox("Account Not Found","Invalid username or E-mail.")
            return
        }

        // either username or email is not null, extract the not null result
        val validResult = if(usernameResult.length() > 0) usernameResult else emailResult

        // check if password matches the not null result
        if(userInput.getString("password") != databaseClient.decrypt(validResult.getString("password"))){
            showMessageBox("Incorrect password", "This password does not match the specified account.")
            return
        }

        // set global user variable
        GlobalVars.currentUser = validResult.getString("username")

        if(rememberMeCheckBox.isChecked){

            sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString("username", validResult.getString("username"))
            editor.putString("password", validResult.getString("password"))
            editor.apply()
        }

        // switch to homepage screen
        finish()
        startActivity(Intent(this, HomePage::class.java))
    }
    private fun showMessageBox(title: String, message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, _ ->

            endLoadingMode()
            dialog.dismiss()
        }
        builder.show()
    }
    private fun startLoadingMode(){

        identityInput.isEnabled = false
        passwordInput.isEnabled = false

        loginButton.isEnabled = false
        loginButton.text = "Logging In..."

        // change colors to grey
        loginButton.setBackgroundResource(R.drawable.grey_btn_bg)
        signUpText.setTextColor(ContextCompat.getColor(this, R.color.light_grey))
    }
    private fun endLoadingMode(){

        identityInput.isEnabled = true
        passwordInput.isEnabled = true

        loginButton.isEnabled = true
        signUpText.isEnabled = true
        loginButton.text = "Log In"

        // change colors back to normal
        loginButton.setBackgroundResource(R.drawable.normal_btn_bg)
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        return
    }
}