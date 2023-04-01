
package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private lateinit var descInput: EditText
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button

    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        nameInput = findViewById(R.id.nameInput)
        descInput = findViewById(R.id.descInput)
        createButton = findViewById(R.id.createButton)
        cancelButton = findViewById(R.id.cancelButton)

        cancelButton.setOnClickListener{
            startActivity(Intent(this, HomePage::class.java))
        }
        createButton.setOnClickListener{

            val roomName = nameInput.text.toString().trim()
            val roomDesc = descInput.text.toString().trim()

            if(roomName.isEmpty()) return@setOnClickListener showMessageBox("A room can't have an empty name.")
            if(roomDesc.isEmpty()) return@setOnClickListener showMessageBox("A room can't have an empty description")

            GlobalScope.launch {

                runOnUiThread{ startLoadingMode() }

                if(!databaseClient.isConnected(this@CreateRoom)){
                    runOnUiThread{ showMessageBox("Unable to connect.\nPlease make sure you have an active internet connection.")}
                    return@launch
                }

                createRoom(roomName, roomDesc)
                startActivity(Intent(this@CreateRoom, ChatRoom::class.java))

                runOnUiThread{ endLoadingMode() }
            }
        }
    }
    private suspend fun createRoom(name: String, desc: String){

        var newCode = generateRandomCode() // 6 random letters/numbers
        var response = JSONObject()

        while(response.length() > 0){ // keep changing code until it is unique

            newCode = generateRandomCode()
            response = databaseClient.findOne("Rooms", JSONObject().put("code", newCode))
        }

        GlobalVars.currentRoomCode = newCode

        val newRoom = JSONObject().apply {

            put("code", newCode)
            put("name", name)
            put("creator", GlobalVars.currentUser)
            put("description", desc)
            put("messages", JSONArray()) // empty list of messages
            put("active_users", JSONArray()) // empty list of messages
        }
        databaseClient.insertOne("Rooms", newRoom)
    }
    private fun showMessageBox(message: String) {

        // Shows message with OK button
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> }

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
        descInput.isEnabled = false
        cancelButton.isEnabled = false
        createButton.isEnabled = false

        createButton.setBackgroundResource(R.drawable.grey_btn_bg)
    }
    private fun endLoadingMode() {

        nameInput.isEnabled = true
        descInput.isEnabled = true
        cancelButton.isEnabled = true
        createButton.isEnabled = true

        createButton.setBackgroundResource(R.drawable.normal_btn_bg)
    }
}