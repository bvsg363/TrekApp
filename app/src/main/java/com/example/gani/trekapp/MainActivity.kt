package com.example.gani.trekapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        login_button.setOnClickListener {
            checkfeilds()
        }

        signup_button.setOnClickListener {
            val loginIntent = Intent(this, SignUpActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun checkfeilds() {



        val loginIntent = Intent(this, HomeScreenActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(loginIntent)
        finish()
    }


}
