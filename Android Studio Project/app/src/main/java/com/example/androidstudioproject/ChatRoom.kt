package com.example.androidstudioproject

import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatRoom : AppCompatActivity() {

    private lateinit var messageList: LinearLayout
    private lateinit var sendButton: Button
    private lateinit var messageInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var titleText: TextView

    private val roomColor = Color.argb(255, (40..200).random(), (40..200).random(), (40..200).random())

    private val databaseClient = MongoClient()
    private var currentRoom = JSONObject()
    private var localMessages = JSONArray()

    var disconnected = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // this scope.launch runs every 2 seconds
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {

            while (true){

                delay(2000)

                try {
                    updateCurrentRoom() // fetch new CurrentRoom JSON
                }
                catch (e: Exception){
                    runOnUiThread{ connectionDropped() }
                }

                runOnUiThread{ // sync messages from currentRoom to the UI
                    syncMessages(currentRoom.getJSONArray("messages"))
                    endSendingMode()
                }
            }
        }

        titleText = findViewById(R.id.titleText)

        GlobalScope.launch {

            // the following code assumes the Global Var Room Code is of an existing room code

            databaseClient.addToActiveUsers(GlobalVars.currentRoomCode, GlobalVars.currentUser)
            updateCurrentRoom()
            runOnUiThread {

                titleText.text = currentRoom.getString("name")

                messageInput.isEnabled = true
                sendButton.isEnabled = true
                sendButton.setBackgroundResource(R.drawable.normal_btn_bg)

                showRoomCodePopup()
                syncMessages(currentRoom.getJSONArray("messages"))
            }
        }

        titleText.setBackgroundColor(roomColor)

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

            if(messageInput.text.toString().trim().isEmpty()){

                showMessageBox("Please type a message first.")
                messageInput.text.clear()
                return@setOnClickListener
            }

            val newMessage = JSONObject().apply {
                put("username", GlobalVars.currentUser)
                put("content", messageInput.text.toString().trim())
                put("timestamp", currentTimestamp())
            }
            GlobalScope.launch {

                runOnUiThread{ startSendingMode() }

                try {
                    handleSend(newMessage)
                }
                catch (e: Exception){
                    runOnUiThread { connectionDropped() }
                }
            }
            messageInput.text.clear()
            messageInput.requestFocus()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Leaving Room")
        builder.setMessage("Are you sure you want to leave this room?")

        builder.setPositiveButton("Yes") { _, _ ->

            GlobalScope.launch { leaveRoom() }
            finish()
            startActivity(Intent(this, HomePage::class.java))
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.chatroom_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.menu_room_info -> {

                showRoomCodePopup()
                return true
            }
            R.id.menu_users_in_room -> {

                showOnlineUsers()
                return true
            }
            R.id.menu_leave_room -> {

                this.onBackPressed()
                return true
            }
            R.id.menu_logout -> {

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private suspend fun leaveRoom(){

        GlobalVars.currentRoomCode = "000000"
        databaseClient.removeFromActiveUsers(GlobalVars.currentRoomCode, GlobalVars.currentUser)
    }
    private fun showOnlineUsers(){

        var popupText = "Users In This Room:\n\n"

        val userList = currentRoom.getJSONArray("active_users")

        for(i in 0 until userList.length()){

            popupText += (i+1).toString() + ". "
            popupText += userList.getString(i) + "\n"
        }

        showMessageBox(popupText)
    }
    private fun showRoomCodePopup(){

        // Build the alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome to ${currentRoom.getString("name")}!")
        builder.setMessage("Creator: ${currentRoom.getString("creator")}\nThis room's code is ${GlobalVars.currentRoomCode}.\nShare it with your friends so they can join!")
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
    private suspend fun handleSend(newMessage: JSONObject){

        databaseClient.addToMessages(currentRoom.getString("code"), newMessage)
    }
    private suspend fun updateCurrentRoom(){

        currentRoom = databaseClient
            .findOne("Rooms", JSONObject()
                .put("code", GlobalVars.currentRoomCode))
    }
    private fun syncMessages(newMessages: JSONArray){

        for(i in localMessages.length() until newMessages.length()){
            addMessageToScrollView(newMessages.getJSONObject(i))
            scrollToBottom()
        }
        localMessages = newMessages // update the local messages
    }
    private fun connectionDropped(){

        if (disconnected) return
        disconnected = true
        // this function needs to run only on first disconnection

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection Failure")
        builder.setMessage("Your internet connection dropped.\nPlease log back in.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish()
                startActivity(Intent(this, Login::class.java))
            }

        val alert = builder.create()
        alert.show()
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
    private fun startSendingMode(){

        sendButton.isEnabled = false
        sendButton.text = "Sending..."
        sendButton.setBackgroundResource(R.drawable.grey_btn_bg)
    }
    private fun endSendingMode(){

        sendButton.isEnabled = true
        sendButton.text = "Send"
        sendButton.setBackgroundResource(R.drawable.normal_btn_bg)
    }
}