package com.example.androidstudioproject

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatRoom : AppCompatActivity() {

    private lateinit var messageList: LinearLayout
    private lateinit var sendButton: Button
    private lateinit var messageInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        messageList = findViewById(R.id.message_list)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        sendButton.setOnClickListener{

            val newMessage = JSONObject().apply {
                put("username", GlobalVars.currentUser)
                put("content", messageInput.text.toString())
                put("timestamp", currentTimestamp())
            }

            addMessage(newMessage)
            messageInput.text.clear()
        }
    }
    private fun addMessage(message: JSONObject){

        val messageLayout = LinearLayout(this)
        messageLayout.orientation = LinearLayout.VERTICAL
        messageLayout.setPadding(20, 20, 20, 20)

        // Username
        val usernameTextView = TextView(this)
        usernameTextView.text = message.getString("username")
        messageLayout.addView(usernameTextView)

        // Content
        val contentTextView = TextView(this)
        contentTextView.text = message.getString("content")
        messageLayout.addView(contentTextView)

        // Timestamp
        val timestampTextView = TextView(this)
        timestampTextView.text = timestampToString(message.getLong("timestamp"))
        timestampTextView.setTextColor(Color.GRAY)
        messageLayout.addView(timestampTextView)

        messageList.addView(messageLayout)
    }

    private fun timestampToString(epoch: Long): String {

        val date = Date(epoch.toLong() * 1000)
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun currentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }
}