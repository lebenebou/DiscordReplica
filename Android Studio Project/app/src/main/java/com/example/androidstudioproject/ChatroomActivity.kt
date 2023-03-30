package com.example.androidstudioproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// ChatroomActivity.kt
class ChatroomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        // Find the RecyclerView by its ID
        val messageRecyclerView = findViewById<RecyclerView>(R.id.message_recycler_view)

        // Set up the LinearLayoutManager for the RecyclerView
        val linearLayoutManager = LinearLayoutManager(this)
        messageRecyclerView.layoutManager = linearLayoutManager

        // Set up the MessageAdapter for the RecyclerView
        val messageAdapter = MessageAdapter(DataRepository.messageList)
        messageRecyclerView.adapter = messageAdapter
    }
}
