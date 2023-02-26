package com.example.androidstudioproject

//import ForTextChannelBtn
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class HomePage : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var btnJoinChannel: Button
    private lateinit var btnCreateChannel: Button
//    private lateinit var btnSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        mAuth = FirebaseAuth.getInstance()
//        btnSignOut = findViewById(R.id.btnSignOut)
        btnCreateChannel = findViewById(R.id.btnCreateChannel)


// Set an onClickListener to sign out the user when the button is clicked
//        ZZZ

    }

    fun showPopupjn(view: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popupjoin, null)

        val popupWidth = ViewGroup.LayoutParams.MATCH_PARENT
        val popupHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)

        // Set the background color of the popup to white
        val popupBg = ContextCompat.getDrawable(this, R.drawable.popup_bg)
        popup.setBackgroundDrawable(popupBg)

        val btnjoin = popupView.findViewById<Button>(R.id.btnjoin)


        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
    }
    fun showPopupcr(view: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup, null)

        val popupWidth = ViewGroup.LayoutParams.MATCH_PARENT
        val popupHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)

        // Set the background color of the popup to white
        val popupBg = ContextCompat.getDrawable(this, R.drawable.popup_bg)
        popup.setBackgroundDrawable(popupBg)

        val btnTextChannel = popupView.findViewById<Button>(R.id.btnTextChannel)
        val btnVoiceChannel = popupView.findViewById<Button>(R.id.btnVoiceChannel)

        btnTextChannel.setOnClickListener {
            // Code to handle button1 click
            val intent = Intent(this, ForTextChannelBtn::class.java)
            startActivity(intent)        }

        btnVoiceChannel.setOnClickListener {
            // Code to handle button2 click
            popup.dismiss()
        }

        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            //write the logic for logout
            mAuth.signOut()
            val intent=Intent(this@HomePage,Login::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true
    }

}

