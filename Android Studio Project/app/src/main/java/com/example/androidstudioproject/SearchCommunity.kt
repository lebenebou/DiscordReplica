package com.example.androidstudioproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SearchCommunity : AppCompatActivity() {
    private lateinit var searchView:SearchView
//    private lateinit var searchButton:Button
    private lateinit var search_results_layout:LinearLayout
    private lateinit var search_results_scroll_view:ScrollView

    private val databaseClient = MongoClient()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_community)
        searchView = findViewById(R.id.searchView)
//        searchButton = findViewById(R.id.searchButton)
        search_results_layout = findViewById(R.id.search_results_layout)

        search_results_scroll_view=findViewById(R.id.search_results_scroll_view)
//        searchButton.setOnClickListener {
//            val query = searchView.query.toString()
//        }



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query==null){
                    showMessageBox("empty Query","cannot be empty")
                    return true ;
                }

                GlobalScope.launch {
                    runOnUiThread{
                        search_results_layout.removeAllViews()
                    }
                    val searchResults = databaseClient.getSearchResults("Communities", "name", query)
                    runOnUiThread{
                        displaySearchResults(searchResults)
                    }
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the search text changes
                // You can use this method to update your search results as the user types
                return true
            }
        })

    }

    fun processSearchResults(searchResults: JSONArray) {
        for (i in 0 until searchResults.length()) {
            val community = searchResults.getJSONObject(i)
            val name = community.getString("name")
            val description = community.getString("description")
            // Do something with name and description
            println("Name: $name, Description: $description")
        }
    }

    fun displaySearchResults(searchResults: JSONArray) {
        for (i in 0 until searchResults.length()) {
            addCommunityToScrollView(searchResults.getJSONObject(i))
        }
    }


    private fun addCommunityToScrollView(community: JSONObject) {

        val context = search_results_scroll_view.context

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)

        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setBackgroundResource(R.drawable.message_rectangle)
        linearLayout.isClickable = true

        linearLayout.setOnClickListener {
            GlobalVars.currentCommunityCode = community.getString("code")
            startActivity(Intent(this, Community::class.java))
        }

        val communityName = TextView(context)
        communityName.text = community.getString("name")
        communityName.setTextColor(Color.BLACK)
        communityName.setTypeface(null, Typeface.BOLD)
        communityName.textSize = 16f
        communityName.setPadding(20, 10, 0, 0)

        linearLayout.addView(communityName)

        val description = TextView(context)
        description.text = "Description:" + community.getString("description")
//        creatorName.setTextColor(getRandomColor(community.getString("creator")))
        description.textSize = 14f
        description.setPadding(20, 10, 0, 0)

        linearLayout.addView(description)

        val members = TextView(context)
        members.text = "Members: " + community.getJSONArray("users").length()
        members.textSize = 14f
        members.setPadding(20, 10, 0, 10)

        linearLayout.addView(members)

        search_results_layout.addView(linearLayout)
    }


    private fun showMessageBox(title: String, message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, _ ->
//            endLoadingMode()
            dialog.dismiss()
        }
        builder.show()
    }
}