package com.example.gani.trekapp

class GlobalVariables {

    val ip = "192.168.0.109"

    val loginUrl = "http://$ip:8080/TrekAppServer/Login"
    val signUpUrl = "http://$ip:8080/TrekAppServer/Signup"
    val pastTreksUrl = "http://$ip:8080/TrekAppServer/GetPastVisit"
    val treksUrl = "http://$ip:8080/TrekAppServer/GetTreks"
    val trekDataUrl = "http://$ip:8080/TrekAppServer/TrekData"
    val trekImageUrl = "http://$ip:8080/TrekAppServer/TrekImage"
    val sendTrekData = "http://$ip:8080/TrekAppServer/ReceiveTrekData"
}