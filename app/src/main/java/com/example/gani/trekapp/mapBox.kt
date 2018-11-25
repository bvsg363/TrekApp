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
import com.mapbox.mapboxsdk.geometry.LatLngBounds
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

        mapView1.onCreate(savedInstanceState)

        val latLngBounds = LatLngBounds.Builder()
                //.include(LatLng(37.7897, -119.5073)) // Northeast
                //.include(LatLng(37.6744, -119.6815)) // Southwest
                .include(LatLng(19.144813, 72.920681)) // Northeast
                .include(LatLng(19.136674, 72.913977)) // Southwest
                //.include(LatLng(23.36, 85.335)) // Northeast
                //.include(LatLng(23.31, 85.284)) // Southwest
                .build()

//        mapView1.set
//        mapView1

        mapView1.getMapAsync{
//            it.uiSettings.setAllGesturesEnabled(false)
            it.setMaxZoomPreference(19.0)
            it.setMinZoomPreference(16.0)
            it.setLatLngBoundsForCameraTarget(latLngBounds)
        }

        val polygonLatLongList = ArrayList<LatLng>()
        polygonLatLongList.add(LatLng(19.130, 72.910))
        polygonLatLongList.add(LatLng(19.135, 72.910))
        polygonLatLongList.add(LatLng(19.135, 72.915))
        polygonLatLongList.add(LatLng(19.140, 72.915))
        polygonLatLongList.add(LatLng(19.140, 72.920))


        mapView1.getMapAsync {
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
