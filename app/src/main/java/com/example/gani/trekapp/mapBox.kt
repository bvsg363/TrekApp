package com.example.gani.trekapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.constants.Style
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.android.synthetic.main.activity_map_box.*

class mapBox : AppCompatActivity() {

//    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map_box)

        mapView1.onCreate(savedInstanceState)
        //mapView.getMapAsync({
         //   it.setStyle(Style.SATELLITE)
        //})
    }

    override fun onResume(){
        super.onResume()
        mapView1.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView1.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView1.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView1.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView1.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView1.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView1.onSaveInstanceState(outState)
    }
}
