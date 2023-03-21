package com.example.androidstudioproject

import android.content.Intent
import android.graphics.DiscretePathEffect
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SignUp : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var imgShowHidePassword: ImageView

    private var isPasswordShown = false
    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide() // to hide actionbar

        edtName=findViewById(R.id.edt_name)
        edtEmail=findViewById(R.id.edt_email)
        edtPassword=findViewById(R.id.edt_password)
        btnSignUp=findViewById(R.id.btnSignUp)
        imgShowHidePassword=findViewById(R.id.imgShowHidePassword)

        // Set up password strength requirements
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
        val passwordError = "Password must contain at least 8 characters, including 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character."

        btnSignUp.setOnClickListener {

            val userInput = JSONObject()
                .put("username", edtName.text.toString())
                .put("email", edtEmail.text.toString())
                .put("password", edtPassword.text.toString())

            // Check if password meets requirements
            // Make email lowercase
            // check if email meets requirements
            // make username lowercase

            GlobalScope.launch(Dispatchers.IO){

                val emailResult = databaseClient.findOne("Users", JSONObject().put("email", userInput.get("email")))
                val usernameResult = databaseClient.findOne("Users", JSONObject().put("username", userInput.get("username")))

                withContext(Dispatchers.Main){

                    handleSignUp(userInput, emailResult, usernameResult)
                }
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

     fun onLoginTextClick(view:View) {
         val intent = Intent(this, Login::class.java)
         startActivity(intent)
     }

    private fun handleSignUp(userInput: JSONObject, emailResult: JSONObject, usernameResult: JSONObject){

        if(emailResult.length() > 0){
            Toast.makeText(this, "This E-mail already exists!", Toast.LENGTH_SHORT).show()
            return
        }
        if(usernameResult.length() > 0){
            Toast.makeText(this, "This username already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO){

            databaseClient.insertOne("Users", userInput)
        }

        startActivity(Intent(this, HomePage::class.java))
    }


}
