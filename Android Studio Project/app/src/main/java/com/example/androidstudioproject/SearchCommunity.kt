package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SearchView

class SearchCommunity : AppCompatActivity() {
    private lateinit var searchView:SearchView
    private lateinit var searchButton:Button


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
                // This method is called when the user presses the search button
                // Do something with the search query here
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the search text changes
                // Do something with the new search text here
                return true
            }
        })
    }
}