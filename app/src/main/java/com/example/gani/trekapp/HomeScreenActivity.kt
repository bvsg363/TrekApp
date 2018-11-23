package com.example.gani.trekapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class HomeScreenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        setSupportActionBar(toolbar)

        title = "Treks Available"

        val treksList : ArrayList<Pair<Int, String>> = ArrayList()
        treksList.add(Pair(1, "Sameer Hill"))
        treksList.add(Pair(2, "Matheran"))
        treksList.add(Pair(3, "Mahabaleshwar"))

        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)



        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = TreksListAdapter(treksList, this)

        (recycler_view.adapter as TreksListAdapter).onItemClick = {
            str ->
            //val intent = Intent(this, TrekActivity::class.java)
            //intent.putExtra("trekName", str)
            //val intent = Intent(this, MapActivity::class.java)
            //val intent = Intent(this, mapBox::class.java)
            val intent = Intent(this, DownloadMap::class.java)
            startActivity(intent)
        }


        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById(R.id.nav_profile_username) as TextView
        navUsername.text = sharedPreferences.getString("username", getString(R.string.nav_header_title))

        val navUsermail = headerView.findViewById(R.id.nav_profile_mail) as TextView
        navUsermail.text = sharedPreferences.getString("email", getString(R.string.nav_header_subtitle))


//        nav_profile_mail.text = sharedPreferences.getString("email", getString(R.string.nav_header_subtitle))
//        nav_profile_username.text = sharedPreferences.getString("username", getString(R.string.nav_header_title))
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
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_past_treks -> {
                // Handle the camera action
                getPastTreks()
            }

            R.id.nav_offline_treks -> {

            }

            R.id.nav_logout -> {
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
}
