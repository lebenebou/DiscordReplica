package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class HomePage : AppCompatActivity() {

    private lateinit var quickJoinButton: Button
    private lateinit var createCommunityButton: Button
    private lateinit var searchButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        quickJoinButton = findViewById(R.id.quickJoinButton)
        createCommunityButton = findViewById(R.id.createCommunityButton)
        searchButton = findViewById(R.id.searchButton)

        quickJoinButton.setOnClickListener{
            startActivity(Intent(this, QuickJoin::class.java))
        }
        createCommunityButton.setOnClickListener{
            startActivity(Intent(this, CreateCommunity::class.java))
        }
        searchButton.setOnClickListener{
            finish()
            startActivity(Intent(this, SearchCommunity::class.java))
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){ // user pressed back <-

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logging Out")
        builder.setMessage("Are you sure you wish to log out?")

        builder.setPositiveButton("Yes") { _, _ ->
            logout()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // Sign Out
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.home_page_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu_logout -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun logout(){

        // Remove user credentials from shared preferences
        getSharedPreferences("user_credentials", Context.MODE_PRIVATE).edit().clear().apply()
        // println("Credentials have been removed")

        GlobalVars.currentUser = "not_logged_in"
        GlobalVars.currentRoomCode = "000000"
        GlobalVars.currentCommunityCode = "000000"

        finish()
        startActivity(Intent(this@HomePage, Login::class.java))
    }
}