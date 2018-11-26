package com.example.gani.trekapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class PlaceInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_info)

        title = intent.getStringExtra("place")
    }
}
