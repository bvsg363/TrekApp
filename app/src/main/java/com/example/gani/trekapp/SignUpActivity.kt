package com.example.gani.trekapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        button2.setOnClickListener {
            val signupIntent = Intent(this, HomeScreenActivity::class.java)

            startActivity(signupIntent)
            finishAffinity()
        }
    }
}
