package com.example.androidstudioproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
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
                    return true
                }

                GlobalScope.launch {

                    runOnUiThread{
                        searchResultsLayout.removeAllViews()
                        startLoadingMode()
                    }

                    var searchResults = JSONArray()

                    try {
                        searchResults = databaseClient.getSearchResults("Communities", "name", query)
                    }
                    catch(e : Exception){
                        runOnUiThread { connectionDropped() }
                    }


                    runOnUiThread{
                        searchResultsLayout.removeAllViews()
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
    private fun connectionDropped(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection Failure")
        builder.setMessage("Your internet connection dropped.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->

            }

        val alert = builder.create()
        alert.show()
    }
    private fun startLoadingMode(){

        val context = nocomm.context
        val noResultsText = TextView(context)
        noResultsText.text = "Fetching..."
        noResultsText.setTextColor(Color.WHITE)
        noResultsText.gravity = Gravity.CENTER
        noResultsText.setPadding(0, 250, 0, 0)
        noResultsText.textSize = 15F
        noResultsText.typeface = ResourcesCompat.getFont(context, R.font.montserratextrabold)

        searchResultsLayout.addView(noResultsText)
    }

    private fun displaySearchResults(searchResults: JSONArray) {

        if (searchResults.length() == 0) {

            val context = nocomm.context
            val noResultsText = TextView(context)
            noResultsText.text = "Your search did not match any communities."
            noResultsText.setTextColor(Color.WHITE)
            noResultsText.gravity = Gravity.CENTER
            noResultsText.setPadding(0, 250, 0, 0)
            noResultsText.textSize = 15F
            noResultsText.typeface = ResourcesCompat.getFont(context, R.font.montserratextrabold)
            searchResultsLayout.addView(noResultsText)
            return
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

            val joinedUsers = community.getJSONArray("users")
            for(i in 0 until joinedUsers.length()){

                if(joinedUsers.getString(i) == GlobalVars.currentUser){

                    askToRedirect(community)
                    return@setOnClickListener
                }
            }
            askToJoin(community)
        }

        val communityName = TextView(context)
        communityName.text = community.getString("name")
        communityName.setTextColor(Color.WHITE)
        communityName.typeface = ResourcesCompat.getFont(context, R.font.montserratextrabold)
        communityName.gravity = Gravity.CENTER
        communityName.textSize = 20f
        communityName.setPadding(20, 10, 0, 0)

        linearLayout.addView(communityName)

        val description = TextView(context)
        description.text = "Description: " + community.getString("description")
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
    private fun askToJoin(community: JSONObject){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Join " + community.getString("name"))

        var message = ""
        message += "Do you wish to join this community?\n\n"
        message += "This community was created by " + community.getString("creator") + "\n\n"
        message += "Members: " + community.getJSONArray("users").length() + "\n\n"
        message += "Description: " + community.getString("description")

        builder.setMessage(message)

        builder.setPositiveButton("Yes") { _, _ ->

            GlobalScope.launch {

                try {
                    join(community)
                    finish()

                    GlobalVars.currentCommunityCode = community.getString("code")
                    startActivity(Intent(this@SearchCommunity, Community::class.java))
                }
                catch(e: Exception){
                    runOnUiThread { connectionDropped() }
                }
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    private suspend fun join(community: JSONObject){

        databaseClient.addToArray("Users", JSONObject().put("username", GlobalVars.currentUser), "communities", community.getString("code"))
        databaseClient.addToArray("Communities", JSONObject().put("code", community.getString("code")), "users", GlobalVars.currentUser)
    }

    private fun askToRedirect(community: JSONObject){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Go to " + community.getString("name"))
        builder.setMessage("You are already a member in ${community.getString("name")}.\n\nDo you wish to browse its open rooms?")

        builder.setPositiveButton("Yes") { _, _ ->

            GlobalVars.currentCommunityCode = community.getString("code")
            startActivity(Intent(this@SearchCommunity, Community::class.java))
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }


}