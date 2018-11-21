package com.example.gani.trekapp

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_past_treks.*
import kotlinx.android.synthetic.main.content_home_screen.*
import org.json.JSONArray
import org.json.JSONObject

class PastTreks : AppCompatActivity() {

    val treksList : ArrayList<Pair<Int, String>> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_treks)

        title = "Past treks"

        past_treks_view.layoutManager = LinearLayoutManager(this)
        past_treks_view.adapter = TreksListAdapter(treksList, this)

        (recycler_view.adapter as TreksListAdapter).onItemClick = {
            index ->
            val intent = Intent(this, TrekActivity::class.java)
            intent.putExtra("trekId", index)
            startActivity(intent)
        }

        getPastTreks()

    }

    fun getPastTreks(){

        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

        val url = GlobalVariables().pastTreksUrl
        val uid = sharedPreferences.getString("uid", "")
        val requestQueue = Volley.newRequestQueue(this)

        val finalUrl = "$url?uid=$uid"

        val jsonRequest = JsonObjectRequest(Request.Method.GET, finalUrl, null, Response.Listener<JSONObject>{ response ->

            print(response)
            Log.i("PastTreks", response.getString("status"))

            if (response.getString("status") == "true"){
                Toast.makeText(this, "Success fetching past treks", Toast.LENGTH_SHORT).show()
                displayPastTreks(response)
            }
            else{
//                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()

            }

        }, Response.ErrorListener {
            Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()
        })

        requestQueue.add(jsonRequest)
    }

    fun displayPastTreks(response: JSONObject){

        val trekData: JSONArray? = response.getJSONArray("data")

        for (i in 0..(trekData?.length()!!.minus(1))){
            val trek = trekData.getJSONObject(i)
            treksList.add(Pair(trek.getInt("trek_id"), trek.getString("name")))
        }
        past_treks_view.adapter.notifyDataSetChanged()
    }
}
