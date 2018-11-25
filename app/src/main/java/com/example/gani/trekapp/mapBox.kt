package com.example.gani.trekapp

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.constants.Style
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_map_box.*
import org.json.JSONObject
import java.io.File

class mapBox : AppCompatActivity() {

//    private lateinit var mapView: MapView

    private var trekInfo: JSONObject? = null
    private var fileName: String? = null
    private var trekId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map_box)

        trekId = intent.getStringExtra("trekId")          // At present sending the id as a string
        fileName = "${filesDir}/trekData_$trekId"
        var file = File(fileName)
        trekInfo = JSONObject(file.readText())
        Log.i("mapBox The file content", file.readText())
        mapView.onCreate(savedInstanceState)
        /*mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;

                // Load and Draw the GeoJSON. The marker animation is also handled here.
                new DrawGeoJson ().execute();
            }
        })*/

        val polygonLatLongList = ArrayList<LatLng>()
        polygonLatLongList.add(LatLng(19.130, 72.910))
        polygonLatLongList.add(LatLng(19.135, 72.910))
        polygonLatLongList.add(LatLng(19.135, 72.915))
        polygonLatLongList.add(LatLng(19.140, 72.915))
        polygonLatLongList.add(LatLng(19.140, 72.920))


        mapView.getMapAsync {
            mapboxMap ->

            //mapboxMap.addMarker(MarkerOptions()
            //        .position(LatLng(19.1334, 72.9133))
            //        .title("IITB"))
            placeMarker(mapboxMap, 19.1334, 72.9133, "IITB")
            mapboxMap.addPolyline(PolylineOptions()
                    .addAll(polygonLatLongList)
                    .color(Color.BLUE)
                    .width(2f))
        }

        //mapView.getMapAsync(
        //)
        //mapView.getMapAsync({
         //   it.setStyle(Style.SATELLITE)
        //})
    }

    private fun placeMarker(mapboxMap: MapboxMap, lat: Double, lon: Double, title: String){
        mapboxMap.addMarker(MarkerOptions()
                .position(LatLng(lat, lon))
                .title(title))
    }

    /*
    private fun drawPolyLine(mapboxMap: MapboxMap,
    */

    override fun onResume(){
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
