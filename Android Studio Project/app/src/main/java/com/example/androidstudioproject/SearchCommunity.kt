package com.example.androidstudioproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        }



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query==null){
                    showMessageBox("empty Query","cannot be empty")
                    return true ;
                }

                GlobalScope.launch {
                    val searchResults = databaseClient.getSearchResults("Communities", "name", query)
                    println(searchResults);
                    runOnUiThread{
                        //displayResults(searchReasults);
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