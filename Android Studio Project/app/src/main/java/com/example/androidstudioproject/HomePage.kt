package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class HomePage : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var btnJoinChannel: Button
    private lateinit var btnCreateChannel: Button
    private lateinit var btnSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        mAuth = FirebaseAuth.getInstance()
        btnSignOut = findViewById(R.id.btnSignOut)
        btnCreateChannel = findViewById(R.id.btnCreateChannel)


// Set an onClickListener to sign out the user when the button is clicked
        btnSignOut.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this@HomePage, Login::class.java)
            startActivity(intent)
            finish()
        }
    }


    fun showPopup(view: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup, null)

        val popupWidth = ViewGroup.LayoutParams.MATCH_PARENT
        val popupHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)

        // Set the background color of the popup to white
        popup.setBackgroundDrawable(ColorDrawable(Color.BLUE))

        val btnTextChannel = popupView.findViewById<Button>(R.id.btnTextChannel)
        val btnVoiceChannel = popupView.findViewById<Button>(R.id.btnVoiceChannel)

        btnTextChannel.setOnClickListener {
            // Code to handle button1 click
            popup.dismiss()
        }

        btnVoiceChannel.setOnClickListener {
            // Code to handle button2 click
            popup.dismiss()
        }

        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
    }



}

