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
    private lateinit var middleButton: Button
    private lateinit var lowerButton: Button
    private lateinit var codeInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        upperButton = findViewById(R.id.upperButton)
        middleButton = findViewById(R.id.middleButton)
        lowerButton = findViewById(R.id.lowerButton)

        codeInput = findViewById(R.id.codeInput)

        upperButton.setOnClickListener{
            setJoinState()
        }
        middleButton.setOnClickListener{
            createRoom()
        }
        lowerButton.setOnClickListener{

            setNormalState()
        }
    }
    private fun setNormalState(){

        dismissKeyboard()
        upperButton.isVisible = true
        lowerButton.isVisible = false

        codeInput.isVisible = false
        codeInput.text.clear()

        middleButton.text = "Create a channel"
        middleButton.setBackgroundResource(R.drawable.normal_btn_bg)

        middleButton.setOnClickListener{
            createRoom()
        }
    }
    private fun setJoinState(){

        upperButton.isVisible = false
        codeInput.isVisible = true
        lowerButton.isVisible = true

        codeInput.text.clear()

        middleButton.text = "Join"
        middleButton.setBackgroundResource(R.drawable.green_btn_bg)

        middleButton.setOnClickListener{
            joinRoom(codeInput.text.toString().lowercase())
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