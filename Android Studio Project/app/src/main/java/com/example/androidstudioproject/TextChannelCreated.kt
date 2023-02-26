package com.example.androidstudioproject

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TextChannelCreated : AppCompatActivity() {
        private lateinit var channelCodeTextView: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_text_channel_created)



            channelCodeTextView = findViewById(R.id.channel_code_text_view)

            // Retrieve the channel code from the intent.
            val channelCode = intent.getStringExtra("channelCode")
            /////////////////////////
            Log.d("ChannelCode", "Retrieved channel code: $channelCode")
//////////////////////////////
            // Set the channel code on the text view.
            channelCodeTextView.text = channelCode
        }
    }


