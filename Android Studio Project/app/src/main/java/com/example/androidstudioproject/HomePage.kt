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
import androidx.core.view.isVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HomePage : AppCompatActivity() {

    private lateinit var upperButton: Button
    private lateinit var lowerButton: Button
    private lateinit var codeInput: EditText

    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        upperButton = findViewById(R.id.upperButton)
        lowerButton = findViewById(R.id.lowerButton)

        codeInput = findViewById(R.id.codeInput)

        upperButton.setOnClickListener{
            setJoinState()
        }
        lowerButton.setOnClickListener{
            createRoom()
        }
    }
    private fun setNormalState(){

        dismissKeyboard()

        codeInput.isVisible = false
        codeInput.text.clear()

        upperButton.text = "Join a room"
        upperButton.setBackgroundResource(R.drawable.normal_btn_bg)
        upperButton.setOnClickListener{
            setJoinState()
        }

        lowerButton.text = "Create a room"
        lowerButton.setBackgroundResource(R.drawable.normal_btn_bg)
        lowerButton.setOnClickListener{
            createRoom()
        }
    }
    private fun setJoinState(){

        codeInput.isVisible = true

        codeInput.text.clear()

        upperButton.text = "Join"
        upperButton.setBackgroundResource(R.drawable.green_btn_bg)
        upperButton.setOnClickListener{

            val givenCode = codeInput.text.toString().trim().uppercase()
            if(givenCode.isEmpty()) return@setOnClickListener showMessageBox("No Code Provided","Please provide a code.")
            if(givenCode.length != 6) return@setOnClickListener showMessageBox("Invalid Code","This code is invalid")

            // code is valid, attempt to join room
            GlobalScope.launch {

                runOnUiThread{ startLoadingMode() }

                try {
                    val roomResult = databaseClient.findOne("Rooms", JSONObject().put("code", givenCode))
                    runOnUiThread{
                        handleJoinAttempt(roomResult)
                        endLoadingMode()
                    }
                }
                catch (e: Exception){
                    runOnUiThread{ connectionDropped() }
                }
            }
        }

        lowerButton.text = "Cancel"
        lowerButton.setBackgroundResource(R.drawable.grey_btn_bg)
        lowerButton.setOnClickListener{
            setNormalState()
        }
    }
    private fun handleJoinAttempt(roomResult: JSONObject){

        if(roomResult.length()==0) return showMessageBox("Room Not Found","This code doesn't match any open rooms.")

        // room exists
        GlobalVars.currentRoomCode = roomResult.getString("code")
        startActivity(Intent(this, ChatRoom::class.java))
    }
    private fun createRoom(){
        startActivity(Intent(this, CreateRoom::class.java))
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
    private fun startLoadingMode() {

        codeInput.isEnabled = false
        upperButton.isEnabled = false
        lowerButton.isEnabled = false

        upperButton.setBackgroundResource(R.drawable.grey_btn_bg)
    }
    private fun endLoadingMode() {

        codeInput.isEnabled = true
        upperButton.isEnabled = true
        lowerButton.isEnabled = true

        upperButton.setBackgroundResource(R.drawable.green_btn_bg)
    }
    private fun dismissKeyboard(){

        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}