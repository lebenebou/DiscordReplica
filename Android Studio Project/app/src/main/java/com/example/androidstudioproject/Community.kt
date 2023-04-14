package com.example.androidstudioproject

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Community : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var openRoomButton: Button
    private lateinit var refreshButton: Button
    private lateinit var descriptionText: TextView
    private lateinit var roomsLayout: LinearLayout

    private val databaseClient = MongoClient()
    private var currentCommunity = JSONObject()
    private var availableRooms = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        titleText = findViewById(R.id.welcomeText)
        descriptionText = findViewById(R.id.descriptionText)

        openRoomButton = findViewById(R.id.openRoomButton)
        refreshButton = findViewById(R.id.refreshButton)
        scrollView = findViewById(R.id.scrollView3)
        roomsLayout= findViewById(R.id.roomsLayout)

        refreshButton.setOnClickListener{

            GlobalScope.launch {

                runOnUiThread { startLoadingMode() }

                fetchCurrentCommunity()
                fetchAvailableRooms()

                runOnUiThread {
                    syncScrollView()
                    endLoadingMode()
                }
            }
        }

        openRoomButton.setOnClickListener{
            startActivity(Intent(this, CreateRoom::class.java))
        }

        GlobalScope.launch {

            fetchCurrentCommunity()
            fetchAvailableRooms()

            runOnUiThread {
                endLoadingMode()
                titleText.text = currentCommunity.getString("name") + "."
                descriptionText.text = currentCommunity.getString("description")

                showCommunityInfo()
                syncScrollView()
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
    private fun syncScrollView(){

        scrollView.removeAllViews()
        for(i in 0 until availableRooms.length()){

            val room = availableRooms.getJSONObject(i)
            addRoomToScrollView(room)
        }
    }
    private fun addRoomToScrollView(room: JSONObject) {

        val context = scrollView.context

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)

        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setBackgroundResource(R.drawable.message_rectangle)
        linearLayout.isClickable = true
        linearLayout.setOnClickListener {
            val roomName = room.getString("name")
            println(roomName)
        }

        val roomName = TextView(context)
        roomName.text = room.getString("name")
        roomName.setTextColor(Color.BLACK)
        roomName.textSize = 16f
        linearLayout.addView(roomName)

        val creatorName = TextView(context)
        creatorName.text = "Created by " + room.getString("creator")
        creatorName.setTextColor(getRandomColor(room.getString("creator")))
        creatorName.textSize = 14f
        linearLayout.addView(creatorName)

        val onlineUsers = TextView(context)
        onlineUsers.text = "Online: " + room.getJSONArray("active_users").length()
        onlineUsers.textSize = 14f
        linearLayout.addView(onlineUsers)

        roomsLayout.addView(linearLayout)
        scrollView.addView(roomsLayout)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Back to Homepage")
        builder.setMessage("Go back to home page?")

        builder.setPositiveButton("Yes") { _, _ ->

            finish()
            GlobalVars.currentCommunityCode = "000000"
            startActivity(Intent(this, HomePage::class.java))
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    private fun showMessageBox(title: String, message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    private fun showCommunityInfo(){

        showMessageBox("Welcome to " + currentCommunity.getString("name") + ".",
            "This community was created by " + currentCommunity.getString("creator") + ".\n\n" +
                    "Description: " + currentCommunity.getString("description") + "\n\n" +
                    "Open rooms: " + availableRooms.length() + "\n\n" +
                    "Join an available room or open a new one to start chatting!")
    }
    private fun startLoadingMode(){

        refreshButton.isEnabled = false
        openRoomButton.isEnabled = false
        refreshButton.text = "Refreshing..."

        refreshButton.setBackgroundResource(R.drawable.grey_btn_bg)
        openRoomButton.setBackgroundResource(R.drawable.grey_btn_bg)
    }
    private fun endLoadingMode(){

        refreshButton.isEnabled = true
        openRoomButton.isEnabled = true
        refreshButton.text = "Refresh"

        refreshButton.setBackgroundResource(R.drawable.green_btn_bg)
        openRoomButton.setBackgroundResource(R.drawable.normal_btn_bg)
    }
    private fun getRandomColor(username: String): Int {

        val random = Random(username.hashCode().toLong() - GlobalVars.currentRoomCode.hashCode().toLong())
        val hue = random.nextInt(360)
        val saturation = 0.7f + random.nextFloat() * 0.4f
        val brightness = 0.6f + random.nextFloat() * 0.4f

        return Color.HSVToColor(floatArrayOf(hue.toFloat(), saturation, brightness))
    }

}