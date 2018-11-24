package com.example.gani.trekapp

class GlobalVariables {
    private val ip = "192.168.0.8"
    val loginUrl = "http://$ip:8080/TrekAppServer/Login"
    val signUpUrl = "http://$ip:8080/TrekAppServer/Signup"
    val pastTreksUrl = "http://$ip:8080/TrekAppServer/GetPastVisit"
    val treksUrl = "http://$ip:8080/TrekAppServer/GetTreks"
}