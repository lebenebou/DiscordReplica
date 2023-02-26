package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.*

class Join : AppCompatActivity() {
    private lateinit var edt_code: EditText
    private lateinit var btnjoin: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popupjoin)

        edt_code = findViewById(R.id.edt_code)
        btnjoin = findViewById(R.id.btnjoin)

        database = FirebaseDatabase.getInstance().reference

        btnjoin.setOnClickListener { joinChannel() }
    }
    data class Channel(
        val name: String = "",
        val description: String = "",
        val code: String = ""
    )

    private fun joinChannel() {
        val channelCode = edt_code.text.toString()
        database.child("channels").child(channelCode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val channel = dataSnapshot.getValue(Channel::class.java)
                    if (channel != null) {
                        // Redirect to the channel activity with the channel code
                        val intent = Intent(applicationContext, TextChannelCreated::class.java)
                        intent.putExtra("channelCode", channelCode)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Invalid Channel Code", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Invalid Channel Code", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("JoinChannelActivity", "onCancelled: " + databaseError.message)
            }
        })
    }
}
