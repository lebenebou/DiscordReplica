package com.example.androidstudioproject

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.util.Base64
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SignUp : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var mailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var signUpButton: Button

    private lateinit var passwordEye: ImageView


    private var isPasswordShown = false
    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        supportActionBar?.hide() // to hide actionbar

        usernameInput = findViewById(R.id.usernameInput)
        mailInput = findViewById(R.id.identityInput)
        passwordInput = findViewById(R.id.passwordInput)
        signUpButton = findViewById(R.id.signUpButton)


        passwordEye = findViewById(R.id.imgShowHidePassword)

        // user clicks sign up button
        signUpButton.setOnClickListener {

            val userInput = JSONObject()
                .put("username", usernameInput.text.toString().lowercase().trim())
                .put("email", mailInput.text.toString().lowercase().trim())
                .put("password", passwordInput.text.toString().trim())

            // username validation check
            if(!isValidUsername(userInput.getString("username"))){

                showMessageBox("Invalid Username","Usernames must be at least 3 characters\nand contain only letters or numbers.")
                return@setOnClickListener
            }

            // email validation check
            if(!isValidMail(userInput.getString("email"))){
                showMessageBox("Invalid Email", "Please enter a valid email.")
                return@setOnClickListener
            }

            // password validation check
            if(!isValidPass(userInput.getString("password"))){

                showMessageBox("Invalid Password", "Password does not meet the following requirements:\n\n" +
                        "Length of at least 8.\n" +
                        "1 capital letter.\n" +
                        "1 lowercase letter.\n" +
                        "1 number.")

                return@setOnClickListener
            }

            // create a thread to fetch from database (async)
            GlobalScope.launch(Dispatchers.IO){

                runOnUiThread{ startLoadingMode() }

                try {
                    val emailResult = databaseClient.findOne("Users", JSONObject().put("email", userInput.get("email")))
                    val usernameResult = databaseClient.findOne("Users", JSONObject().put("username", userInput.get("username")))
                    runOnUiThread{ handleSignUp(userInput, emailResult, usernameResult) }
                }
                catch (e: Exception){
                    runOnUiThread{ showMessageBox("Connection Failure","Please make sure you have an active internet connection.")}
                }
            }
        }

        passwordEye.setOnClickListener {
            isPasswordShown = !isPasswordShown
            if (isPasswordShown) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordEye.setImageResource(R.drawable.ic_baseline_visibility_24)
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordEye.setImageResource(R.drawable.ic_baseline_visibility_24)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }
    }

    private fun handleSignUp(userInput: JSONObject, emailResult: JSONObject, usernameResult: JSONObject){

        // username findOne was not null
        if(usernameResult.length() > 0){
            showMessageBox("Duplicate Username","This username already exists.\nTry a different one.")
            return
        }
        // email findOne was not null
        if(emailResult.length() > 0){
            showMessageBox("Duplicate Email","This E-mail already exists.")
            return
        }

        // encrypt password
        userInput.put("password", databaseClient.encrypt(userInput.getString("password")))
        // add empty communities array
        userInput.put("communities", JSONArray())

        // email and username are valid
        // start thread to insert new user into database
        GlobalScope.launch(Dispatchers.IO){
            databaseClient.insertOne("Users", userInput)
        }

        // set global username value
        GlobalVars.currentUser = userInput.getString("username")
        // switch to home page screen
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
    private fun isValidPass(password: String): Boolean {

        return Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}\$").matches(password)
    }
    private fun isValidMail(email: String): Boolean {

        return Regex("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-]+)(\\.[a-zA-Z]{2,5}){1,2}\$").matches(email)
    }
    private fun isValidUsername(username: String): Boolean {

        if(username.length < 3) return false
        return Regex("^[a-zA-Z_\$][a-zA-Z_\$0-9]*\$").matches(username)
    }
    fun onLoginTextClick(view:View) {

        finish()
        startActivity(Intent(this, Login::class.java))
    }
    private fun startLoadingMode(){

        usernameInput.isEnabled = false
        mailInput.isEnabled = false
        passwordInput.isEnabled = false

        signUpButton.isEnabled = false
        findViewById<TextView>(R.id.loginText).isEnabled = false

        // change colors ot grey
        findViewById<TextView>(R.id.loginText).setTextColor(ContextCompat.getColor(this, R.color.light_grey))
        signUpButton.setBackgroundResource(R.drawable.grey_btn_bg)
    }
    private fun endLoadingMode(){

        usernameInput.isEnabled = true
        mailInput.isEnabled = true
        passwordInput.isEnabled = true

        signUpButton.isEnabled = true
        findViewById<TextView>(R.id.loginText).isEnabled = true

        // change colors back to normal
        signUpButton.setBackgroundResource(R.drawable.normal_btn_bg)
    }
}
