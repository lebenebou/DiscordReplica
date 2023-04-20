package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RecordActivity : AppCompatActivity() {
    private lateinit var playbutton: Button
    private lateinit var startbutton: Button
    private lateinit var stopbutton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        playbutton = findViewById(R.id.playbutton)
        startbutton = findViewById(R.id.startbutton)
        stopbutton = findViewById(R.id.stopbutton)
        playbutton.setOnClickListener {
            println("Play was pressed")
        }
        startbutton.setOnClickListener {
            println("Start was pressed")
        }
        stopbutton.setOnClickListener {
            println("stop was pressed")
        }

    }
}