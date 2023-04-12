package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class Community : AppCompatActivity() {
    private lateinit var welcomeText: TextView
    private lateinit var openRoomButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        welcomeText=findViewById(R.id.welcomeText)
        openRoomButton=findViewById(R.id.openRoomButton)

        openRoomButton.setOnClickListener{
            startActivity(Intent(this,CreateRoom::class.java))
        }

    }
}