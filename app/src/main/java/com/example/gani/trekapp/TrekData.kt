package com.example.gani.trekapp

class TrekData {
    data class Treks(
            val data : ArrayList<Trek>,
            val status : String
    )

    data class Trek(
            val name : String,
            val trek_id : String
    )
}