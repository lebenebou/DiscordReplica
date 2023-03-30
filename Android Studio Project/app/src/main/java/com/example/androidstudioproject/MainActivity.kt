package com.example.androidstudioproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var accountsButton: Button
    private lateinit var chatroomsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accountsButton = findViewById(R.id.accountsButton)
        chatroomsButton = findViewById(R.id.chatroomsButton)

        accountsButton.setOnClickListener{
            startActivity(Intent(this, Login::class.java))
        }
        chatroomsButton.setOnClickListener{
            startActivity(Intent(this, HomePage::class.java))
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.logout){

            // logic for logout
            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent)
            return false
        }
        return false
    }
}