package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomePage : AppCompatActivity() {

    private lateinit var joinButton: Button
    private lateinit var createButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        joinButton = findViewById(R.id.joinButton)
        createButton = findViewById(R.id.createButton)


    }
}