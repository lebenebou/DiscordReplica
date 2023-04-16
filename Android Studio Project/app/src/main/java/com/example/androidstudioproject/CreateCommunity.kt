package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class CreateCommunity : AppCompatActivity() {

    private lateinit var communityNameInput: EditText
    private lateinit var descInput: EditText

    private lateinit var createComButton: Button
    private lateinit var goBackButton: Button

    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_community)

        communityNameInput = findViewById(R.id.communityNameInput)
        descInput = findViewById(R.id.descInput)

        createComButton = findViewById(R.id.createComButton)
        goBackButton = findViewById(R.id.goBackButton)

        goBackButton.setOnClickListener{
            onBackPressed()
        }
        createComButton.setOnClickListener{

            val communityName = communityNameInput.text.toString().trim()
            val desc = descInput.text.toString().trim()

            if(communityName.isEmpty()) return@setOnClickListener showMessageBox("Empty Name","A community can't have an empty name.")
            if(desc.isEmpty()) return@setOnClickListener showMessageBox("Empty Description","A community can't have an empty description.")

            GlobalScope.launch {

                runOnUiThread{ startLoadingMode() }

                try {
                    createCommunity(communityName, desc)
                    startActivity(Intent(this@CreateCommunity, Community::class.java))

                    runOnUiThread{ endLoadingMode() }
                }
                catch (e: Exception){
                    runOnUiThread{ connectionDropped() }
                }
            }
        }
    }
    private suspend fun createCommunity(name: String, desc: String){

        var newCode = generateRandomCode() // 6 random letters/numbers
        var response = JSONObject()

        while(response.length() > 0){ // keep changing code until it is unique

            newCode = generateRandomCode()
            response = databaseClient.findOne("Communities", JSONObject().put("code", newCode))
        }

        GlobalVars.currentCommunityCode = newCode
        databaseClient.addToArray("Users", JSONObject().put("username", GlobalVars.currentUser), "communities", newCode)

        val newRoom = JSONObject().apply {

            put("code", newCode)
            put("name", name)
            put("description", desc)
            put("creator", GlobalVars.currentUser)
            put("users", JSONArray().put(GlobalVars.currentUser)) // list of just the creator's name for now
            put("rooms", JSONArray()) // empty list of rooms
        }
        databaseClient.insertOne("Communities", newRoom)
    }
    private fun showMessageBox(title: String, message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, _ ->

            endLoadingMode()
            dialog.dismiss()
        }
        builder.show()
    }
    private fun connectionDropped(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection Failure")
        builder.setMessage("Your internet connection dropped.\n\nYou are being redirected to the home screen.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish()
                startActivity(Intent(this, HomePage::class.java))
            }

        val alert = builder.create()
        alert.show()
    }
    private fun generateRandomCode(): String {

        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
    private fun startLoadingMode(){

        communityNameInput.isEnabled = false
        descInput.isEnabled = false
        goBackButton.isEnabled = false
        createComButton.isEnabled = false

        createComButton.setBackgroundResource(R.drawable.grey_btn_bg)
        createComButton.text = "Creating..."

        dismissKeyboard()
    }
    private fun endLoadingMode() {

        communityNameInput.isEnabled = true
        descInput.isEnabled = true
        goBackButton.isEnabled = true
        createComButton.isEnabled = true

        createComButton.setBackgroundResource(R.drawable.green_btn_bg)
        createComButton.text = "Create Community"
    }
    private fun dismissKeyboard(){

        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}