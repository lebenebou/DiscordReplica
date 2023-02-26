package com.example.androidstudioproject

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class ForTextChannelBtn : AppCompatActivity() {
    private lateinit var channelNameEditText: EditText
    private lateinit var channelDescriptionEditText: EditText
    private lateinit var createChannelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_for_text_channel_btn)

        channelNameEditText = findViewById(R.id.channel_name_edit_text)
        channelDescriptionEditText = findViewById(R.id.channel_description_edit_text)
        createChannelButton = findViewById(R.id.create_channel_button)
        createChannelButton.setOnClickListener { createNewChannel() }
    }
    data class Channel(
        val name: String = "",
        val description: String = "",
        val code: String = ""
    )



    private fun createNewChannel() {
        // Get the values entered by the user.
        val channelName = channelNameEditText.text.toString()
        val channelDescription = channelDescriptionEditText.text.toString()

        // Generate a unique channel code.
        val channelCode = generateChannelCode()

        // Save the new channel to Firebase Realtime Database.
        val database = FirebaseDatabase.getInstance().reference
        val newChannel = Channel(channelName, channelDescription, channelCode)
        database.child("channels").child(channelCode).setValue(newChannel)
///////////////////////////////////////////////////////////

        Log.d("ChannelCode", "Generated channel code: $channelCode")


        /////////////////////////////////////////////////
        // Return the channel code to the main activity.
        val resultIntent = Intent()
        resultIntent.putExtra("channelCode", channelCode)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()

        // redirection
        val intent = Intent(this, TextChannelCreated::class.java)
        startActivity(intent)
    }

    private fun generateChannelCode(): String {
        // Generate a random 6-character alphanumeric code.
        val allowedChars = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = kotlin.random.Random
        return (1..6)
            .map { allowedChars[random.nextInt(allowedChars.length)] }
            .joinToString("")

    }

}