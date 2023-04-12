package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class CreateCommunity : AppCompatActivity() {
    private lateinit var communityNameInput: EditText
    private lateinit var descInput: EditText
    private lateinit var createComButton: Button
    private lateinit var goBackButton: Button
    private val databaseClient=MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_community)
        communityNameInput=findViewById(R.id.communityNameInput)
        descInput=findViewById(R.id.descInput)
        createComButton=findViewById(R.id.createComButton)
        goBackButton=findViewById(R.id.goBackButton)
    }
}