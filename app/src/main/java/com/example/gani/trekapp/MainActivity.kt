package com.example.gani.trekapp

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

        if (sharedPreferences.getInt("uid", 0) > 0){
            doLogin()
        }

        progressBar.visibility = View.INVISIBLE

//        val requestQueue = Volley.newRequestQueue(this)



        login_button.setOnClickListener {

            try{
                val inp: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inp.hideSoftInputFromWindow(login_button.windowToken, 0)
            }
            catch(e :Exception){
                Log.i("MainActivity", e.toString())
            }
            checkFields()
            clearFields()

        }

        signup_button.setOnClickListener {
            val loginIntent = Intent(this, SignUpActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun checkFields() {

        val emailId = login_email.text.toString()
        val password = login_password.text.toString()

        when {
            emailId.trim().isEmpty() -> Toast.makeText(this, "Email field is Empty!", Toast.LENGTH_SHORT).show()
            password.trim().isEmpty() -> Toast.makeText(this, "Password field is Empty!", Toast.LENGTH_SHORT).show()
            else -> {
                progressBar.visibility = View.VISIBLE
                sendGetVolleyLogin(emailId, password)
            }
        }
    }

    private fun clearFields() {
        login_email.text.clear()
        login_password.text.clear()
    }

    private fun doLogin(){

        val loginIntent = Intent(this, HomeScreenActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(loginIntent)
        finish()
    }

    private fun sendGetVolleyLogin(emailId : String, password: String){
        val url = GlobalVariables().loginUrl

        val finalUrl = "$url?email=$emailId&password=$password"
        val requestQueue = Volley.newRequestQueue(this)


        val jsonRequest = JsonObjectRequest(Request.Method.GET, finalUrl, null, Response.Listener<JSONObject>{ response ->

            progressBar.visibility = View.INVISIBLE
            print(response)

            Log.i("MainActivity", response.getString("status"))


            if (response.getString("status") == "success"){

                Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                saveSession(response.getInt("uid"), emailId, password)
                doLogin()
            }
            else{
                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
            }

        }, Response.ErrorListener {
            progressBar.visibility = View.INVISIBLE
            Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()
        })

        requestQueue.add(jsonRequest)

    }

//    override fun onStop() {
//        super.onStop()
//    }

    fun saveSession(uid : Int, emailId: String, password: String){
        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
        val sharedPrefEditor =  sharedPreferences.edit()

        sharedPrefEditor.putInt("uid", uid)
        sharedPrefEditor.putString("email", emailId)
        sharedPrefEditor.putString("password", password)
        sharedPrefEditor.apply()
    }


}
