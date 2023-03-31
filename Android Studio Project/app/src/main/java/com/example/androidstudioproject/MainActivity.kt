package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var accountsButton: Button
    private lateinit var joinCreateButton: Button
    private lateinit var chatroomsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accountsButton = findViewById(R.id.accountsButton)
        joinCreateButton = findViewById(R.id.joinCreateButton)
        chatroomsButton = findViewById(R.id.chatroomsButton)

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