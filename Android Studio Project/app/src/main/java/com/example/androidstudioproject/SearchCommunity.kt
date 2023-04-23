package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.runBlocking

class SearchCommunity : AppCompatActivity() {
    private lateinit var searchView:SearchView
    private lateinit var searchButton:Button
    private val databaseClient = MongoClient()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_community)
        searchView = findViewById(R.id.searchView)
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val query = searchView.query.toString()
            // Do something with the query, such as perform a search
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val searchResults = runBlocking {
                        
                        databaseClient.getSearchResults("Communities", "name", query)
                    }
                    // Do something with the search results, such as display them in a RecyclerView
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
}