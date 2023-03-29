package com.example.androidstudioproject

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible

class HomePage : AppCompatActivity() {

    private lateinit var upperButton: Button
    private lateinit var lowerButton: Button
    private lateinit var codeInput: EditText
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
            joinRoom(codeInput.text.toString().lowercase())
        }

        lowerButton.text = "Cancel"
        lowerButton.setBackgroundResource(R.drawable.grey_btn_bg)
        lowerButton.setOnClickListener{
            setNormalState()
        }
    }
    private fun joinRoom(roomCode: String){

        if(true){

            showMessageBox("This room code doesn't match any open rooms.")
        }
    }
    private fun createRoom(){

        showMessageBox("This activity doesn't exist yet.")
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
    private fun dismissKeyboard(){

        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}