package com.example.androidstudioproject

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.*
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences

class ChatRoom : AppCompatActivity() {

    private lateinit var messageList: LinearLayout
    private lateinit var sendButton: Button
    private lateinit var messageInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var titleText: TextView
    private lateinit var micIcon: ImageView

    private val roomColor = getRandomColor()

    private val databaseClient = MongoClient()
    private var currentRoom = JSONObject()
    private var localMessages = JSONArray()

    private lateinit var sharedPrefs: SharedPreferences

    var disconnected = false

    // Objects for Recording
    private var isRecording = false
    private var isPlaying = false

    private val deferred = CompletableDeferred<Boolean>()

    private val audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_VOICE_CALL)

    private val intBufferSize = AudioRecord.getMinBufferSize(intRecordSampleRate, channelConfig, audioFormat)

    var recordedShorts: MutableList<Short> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        sharedPrefs = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val roomCode = GlobalVars.currentRoomCode
        val welcomeMessageShown = sharedPreferences.getBoolean(roomCode, false)



        setContentView(R.layout.activity_chat_room)

        // this scope runs every 2 seconds
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

                if(currentRoom.length() == 0) continue

                runOnUiThread{ // sync messages from currentRoom to the UI
                    syncMessages(currentRoom.getJSONArray("messages"))
                    endSendingMode()
                }
            }
        }

        titleText = findViewById(R.id.titleText)
        micIcon = findViewById(R.id.micIcon)

        GlobalScope.launch {

            // the following code assumes the Global Var Room Code is of an existing room code

            try{
                databaseClient.addToActiveUsers(GlobalVars.currentRoomCode, GlobalVars.currentUser)
                updateCurrentRoom()
            }
            catch (e: Exception){
                runOnUiThread { connectionDropped() }
            }

            runOnUiThread {

                titleText.text = currentRoom.getString("name")

                messageInput.isEnabled = true
                sendButton.isEnabled = true
                sendButton.setBackgroundResource(R.drawable.normal_btn_bg)

                if (firstTimeEntering()) showRoomInfo()
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

                showMessageBox("Empty message","Please type a message first.")
                messageInput.text.clear()
                return@setOnClickListener
            }

            val newMessage = JSONObject().apply {
                put("username", GlobalVars.currentUser)
                put("content", messageInput.text.toString().trim())
                put("timestamp", currentTimestamp())
                put("is_text", true)
            }
            GlobalScope.launch {

                runOnUiThread{ startSendingMode() }

                try {
                    addMessageToDB(newMessage)
                }
                catch (e: Exception){
                    runOnUiThread { connectionDropped() }
                }
            }
            messageInput.text.clear()
            messageInput.requestFocus()
        }
        hideSendButton()

        messageInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                // This method is called when the text is changed.
                // You can add your code to be executed here.

            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called after the text is changed.
                if(messageInput.text.isEmpty()) hideSendButton()
                else showSendButton()
            }
        })

        micIcon.setOnClickListener{

            if(!isRecording){

                startRecordingMode()
                GlobalScope.launch {

                    recordedShorts = recordAndSave()
                    println("Recorded a buffer of size ${recordedShorts.size}")
                }
            }
            else{ // is already recording

                endRecordingMode()
                GlobalScope.launch {

                    delay(100)

                    runOnUiThread {
                        startSendingMode()
                    }

                    val newVoiceMessage = JSONObject().apply {
                        put("username", GlobalVars.currentUser)
                        put("content", databaseClient.compressList(recordedShorts))
                        put("timestamp", currentTimestamp())
                        put("is_text", false)
                    }

                    try{
                        addVoiceMessageToDB(newVoiceMessage)
                    }
                    catch(e: Exception){
                        runOnUiThread { connectionDropped() }
                    }

                    recordedShorts.clear()
                }
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Leaving Room")
        builder.setMessage("Are you sure you want to leave this room?")

        builder.setPositiveButton("Yes") { _, _ ->

            GlobalScope.launch {

                try {
                    databaseClient.removeFromActiveUsers(GlobalVars.currentRoomCode, GlobalVars.currentUser)
                }
                catch(e: Exception){
                    runOnUiThread { connectionDropped() }
                }
            }
            finish()
            if(GlobalVars.currentCommunityCode=="000000"){
                startActivity(Intent(this, HomePage::class.java))
            }
            else{
                startActivity(Intent(this, Community::class.java))
            }
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

                showRoomInfo()
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
            R.id.clear_chat -> {

                attemptToClearChat()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun attemptToClearChat(){

        if(currentRoom.getString("creator") != GlobalVars.currentUser) return showMessageBox("Access Denied", "Sorry, only the room creator can clear a rooms chat.")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Clear Chat")
        builder.setMessage("Are you sure you want to clear this room's chat?\n\nThis operation might take a while to take effect for other users.\n\nYou will also have to rejoin the room.")

        builder.setPositiveButton("Yes") { _, _ ->

            GlobalScope.launch {

                try {
                    clearRoomMessages()
                    databaseClient.removeFromActiveUsers(GlobalVars.currentRoomCode, GlobalVars.currentUser)
                }
                catch(e: Exception){
                    runOnUiThread { connectionDropped() }
                }
            }
            finish()
            if(GlobalVars.currentCommunityCode=="000000"){
                startActivity(Intent(this, HomePage::class.java))
            }
            else{
                startActivity(Intent(this, Community::class.java))
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    private fun showOnlineUsers(){

        var popupText = ""

        val userList = currentRoom.getJSONArray("active_users")

        for(i in 0 until userList.length()){

            popupText += (i+1).toString() + ". "
            popupText += userList.getString(i) + "\n"
        }

        showMessageBox("Users in This Room", popupText)
    }

    private fun firstTimeEntering(): Boolean {
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val roomCode = GlobalVars.currentRoomCode
        val welcomeMessageShown = sharedPreferences.getBoolean(roomCode, false)

        if (!welcomeMessageShown) {
            // Set the flag to indicate that the welcome message has been shown for this room code
            val editor = sharedPreferences.edit()
            editor.putBoolean(roomCode, true)
            editor.apply()

            return true
        }

        return false
    }

    private fun showRoomInfo() {

        val roomCode = GlobalVars.currentRoomCode
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome to ${currentRoom.getString("name")}!")

        var messageText = "Creator: ${currentRoom.getString("creator")}"
        if (currentRoom.getString("creator") == GlobalVars.currentUser) {
            messageText += "\n\nThis room's code is $roomCode.\n" +
                    "This code is only shown to you.\n" +
                    "\nShare it with friends who wish to join this room."
        }

        builder.setMessage(messageText)
        if (currentRoom.getString("creator") == GlobalVars.currentUser) {
            builder.setPositiveButton("Copy Code") { _, _ ->
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", roomCode)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this, "Code copied!", Toast.LENGTH_SHORT).show()
                showRoomInfo()
            }
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
        usernameTextView.setTypeface(null, Typeface.BOLD)
        usernameTextView.setTextColor(getRandomColor(message.getString("username")))
        messageLayout.addView(usernameTextView)

        // Content
        val contentTextView = TextView(this)

        if(message.getBoolean("is_text")) contentTextView.text = message.getString("content")
        else contentTextView.text = "VOICE MESSAGE (${databaseClient.estimateSize(message.getString("content"))} KB) - CLICK TO PLAY"

        contentTextView.setTypeface(null, Typeface.BOLD)
        contentTextView.setTextColor(Color.WHITE)
        messageLayout.addView(contentTextView)

        // Timestamp
        val timestampTextView = TextView(this)
        timestampTextView.text = timestampToString(message.getLong("timestamp"))
        timestampTextView.setTextColor(Color.GRAY)
        messageLayout.addView(timestampTextView)

        // Stop here if just text
        if(message.getBoolean("is_text")) return messageList.addView(messageLayout)

        // Modify message for voice
        messageLayout.isClickable = true
        messageLayout.setOnClickListener{

            GlobalScope.launch {

                if(isPlaying){
                    runOnUiThread { showMessageBox("Already Listening", "Please finish listening to the current voice message") }
                    return@launch
                }

                isPlaying = true
                playRecording(databaseClient.decompressString(message.getString("content")))
            }
        }

        messageList.addView(messageLayout)
    }
    private suspend fun addMessageToDB(newMessage: JSONObject){

        databaseClient.addToMessages(currentRoom.getString("code"), newMessage)
    }
    private suspend fun addVoiceMessageToDB(newVoiceMessage: JSONObject){

        databaseClient.addToMessages(currentRoom.getString("code"), newVoiceMessage)
    }
    private suspend fun clearRoomMessages(){

        databaseClient.updateOne("Rooms", JSONObject().put("code", GlobalVars.currentRoomCode), JSONObject().put("messages", JSONArray()))
    }
    private suspend fun updateCurrentRoom(){

        currentRoom = databaseClient
            .findOne("Rooms", JSONObject()
                .put("code", GlobalVars.currentRoomCode))
    }
    private fun syncMessages(newMessages: JSONArray){

        for(i in localMessages.length() until newMessages.length()){

            val newMessage = newMessages.getJSONObject(i)
            val messageText = if(newMessage.getBoolean("is_text")) newMessage.getString("content") else "VOICE MESSAGE"

            if(appInBackground()) sendNotification(newMessage.getString("username"), messageText)

            addMessageToScrollView(newMessage)
            scrollToBottom()
        }

        localMessages = newMessages // update the local messages
    }
    private fun sendNotification(title: String, message: String) {

        val channelId = "my_channel_id"
        val notificationId = Random().nextInt()

        // Create an intent for when the user taps the notification
//        val intent = Intent(this, ChatRoom::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Create a notification builder
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_email_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set the priority to high
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Set vibration and sound
        val vibrate = longArrayOf(1000, 1000, 1000, 1000, 1000)
//        builder.setVibration(vibrate)
//        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        // Create a notification manager and show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, builder.build())
    }
    private fun appInBackground(): Boolean {

        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in appProcess.pkgList) {
                    if (activeProcess == this.packageName) {
                        // App is running in the foreground
                        return false
                    }
                }
            }
        }
        // App is running in the background
        return true
    }
    private fun connectionDropped(){

        if (disconnected) return
        disconnected = true
        // this function needs to run only on first disconnection

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection Failure")
        builder.setMessage("Your internet connection dropped.\nPlease join back in.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish()
                startActivity(Intent(this, HomePage::class.java))
            }

        val alert = builder.create()
        alert.show()
    }
    private fun timestampToString(epochTime: Long): String {

        val now = System.currentTimeMillis() / 1000
        val timeDifference = now - epochTime

        return when {
            timeDifference < 60 -> "Just now"
            timeDifference < 60 * 60 -> {
                val minutes = timeDifference / 60
                "${minutes}m ago"
            }
            timeDifference < 24 * 60 * 60 -> {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Yesterday at ${sdf.format(Date(epochTime * 1000))}"
            }
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                sdf.format(Date(epochTime * 1000))
            }
        }
    }
    private fun currentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }
    private fun showMessageBox(title: String, message: String) {

        // Shows message with OK button
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
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
    private fun startRecordingMode(){

        isRecording = true
        messageInput.isEnabled = false
        micIcon.setBackgroundResource(R.drawable.red_square)
    }
    private fun endRecordingMode(){

        isRecording = false
        messageInput.isEnabled = true
        micIcon.setBackgroundResource(R.drawable.baseline_mic_24)
    }
    private fun startSendingMode(){

        showSendButton()

        sendButton.isEnabled = false
        sendButton.text = "Sending..."
        sendButton.setBackgroundResource(R.drawable.grey_btn_bg)
    }
    private fun endSendingMode(){

        sendButton.text = "Send"
        sendButton.setBackgroundResource(R.drawable.normal_btn_bg)

        if(messageInput.text.isEmpty()) hideSendButton()
    }
    private fun hideSendButton(){

        sendButton.isVisible = false
        sendButton.isEnabled = false

        micIcon.isVisible = true
        micIcon.isEnabled = true
    }
    private fun showSendButton(){

        sendButton.isVisible = true
        sendButton.isEnabled = true

        micIcon.isVisible = false
        micIcon.isEnabled = false
    }
    private fun getRandomColor(username: String): Int {

        val random = Random(username.hashCode().toLong() - GlobalVars.currentRoomCode.hashCode().toLong())
        val hue = random.nextInt(360)
        val saturation = 0.7f + random.nextFloat() * 0.4f
        val brightness = 0.6f + random.nextFloat() * 0.4f

        return Color.HSVToColor(floatArrayOf(hue.toFloat(), saturation, brightness))
    }
    private fun getRandomColor(): Int {

        val random = Random()
        val hue = random.nextInt(360)
        val saturation = random.nextInt(41) + 60
        val brightness = random.nextInt(41) + 20
        return Color.HSVToColor(floatArrayOf(hue.toFloat(), saturation.toFloat() / 100, brightness.toFloat() / 100))
    }
    private suspend fun recordAndSave() : MutableList<Short> {

        val buffer: MutableList<Short> = mutableListOf()

        val shortAudioArray = ShortArray(intBufferSize)

        // Ask for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            deferred.await() // we suspend the activity till the user answer
        }

        val audioRecord = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, intBufferSize)
        audioRecord.startRecording()

        while (isRecording) {

            // we read the bytes captured by audioRecord and save them in SHORT FORMAT inside shortAudioData (not bytes)
            audioRecord.read(shortAudioArray, 0, shortAudioArray.size)

            // we add all shortAudioData inside a buffer
            for (element in shortAudioArray) {
                buffer.add(element)
            }
        }

        audioRecord.stop()
        audioRecord.release()

        return buffer
    }
    private suspend fun playRecording(shorts: MutableList<Short>){

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA) //to be on speaker
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(intRecordSampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(intBufferSize)
            .build()

        audioTrack.playbackRate = intRecordSampleRate
        audioTrack.play()

        val array = shorts.toShortArray()

        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {

            println("Attempting to play recording...")
            audioTrack.write(array, 0, array.size)
        }

        isPlaying = false
    }
    @Override
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //to continue the code when the user check permission
                    deferred.complete(true)

                } else {
//                    buttonStop()
                    showMessageBox("Access Denied", "You denied access to the microphone")
                }
            }
        }
    }
}