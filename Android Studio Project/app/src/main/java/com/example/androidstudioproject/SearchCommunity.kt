package com.example.androidstudioproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
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

    private lateinit var searchView : SearchView
    private lateinit var searchResultsLayout : LinearLayout
    private lateinit var searchResultsScrollView : ScrollView
    private lateinit var nocomm : TextView


    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_community)
        searchView = findViewById(R.id.searchView)
        searchResultsLayout = findViewById(R.id.search_results_layout)
        nocomm = findViewById(R.id.nocomm)


        searchResultsScrollView=findViewById(R.id.search_results_scroll_view)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query==null || query.isBlank()){
                    showMessageBox("empty Query","cannot be empty")
                    return true ;
                }

                GlobalScope.launch {
                    runOnUiThread{
                        searchResultsLayout.removeAllViews()
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
        searchView.queryHint = "Search"
        searchView.isIconified = false
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
    private fun displaySearchResults(searchResults: JSONArray) {

        if (searchResults.length() == 0) {
            val context = nocomm.context
            val nocomm = TextView(context)
            nocomm.text = "No results found."
            nocomm.setTextColor(Color.WHITE)
            nocomm.gravity = Gravity.CENTER
            nocomm.setPadding(0, 250, 0, 0)
            searchResultsLayout.addView(nocomm)
            return;
        }
        for (i in 0 until searchResults.length()) {
            addCommunityToScrollView(searchResults.getJSONObject(i))
        }
    }
    private fun addCommunityToScrollView(community: JSONObject) {

        val context = searchResultsScrollView.context

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)

        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setBackgroundResource(R.drawable.community_results)
        linearLayout.isClickable = true

        linearLayout.setOnClickListener {
            GlobalVars.currentCommunityCode = community.getString("code")
            startActivity(Intent(this, Community::class.java))
        }

        val communityName = TextView(context)
        communityName.text = community.getString("name")
        communityName.setTextColor(Color.WHITE)
        communityName.setTypeface(null, Typeface.BOLD)
        communityName.gravity = Gravity.CENTER
        communityName.textSize = 16f
        communityName.setPadding(20, 10, 0, 0)

        linearLayout.addView(communityName)

        val description = TextView(context)
        description.text = "Description:" + community.getString("description")
//        creatorName.setTextColor(getRandomColor(community.getString("creator")))
        description.setTextColor(Color.WHITE)
        description.textSize = 14f
        description.setPadding(20, 10, 0, 0)

        linearLayout.addView(description)

        val members = TextView(context)
        members.text = "Members: " + community.getJSONArray("users").length()
        members.textSize = 14f
        members.setPadding(20, 10, 0, 10)
        members.setTextColor(Color.WHITE)
        linearLayout.addView(members)

        searchResultsLayout.addView(linearLayout)
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