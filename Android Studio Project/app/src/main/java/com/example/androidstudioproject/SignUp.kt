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
                .put("username", usernameInput.text.toString().lowercase())
                .put("email", mailInput.text.toString().lowercase())
                .put("password", passwordInput.text.toString())

            // username validation check
            if(!isValidUsername(userInput.getString("username"))){

                showMessageBox("Usernames must be at least 3 characters\nand contain only letters or numbers.")
                return@setOnClickListener
            }

            // email validation check
            if(!isValidMail(userInput.getString("email"))){
                showMessageBox("Please enter a valid email")
                return@setOnClickListener
            }

            // password validation check
            println(userInput.getString("password"))
            if(!isValidPass(userInput.getString("password"))){

                showMessageBox("Password does not meet the following requirements:\n\n" +
                        "Length of at least 8.\n" +
                        "1 capital letter.\n" +
                        "1 lowercase letter.\n" +
                        "1 number.")

                return@setOnClickListener
            }

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
        // switch to Login screen
        startActivity(Intent(this, Login::class.java))
    }
}
