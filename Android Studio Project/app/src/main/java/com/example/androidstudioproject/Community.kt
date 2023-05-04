package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import android.content.SharedPreferences



class Community : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var openRoomButton: Button
    private lateinit var refreshButton: Button
    private lateinit var descriptionText: TextView
    private lateinit var noroom: TextView
    private lateinit var availableRoomsText: TextView
    private lateinit var roomsLayout: LinearLayout

    private lateinit var sharedPrefs: SharedPreferences

    private val databaseClient = MongoClient()
    private var currentCommunity = JSONObject()
    private var localAvailableRooms = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        titleText = findViewById(R.id.welcomeText)
        descriptionText = findViewById(R.id.descriptionText)
        availableRoomsText = findViewById(R.id.mainText2)

        openRoomButton = findViewById(R.id.openRoomButton)
        refreshButton = findViewById(R.id.refreshButton)
        scrollView = findViewById(R.id.scrollView3)
        roomsLayout= findViewById(R.id.roomsLayout)
        noroom= findViewById(R.id.noroom)

        GlobalScope.launch {

            try {
                fetchCurrentCommunity()
                fetchAvailableRooms()
            }
            catch (e: Exception){
                runOnUiThread{ connectionDropped() }
                return@launch
            }

            runOnUiThread {

                endLoadingMode()
                titleText.text = currentCommunity.getString("name") + "."
                descriptionText.text = currentCommunity.getString("description")
                availableRoomsText.text = "Available Rooms (${currentCommunity.getJSONArray("rooms").length()})"

                syncScrollView()
                if(firstTimeEntering()) showCommunityInfo()
            }
        }

        refreshButton.setOnClickListener{

            GlobalScope.launch {

                runOnUiThread { startLoadingMode() }

                try {
                    fetchCurrentCommunity()
                    fetchAvailableRooms()
                }
                catch (e: Exception){
                    runOnUiThread{ connectionDropped() }
                }

                runOnUiThread {
                    syncScrollView()
                    endLoadingMode()
                    availableRoomsText.text = "Available Rooms (${currentCommunity.getJSONArray("rooms").length()})"
                    Toast.makeText(this@Community, "Refreshed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        openRoomButton.setOnClickListener{
            startActivity(Intent(this, CreateRoom::class.java))
        }


    }
    private suspend fun fetchCurrentCommunity(){
        currentCommunity = databaseClient.findOne("Communities", JSONObject().put("code", GlobalVars.currentCommunityCode))
    }
    private suspend fun fetchAvailableRooms(){

        localAvailableRooms = databaseClient.findMultiple("Rooms", JSONObject()
            .put("code", JSONObject()
                .put("\$in", currentCommunity.getJSONArray("rooms"))))
    }
    private fun syncScrollView() {

        roomsLayout.removeAllViews()

        if (localAvailableRooms.length() == 0) {

            val context = noroom.context
            val noRoomText = TextView(context)
            noRoomText.text = "No currently open rooms.\n\nBe the first and open one below!"
            noRoomText.textSize = 15F
            noRoomText.typeface = ResourcesCompat.getFont(context, R.font.montserratextrabold)
            noRoomText.setTextColor(Color.WHITE)
            noRoomText.gravity = Gravity.CENTER
            noRoomText.setPadding(0, 350, 0, 0)
            roomsLayout.addView(noRoomText)
            return
        }

        for (i in 0 until localAvailableRooms.length()) {

            val room = localAvailableRooms.getJSONObject(i)
            addRoomToScrollView(room)
        }
        scrollToBottom()

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
        linearLayout.setBackgroundResource(R.drawable.room_result_bg)
        linearLayout.isClickable = true

        linearLayout.setOnClickListener {
            GlobalVars.currentRoomCode = room.getString("code")
            startActivity(Intent(this, ChatRoom::class.java))
        }

        val roomName = TextView(context)
        roomName.text = room.getString("name")
        roomName.setTextColor(Color.WHITE)
        roomName.setTypeface(null, Typeface.BOLD)
        roomName.textSize = 16f
        roomName.setPadding(20, 10, 0, 0)

        linearLayout.addView(roomName)

        val creatorName = TextView(context)
        creatorName.text = "Created by " + room.getString("creator")
        creatorName.setTextColor(getRandomColor(room.getString("creator")))
        creatorName.textSize = 14f
        creatorName.setPadding(20, 10, 0, 0)
        linearLayout.addView(creatorName)

        val onlineUsers = TextView(context)
        onlineUsers.text = "Online: " + room.getJSONArray("active_users").length()
        onlineUsers.textSize = 14f
        onlineUsers.setPadding(20, 10, 0, 10)
        onlineUsers.setTextColor(Color.WHITE)

        linearLayout.addView(onlineUsers)

        roomsLayout.addView(linearLayout)
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        finish()
        GlobalVars.currentCommunityCode = "000000"
        startActivity(Intent(this, HomePage::class.java))

//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Back to Homepage")
//        builder.setMessage("Go back to home page?")
//
//        builder.setPositiveButton("Yes") { _, _ ->
//
//            finish()
//            GlobalVars.currentCommunityCode = "000000"
//            startActivity(Intent(this, HomePage::class.java))
//        }
//        builder.setNegativeButton("No") { dialog, _ ->
//            dialog.dismiss()
//        }
//        builder.show()
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

    private fun firstTimeEntering(): Boolean {
        sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val communityCode = GlobalVars.currentCommunityCode
        val welcomeMessageShown = sharedPrefs.getBoolean(communityCode, false)

        if (!welcomeMessageShown) {
            // Set the flag to indicate that the welcome message has been shown for this room code
            val editor = sharedPrefs.edit()
            editor.putBoolean(communityCode, true)
            editor.apply()

            return true
        }

        return false
    }

    private fun showCommunityInfo(){

        showMessageBox("Welcome to " + currentCommunity.getString("name") + ".",
            "This community was created by " + currentCommunity.getString("creator") + ".\n\n" +
                    "Description: " + currentCommunity.getString("description") + "\n\n" +
                    "People in this community: " + currentCommunity.getJSONArray("users").length() + "\n\n" +
                    "Open rooms: " + localAvailableRooms.length() + "\n\n" +
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

        refreshButton.setBackgroundResource(R.drawable.normal_btn_bg)
        openRoomButton.setBackgroundResource(R.drawable.green_btn_bg)
    }
    private fun getRandomColor(username: String): Int {

        val random = Random(username.hashCode().toLong() - GlobalVars.currentRoomCode.hashCode().toLong())
        val hue = random.nextInt(360)
        val saturation = 0.7f + random.nextFloat() * 0.4f
        val brightness = 0.6f + random.nextFloat() * 0.4f

        return Color.HSVToColor(floatArrayOf(hue.toFloat(), saturation, brightness))
    }

    private fun connectionDropped(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection Failure")
        builder.setMessage("Your internet connection dropped.\nYou are being redirected to the home screen.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish()
                startActivity(Intent(this, HomePage::class.java))
            }

        val alert = builder.create()
        alert.show()
    }
    private fun scrollToBottom(){
        scrollView.post{
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}