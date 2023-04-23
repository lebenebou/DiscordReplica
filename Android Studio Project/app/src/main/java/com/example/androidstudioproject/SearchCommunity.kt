package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray

class SearchCommunity : AppCompatActivity() {
    private lateinit var searchView:SearchView
//    private lateinit var searchButton:Button
    private lateinit var search_results_layout:LinearLayout
    private lateinit var name_text_view:TextView
    private lateinit var description_text_view:TextView
    private lateinit var search_results_scroll_view:ScrollView

    private val databaseClient = MongoClient()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_community)
        searchView = findViewById(R.id.searchView)
//        searchButton = findViewById(R.id.searchButton)
        search_results_layout = findViewById(R.id.search_results_layout)
        name_text_view = findViewById(R.id.name_text_view)
        description_text_view = findViewById(R.id.description_text_view)
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
                    val searchResults = databaseClient.getSearchResults("Communities", "name", query)


                    runOnUiThread{
                        createSearchResultViews(searchResults, search_results_layout)
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

    fun createSearchResultViews(searchResults: JSONArray, searchResultsLayout: LinearLayout) {
        for (i in 0 until searchResults.length()) {
            val community = searchResults.getJSONObject(i)
            val name = community.getString("name")
            val description = community.getString("description")
            val searchResultView = LayoutInflater.from(this@SearchCommunity).inflate(R.layout.activity_search_community, searchResultsLayout, false)
            val nameTextView = searchResultView.findViewById<TextView>(R.id.name_text_view)
            val descriptionTextView = searchResultView.findViewById<TextView>(R.id.description_text_view)
            nameTextView.text = name
            descriptionTextView.text = description
            searchResultsLayout.addView(searchResultView)
        }
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