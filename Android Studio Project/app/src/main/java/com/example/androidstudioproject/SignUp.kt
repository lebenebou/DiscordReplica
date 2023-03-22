package com.example.androidstudioproject

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SignUp : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var mailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var signUpButton: Button

    private lateinit var imgShowHidePassword: ImageView

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

        imgShowHidePassword = findViewById(R.id.imgShowHidePassword)

        // user clicks sign up button
        signUpButton.setOnClickListener {

            val userInput = JSONObject()
                .put("username", usernameInput.text.toString())
                .put("email", mailInput.text.toString())
                .put("password", passwordInput.text.toString())

            // Check if password meets requirements
            // Make email lowercase
            // check if email meets requirements
            // make username lowercase

            // simulate loading screen here...

            // create a thread to fetch from database (async)
            GlobalScope.launch(Dispatchers.IO){

                val emailResult = databaseClient.findOne("Users", JSONObject().put("email", userInput.get("email")))
                val usernameResult = databaseClient.findOne("Users", JSONObject().put("username", userInput.get("username")))

                // wait for thread to finish and then handleSignUp with the received info
                withContext(Dispatchers.Main){
                    handleSignUp(userInput, emailResult, usernameResult)
                }
            }
        }

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

     fun onLoginTextClick(view:View) {
         // switch to Login screen
         startActivity(Intent(this, Login::class.java))
     }

    private fun handleSignUp(userInput: JSONObject, emailResult: JSONObject, usernameResult: JSONObject){

        // username findOne was not null
        if(usernameResult.length() > 0){
            showMessageBox("This username already exists.\nTry a different one.")
            return
        }
        // email findOne was not null
        if(emailResult.length() > 0){
            showMessageBox("This E-mail already exists.")
            return
        }

        // simulate loading screen here...

        // email and username are valid
        // start thread to insert new user into database
        GlobalScope.launch(Dispatchers.IO){
            databaseClient.insertOne("Users", userInput)
        }


        // switch to home page screen
        startActivity(Intent(this, HomePage::class.java))
    }

    private fun showMessageBox(message: String) {

        // Shows message with OK button
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> ; }

        val alert = builder.create()
        alert.show()
    }
}
