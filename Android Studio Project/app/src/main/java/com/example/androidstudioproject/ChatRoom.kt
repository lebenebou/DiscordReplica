package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ChatRoom : AppCompatActivity() {

    private var currentRoomCode: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
    }
}