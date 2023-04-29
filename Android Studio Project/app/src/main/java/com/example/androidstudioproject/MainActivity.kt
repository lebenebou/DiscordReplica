package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var accountsButton: Button
    private lateinit var joinCreateButton: Button
    private lateinit var chatroomsButton: Button

    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accountsButton = findViewById(R.id.accountsButton)
        joinCreateButton = findViewById(R.id.joinCreateButton)
        chatroomsButton = findViewById(R.id.chatroomsButton)


        // Get the user credentials from shared preferences
        sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)

        // Check if credentials exist and redirect to home page
        if (username != null && password != null) {
            // Credentials exist, redirect to home page
            GlobalVars.currentUser = username
            startActivity(Intent(this, HomePage::class.java))
            finish() // Remove LoginActivity from back stack
        } else {
            // Credentials do not exist, show login page
            startActivity(Intent(this, Login::class.java))
        }


        accountsButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
        joinCreateButton.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
        }
        chatroomsButton.setOnClickListener {
            startActivity(Intent(this, ChatRoom::class.java))
        }
    }
}