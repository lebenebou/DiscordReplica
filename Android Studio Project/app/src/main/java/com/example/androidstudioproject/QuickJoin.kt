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
import org.json.JSONObject

class QuickJoin : AppCompatActivity() {

    private lateinit var codeInput: EditText
    private lateinit var joinButton: Button
    private lateinit var backButton: Button

    private val databaseClient = MongoClient()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_join)

        codeInput = findViewById(R.id.codeInput)
        joinButton = findViewById(R.id.joinButton)
        backButton = findViewById(R.id.backButton)

        joinButton.setOnClickListener{

            val givenCode = codeInput.text.toString().trim().uppercase()
            if(givenCode.isEmpty()) return@setOnClickListener showMessageBox("No Code Provided","Please provide a code.")
            if(givenCode.length != 6) return@setOnClickListener showMessageBox("Invalid Code","This code is invalid.")
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

        backButton.setOnClickListener {
            onBackPressed()
        }
    }
    private fun handleJoinAttempt(roomResult: JSONObject){

        if(roomResult.length()==0) return showMessageBox("Room Not Found","This code doesn't match any open rooms.")

        // room exists
        GlobalVars.currentRoomCode = roomResult.getString("code")
        startActivity(Intent(this, ChatRoom::class.java))
    }
    private fun connectionDropped(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection Failure")
        builder.setMessage("Your internet connection dropped.\nPlease try again.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                endLoadingMode()
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
        joinButton.isEnabled = false
        backButton.isEnabled = false

        joinButton.setBackgroundResource(R.drawable.grey_btn_bg)
        joinButton.text = "Joining..."

        dismissKeyboard()
    }
    private fun endLoadingMode() {

        codeInput.isEnabled = true
        joinButton.isEnabled = true
        backButton.isEnabled = true

        joinButton.setBackgroundResource(R.drawable.green_btn_bg)
        joinButton.text = "QuickJoin"
    }
    private fun dismissKeyboard(){

        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}