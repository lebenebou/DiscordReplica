
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

            if(roomName.isEmpty()) return@setOnClickListener showMessageBox("Empty Name","A room can't have an empty name.")
            if(roomDesc.isEmpty()) return@setOnClickListener showMessageBox("Empty Description","A room can't have an empty description")

            GlobalScope.launch {

                runOnUiThread{ startLoadingMode() }

                try {
                    createRoom(roomName, roomDesc)
                    startActivity(Intent(this@CreateRoom, ChatRoom::class.java))

                    runOnUiThread{ endLoadingMode() }
                }
                catch (e: Exception){
                    runOnUiThread{ connectionDropped() }
                }
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

        createButton.setBackgroundResource(R.drawable.green_btn_bg)
    }
}