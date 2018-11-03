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
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        progressBar2.visibility = View.INVISIBLE


        button2.setOnClickListener {

            try{
                val inp: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inp.hideSoftInputFromWindow(button2.windowToken, 0)
            }
            catch(e :Exception){
                Log.i("SignUpActivity", e.toString())
            }

            checkFeilds()
            clearFields()
        }
    }

    private fun sendGetVolleySignup(emailId : String, password: String, username: String){
        val url = GlobalVariables().signUpUrl

        val finalUrl = "$url?email=$emailId&password=$password&user_name=$username&verified=0"

        val requestQueue = Volley.newRequestQueue(this)


        val jsonRequest = JsonObjectRequest(Request.Method.GET, finalUrl, null, Response.Listener<JSONObject>{ response ->

            progressBar2.visibility = View.INVISIBLE
            print(response)

            Log.i("MainActivity", response.getString("status"))


            if (response.getString("status") == "success"){

                Toast.makeText(this, "Registration Success!", Toast.LENGTH_SHORT).show()
                saveSesion(response.getInt("uid"), emailId, password, username)
                doSignUp()
            }
            else{
                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
            }

        }, Response.ErrorListener {
            progressBar2.visibility = View.INVISIBLE
            Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()
        })

        requestQueue.add(jsonRequest)

    }

    private fun checkFeilds(){

        val emailId = editText5.text.toString()
        val password = editText6.text.toString()
        val username = editText.text.toString()

        when {
            username.trim().isEmpty() -> Toast.makeText(this, "Username field is Empty!", Toast.LENGTH_SHORT).show()
            emailId.trim().isEmpty() -> Toast.makeText(this, "Email field is Empty!", Toast.LENGTH_SHORT).show()
            password.trim().isEmpty() -> Toast.makeText(this, "Password field is Empty!", Toast.LENGTH_SHORT).show()
            password.trim().length < 5 -> Toast.makeText(this, "Enter Password between 5-20 characters", Toast.LENGTH_SHORT).show()
            password.trim().length > 20 -> Toast.makeText(this, "Enter Password between 5-20 characters", Toast.LENGTH_SHORT).show()

            else -> {
                progressBar2.visibility = View.VISIBLE
                sendGetVolleySignup(emailId, password, username)
            }
        }
    }

    private fun clearFields() {
        editText.text.clear()
        editText5.text.clear()
        editText6.text.clear()
    }

    fun doSignUp(){
        val signupIntent = Intent(this, HomeScreenActivity::class.java)
        startActivity(signupIntent)
        finishAffinity()
    }

    fun saveSesion(uid : Int, emailId: String, password: String, username: String){
        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
        val sharedPrefEditor =  sharedPreferences.edit()

        sharedPrefEditor.putInt("uid", uid)
        sharedPrefEditor.putString("email", emailId)
        sharedPrefEditor.putString("password", password)
        sharedPrefEditor.putString("username", username)
        sharedPrefEditor.apply()
    }
}
