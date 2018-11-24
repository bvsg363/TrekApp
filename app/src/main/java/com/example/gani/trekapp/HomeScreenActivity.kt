package com.example.gani.trekapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.JsonReader
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.app_bar_home_screen.*
import kotlinx.android.synthetic.main.content_home_screen.*
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_past_treks.*
import org.json.JSONArray
import org.json.JSONObject


class HomeScreenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val treksList : ArrayList<Pair<Int, String>> = ArrayList()
    val displayList : ArrayList<Pair<Int, String>> = ArrayList()
    var searchView : SearchView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        setSupportActionBar(toolbar)

        title = "Treks Available"

        setViews()
        getTreks()
    }

    fun setViews(){

        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = TreksListAdapter(treksList, this)


        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById(R.id.nav_profile_username) as TextView
        navUsername.text = sharedPreferences.getString("username", getString(R.string.nav_header_title))

        val navUsermail = headerView.findViewById(R.id.nav_profile_mail) as TextView
        navUsermail.text = sharedPreferences.getString("email", getString(R.string.nav_header_subtitle))

        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = TreksListAdapter(treksList, this)

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_screen, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menu_search -> {
                searchView = item.actionView as SearchView

                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {

                        treksList.clear()

                        if (newText!!.isNotEmpty()){

                            val search = newText.toLowerCase()
                            displayList.forEach {
                                if(it.second.toLowerCase().contains(search)){
                                    treksList.add(it)
                                }
                            }
                        }
                        else{
                            treksList.addAll(displayList)
                        }

                        recycler_view.adapter.notifyDataSetChanged()

                        return  true
                    }

                })
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        when (item.itemId) {
            R.id.nav_past_treks -> {

                item.isChecked = !item.isChecked
                // Handle the camera action
                getPastTreks()
            }

            R.id.nav_offline_treks -> {
                item.isChecked = !item.isChecked
            }

            R.id.nav_logout -> {
                item.isChecked = !item.isChecked
                logout()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun getPastTreks(){
        val intent = Intent(this, PastTreks::class.java)
        startActivity(intent)
    }


    fun logout() {
        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

        sharedPreferences.edit().clear().apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun getTreks(){

        val url = GlobalVariables().treksUrl
        val requestQueue = Volley.newRequestQueue(this)

        val jsonRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject>{ response ->

            print(response)
            Log.i("PastTreks", response.getString("status"))

            if (response.getString("status") == "true"){
//                Toast.makeText(this, "Success getting treks", Toast.LENGTH_SHORT).show()
                saveTreks(response)
                displayTreks(response)
            }
            else{

                val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

//                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                if (sharedPreferences.contains("treksAvailable")){
                    displayTreks(JSONObject(sharedPreferences.getString("treksAvailable", "")))
                }

                Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show()
            }

        }, Response.ErrorListener {
            val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

            Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()

            if (sharedPreferences.contains("treksAvailable")){
                displayTreks(JSONObject(sharedPreferences.getString("treksAvailable", "")))
            }
        })

        requestQueue.add(jsonRequest)

    }

    fun displayTreks(response: JSONObject){

        val trekData: JSONArray? = response.getJSONArray("data")

        for (i in 0..(trekData?.length()!!.minus(1))){
            val trek = trekData.getJSONObject(i)
            treksList.add(Pair(trek.getInt("trek_id"), trek.getString("name")))
        }

        displayList.addAll(treksList)
        recycler_view.adapter.notifyDataSetChanged()

        (recycler_view.adapter as TreksListAdapter).onItemClick = {
            str ->
            val intent = Intent(this, DownloadMap::class.java)

//            searchView?.setQuery("", false)
//            searchView?.clearFocus()
//            searchView?.onActionViewCollapsed()


            //val intent = Intent(this, TrekActivity::class.java)
            //intent.putExtra("trekName", str)
            val intent = Intent(this, DownloadMap::class.java)
            intent.putExtra("trekName", str)
            startActivity(intent)
        }
    }

    fun saveTreks(response: JSONObject){
        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
        val sharedPrefEditor =  sharedPreferences.edit()

        sharedPrefEditor.putString("treksAvailable", response.toString())

        sharedPrefEditor.apply()
    }
}
