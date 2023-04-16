
package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class CreateRoom : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var createButton: Button
    private lateinit var backButton: Button

    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        nameInput = findViewById(R.id.nameInput)
        createButton = findViewById(R.id.createComButton)
        backButton = findViewById(R.id.goBackButton)

        backButton.setOnClickListener{
            onBackPressed()
        }
        createButton.setOnClickListener{

            val roomName = nameInput.text.toString().trim()

            if(roomName.isEmpty()) return@setOnClickListener showMessageBox("Empty Name","A room can't have an empty name.")

            GlobalScope.launch {

                runOnUiThread{ startLoadingMode() }

                try {
                    createRoom(roomName)
                    finish()
                    startActivity(Intent(this@CreateRoom, ChatRoom::class.java))

                    runOnUiThread{ endLoadingMode() }
                }
                catch (e: Exception){
                    runOnUiThread{ connectionDropped() }
                }
            }
        }
    }
    private suspend fun createRoom(name: String){

        var newCode = generateRandomCode() // 6 random letters/numbers
        var response = JSONObject()

        while(response.length() > 0){ // keep changing code until it is unique

            newCode = generateRandomCode()
            response = databaseClient.findOne("Rooms", JSONObject().put("code", newCode))
        }

        GlobalVars.currentRoomCode = newCode

        val newRoom = JSONObject().apply {

            put("code", newCode)
            put("community", GlobalVars.currentCommunityCode)
            put("name", name)
            put("creator", GlobalVars.currentUser)
            put("messages", JSONArray()) // empty list of messages
            put("active_users", JSONArray()) // empty list of online users
        }
        databaseClient.addToArray("Communities", JSONObject().put("code", GlobalVars.currentCommunityCode), "rooms", newCode)
        databaseClient.insertOne("Rooms", newRoom)
    }
    private fun showMessageBox(title: String, message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, _ ->

            endLoadingMode()
            dialog.dismiss()
        }
        builder.show()
    }
    private fun connectionDropped(){

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
    private fun generateRandomCode(): String {

        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
    private fun startLoadingMode(){

        nameInput.isEnabled = false
        backButton.isEnabled = false
        createButton.isEnabled = false
        createButton.text = "Creating..."

        createButton.setBackgroundResource(R.drawable.grey_btn_bg)
        dismissKeyboard()
    }
    private fun endLoadingMode() {

        nameInput.isEnabled = true
        backButton.isEnabled = true
        createButton.isEnabled = true
        createButton.text = "Create Room"

        createButton.setBackgroundResource(R.drawable.green_btn_bg)
    }
    private fun dismissKeyboard(){

        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}