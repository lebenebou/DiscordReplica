package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

class HomePage : AppCompatActivity() {

    private lateinit var upperButton: Button
    private lateinit var lowerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        upperButton = findViewById(R.id.upperButton)
        lowerButton = findViewById(R.id.lowerButton)

        upperButton.setOnClickListener{
            startActivity(Intent(this, QuickJoin::class.java))
        }
        lowerButton.setOnClickListener{
            startActivity(Intent(this, CreateRoom::class.java))
        }
    }

    // Sign Out
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.home_page_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu_logout -> {
                val intent=Intent(this@HomePage,Login::class.java)
                finish()
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }
}