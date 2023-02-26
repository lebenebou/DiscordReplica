package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
//    private lateinit var btnSignUp: Button
    private lateinit var text_signup:EditText
    private lateinit var imgShowHidePassword: ImageView

    //firebase auth
    private lateinit var mAuth: FirebaseAuth
    private var isPasswordShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()//to hide actionbar
        mAuth = FirebaseAuth.getInstance()//firebase authentication

        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnLogin = findViewById(R.id.btnLogin)
//        btnSignUp = findViewById(R.id.btnSignUp)
        val text_signup=findViewById<TextView>(R.id.text_signup)
        imgShowHidePassword = findViewById(R.id.imgShowHidePassword)

        //to when signup btn press go to signup w zabatet l inten bl manifest  hatyta b login ta y2ale3 login msh main activity
//        text_login.setOnClickListener {
//            val intent = Intent(this, SignUp::class.java)
//            startActivity(intent)
//        }
        text_signup.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
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

        //shubdo ysir onclicking login
        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //code for loging in user
                        val intent = Intent(this@Login, HomePage::class.java)
                        finish()
                        startActivity(intent)
                    } else {
                        // get the exception message
                        val errorMessage = task.exception?.message
                        // check if the error message contains "no user" or "password"
                        if (errorMessage?.contains("no user") == true) {
                            Toast.makeText(this@Login, "User does not exist", Toast.LENGTH_SHORT)
                                .show()
                        } else if (errorMessage?.contains("password") == true) {
                            Toast.makeText(this@Login, "Incorrect password", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                this@Login,
                                "Login failed: $errorMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }


    }
}