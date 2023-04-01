package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

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

            if (userInput.get("identity_input")=="") return@setOnClickListener showMessageBox("Please provide a username or E-mail.")
            if (userInput.get("password")=="") return@setOnClickListener showMessageBox("Please provide a password.")

            // start new thread to fetch from database
            GlobalScope.launch(Dispatchers.IO){

                runOnUiThread{ startLoadingMode() }

                if(!isConnected(this@Login)){ // check internet connection
                    runOnUiThread{ showMessageBox("Unable to connect.\nPlease make sure you have an active internet connection.")}
                    return@launch
                }

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

        // set global user variable
        GlobalVars.currentUser = validResult.getString("username")
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

        // change colors to grey
        loginButton.setBackgroundResource(R.drawable.grey_btn_bg)
        signUpText.setTextColor(ContextCompat.getColor(this, R.color.grey))
    }
    private fun endLoadingMode(){

        identityInput.isEnabled = true
        passwordInput.isEnabled = true

        loginButton.isEnabled = true
        signUpText.isEnabled = true

        // change colors back to normal
        loginButton.setBackgroundResource(R.drawable.normal_btn_bg)
        signUpText.setTextColor(ContextCompat.getColor(this, R.color.purple_700))
    }
    private suspend fun isConnected(context: Context): Boolean {

        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://www.google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1000
                connection.connect()
                connection.responseCode == 200
            } catch (e: IOException) {
                false
            }
        }
    }
}