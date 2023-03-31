package com.example.androidstudioproject

import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatRoom : AppCompatActivity() {

    private lateinit var messageList: LinearLayout
    private lateinit var sendButton: Button
    private lateinit var messageInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var titleText: TextView

    private val databaseClient = MongoClient()
    private var currentRoom = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        titleText = findViewById(R.id.titleText)

        GlobalScope.launch {

            // the following code assumes the Global Var Room Code is of an existing room code
            currentRoom = databaseClient.findOne("Rooms", JSONObject().put("code", GlobalVars.currentRoomCode))
            runOnUiThread{
                titleText.text = currentRoom.getString("name")

                messageInput.isEnabled = true
                sendButton.isEnabled = true
                sendButton.setBackgroundResource(R.drawable.normal_btn_bg)

                showRoomCodePopup()
            }
        }

        val randomColor = Color.argb(255, (40..200).random(), (40..200).random(), (40..200).random())
        titleText.setBackgroundColor(randomColor)

        scrollView = findViewById(R.id.scrollView)
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY < oldScrollY) {
                // Dismiss Keyboard if scrolling up
                dismissKeyboard()
            }
        }

        messageList = findViewById(R.id.message_list)
        messageInput = findViewById(R.id.messageInput)
        messageInput.setOnClickListener{

            GlobalScope.launch(Dispatchers.IO){
                delay(150)
                scrollToBottom()
            }
        }

        sendButton = findViewById(R.id.sendButton)
        sendButton.setOnClickListener{
            send()
        }

    }
    private fun showRoomCodePopup(){

        // Build the alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome to ${currentRoom.getString("name")}!")
        builder.setMessage("This room's code is ${GlobalVars.currentRoomCode}.\nShare it with your friends so they can join!")
        builder.setPositiveButton("Copy Code") { _, _ ->

            // Copy the code to clipboard
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", GlobalVars.currentRoomCode)
            clipboardManager.setPrimaryClip(clipData)
            showRoomCodePopup()

            Toast.makeText(this, "Code copied!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun addMessageToScrollView(message: JSONObject){

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
        contentTextView.setTypeface(null, Typeface.BOLD)
        contentTextView.setTextColor(Color.BLACK)
        messageLayout.addView(contentTextView)

        // Timestamp
        val timestampTextView = TextView(this)
        timestampTextView.text = timestampToString(message.getLong("timestamp"))
        timestampTextView.setTextColor(Color.GRAY)
        messageLayout.addView(timestampTextView)

        messageLayout.setBackgroundResource(R.drawable.message_rectangle)
        messageList.addView(messageLayout)

    }
    private fun send(){

        if(messageInput.text.toString().trim().isEmpty()){

            showMessageBox("Please type a message first.")
            return
        }

        val newMessage = JSONObject().apply {
            put("username", GlobalVars.currentUser)
            put("content", messageInput.text.toString().trim())
            put("timestamp", currentTimestamp())
        }

        addMessageToScrollView(newMessage)
        messageInput.text.clear()
        scrollToBottom()
        messageInput.requestFocus()
    }
    private fun timestampToString(epoch: Long): String {

        val date = Date(epoch.toLong() * 1000)
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }
    private fun currentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }
    private fun showMessageBox(message: String) {

        // Shows message with OK button
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->  }

        val alert = builder.create()
        alert.show()
    }
    private fun dismissKeyboard(){

        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
    private fun scrollToBottom(){
        scrollView.post{
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}