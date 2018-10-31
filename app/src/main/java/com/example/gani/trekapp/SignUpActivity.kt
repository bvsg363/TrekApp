package com.example.gani.trekapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val signupIntent = Intent(this, MainActivity::class.java)

        button2.setOnClickListener {
            startActivity(signupIntent)
        }
    }
}
