package com.example.gani.trekapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class PlaceInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_info)

        Toast.makeText(this, intent.getStringExtra("place"), Toast.LENGTH_SHORT).show()

        title = intent.getStringExtra("place")
    }
}
