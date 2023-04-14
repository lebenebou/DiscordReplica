package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class Community : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var openRoomButton: Button

    private val databaseClient = MongoClient()
    private var currentCommunity = JSONObject()
    private var availableRooms = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        welcomeText = findViewById(R.id.welcomeText)
        openRoomButton = findViewById(R.id.openRoomButton)
        scrollView = findViewById(R.id.scrollView3)

        openRoomButton.setOnClickListener{
            startActivity(Intent(this, CreateRoom::class.java))
        }

        GlobalScope.launch {

            fetchCurrentCommunity()
            fetchAvailableRooms()

            runOnUiThread {
                openRoomButton.isEnabled = true
                openRoomButton.setBackgroundResource(R.drawable.normal_btn_bg)
                welcomeText.text = "Welcome to " + currentCommunity.getString("name") + "."
            }
        }
    }
    private suspend fun fetchCurrentCommunity(){
        currentCommunity = databaseClient.findOne("Communities", JSONObject().put("code", GlobalVars.currentCommunityCode))
    }
    private suspend fun fetchAvailableRooms(){

        availableRooms = databaseClient.findMultiple("Rooms", JSONObject()
            .put("code", JSONObject()
                .put("\$in", currentCommunity.getJSONArray("rooms"))))
    }
    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Back to Homepage")
        builder.setMessage("Go back to home page?")

        builder.setPositiveButton("Yes") { _, _ ->

            finish()
            startActivity(Intent(this, HomePage::class.java))
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}