package com.example.androidstudioproject

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text


class HomePage : AppCompatActivity() {

    private lateinit var quickJoinButton: Button
    private lateinit var createCommunityButton: Button
    private lateinit var searchButton: Button
    private lateinit var scrollView: ScrollView
    private lateinit var communitiesLayout: LinearLayout

    private val databaseClient = MongoClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        quickJoinButton = findViewById(R.id.quickJoinButton)
        createCommunityButton = findViewById(R.id.createCommunityButton)
        searchButton = findViewById(R.id.searchButton)
        scrollView = findViewById(R.id.scrollView2)
        communitiesLayout= findViewById(R.id.communitiesLayout)

        quickJoinButton.setOnClickListener{
            startActivity(Intent(this, QuickJoin::class.java))
        }
        createCommunityButton.setOnClickListener{
            startActivity(Intent(this, CreateCommunity::class.java))
        }
        searchButton.setOnClickListener{
            startActivity(Intent(this, SearchCommunity::class.java))
        }

        GlobalScope.launch {

            var userInfo = JSONObject()

            try {
                userInfo = databaseClient.findOne("Users", JSONObject().put("username", GlobalVars.currentUser))
            }
            catch (e : Exception){
                runOnUiThread { connectionDropped() }
                return@launch
            }

            val communityCodes = userInfo.getJSONArray("communities")
            var joinedCommunities = JSONArray()

            try {
                joinedCommunities = databaseClient.findMultiple("Communities", JSONObject()
                    .put("code", JSONObject()
                        .put("\$in", communityCodes)))
            }
            catch (e : java.lang.Exception){
                runOnUiThread { connectionDropped() }
                return@launch
            }
            runOnUiThread{ syncScrollView(joinedCommunities) }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){ // user pressed back <-

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logging Out")
        builder.setMessage("Are you sure you wish to log out?")

        builder.setPositiveButton("Yes") { _, _ ->
            logout()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // Sign Out
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.home_page_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu_logout -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun logout(){

        // Remove user credentials from shared preferences
        getSharedPreferences("user_credentials", Context.MODE_PRIVATE).edit().clear().apply()
        // println("Credentials have been removed")

        GlobalVars.currentUser = "not_logged_in"
        GlobalVars.currentRoomCode = "000000"
        GlobalVars.currentCommunityCode = "000000"

        finish()
        startActivity(Intent(this@HomePage, Login::class.java))
    }
    private fun addCommunityToScrollView(community: JSONObject) {

        val context = scrollView.context

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
        communityName.typeface = ResourcesCompat.getFont(context, R.font.montserratextrabold)
        communityName.gravity = Gravity.CENTER
        communityName.textSize = 15f
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

        communitiesLayout.addView(linearLayout)
    }

    private fun syncScrollView(communityResults: JSONArray){

        communitiesLayout.removeAllViews()

        if (communityResults.length() == 0) {

            val noResultsText = TextView(this)
            noResultsText.text = "Looks like you haven't joined\nany communities yet.\n\nSearch for communities\nor create your own below!"
            noResultsText.setTextColor(Color.WHITE)
            noResultsText.gravity = Gravity.CENTER
            noResultsText.setPadding(0, 250, 0, 0)
            noResultsText.textSize = 15F
            noResultsText.typeface = ResourcesCompat.getFont(this, R.font.montserratextrabold)
            communitiesLayout.addView(noResultsText)
            return
        }

        for(i in 0 until communityResults.length()){

            addCommunityToScrollView(communityResults.getJSONObject(i))
        }
    }
    private fun connectionDropped(){

//        communitiesLayout.removeAllViews()

        val noResultsText = findViewById<TextView>(R.id.noCommunitiesText)
        noResultsText.text = "There was an error fetching\nyour communities.\n\nMake sure you have an active\ninternet connection."
        noResultsText.setTextColor(Color.WHITE)
        noResultsText.gravity = Gravity.CENTER
        noResultsText.setPadding(0, 250, 0, 0)
        noResultsText.textSize = 15F
        noResultsText.typeface = ResourcesCompat.getFont(this, R.font.montserratextrabold)
        return
    }
}