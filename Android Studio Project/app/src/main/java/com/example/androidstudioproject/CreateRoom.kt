
package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class CreateRoom : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var descInput: EditText
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button

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
            createRoom(nameInput.text.toString(), descInput.text.toString())
        }
    }

    private fun createRoom(name: String, desc: String){

        showMessageBox("This isn't implemented yet.")
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
}