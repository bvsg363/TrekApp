package com.example.gani.trekapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.gms.maps.CameraUpdateFactory
import android.widget.Toast
import com.google.gson.JsonArray
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map_box.*
import org.json.JSONObject
import java.io.File

class mapBox : AppCompatActivity(), PermissionsListener, LocationEngineListener {

//    private lateinit var mapView: MapView

    private var trekInfo: JSONObject? = null
    private var fileName: String? = null
    private var trekId: String? = null

    private var permissionsManager: PermissionsManager? = null
    private var originLocation: Location? = null
    private var mapboxMap: MapboxMap? = null
    private var locationEngine: LocationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map_box)

        trekId = intent.getStringExtra("trekId")          // At present sending the id as a string
        fileName = "${filesDir}/trekData_$trekId"
        val file = File(fileName)
        trekInfo = JSONObject(file.readText())
        Log.i("mapBox The file content", file.readText())

        my_location.setOnClickListener {

        }

        card_get_directions.setOnClickListener {
//            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show()
            getDirections()
        }

        cardView.setOnClickListener {

        }

        card_get_info.setOnClickListener {
            displayPlaceInfo()
        }

        loadMap(savedInstanceState)


    }

    fun loadMap(savedInstanceState: Bundle?){

        mapView1.onCreate(savedInstanceState)


        val latLngBounds = LatLngBounds.Builder()
                //.include(LatLng(19.144813, 72.920681)) // Northeast
                //.include(LatLng(19.136674, 72.913977)) // Southwest
                .include(LatLng(trekInfo?.getDouble("ne-lat")!!, trekInfo?.getDouble("ne-long")!!)) // Northeast
                .include(LatLng(trekInfo?.getDouble("sw-lat")!!, trekInfo?.getDouble("sw-long")!!)) // Southwest
                .build()

        mapView1.getMapAsync{
            //            it.uiSettings.setAllGesturesEnabled(false)
            it.setMaxZoomPreference(19.0)
            it.setMinZoomPreference(16.0)
            it.setLatLngBoundsForCameraTarget(latLngBounds)
        }

//        val polygonLatLongList = ArrayList<LatLng>()
//        polygonLatLongList.add(LatLng(19.130, 72.910))
//        polygonLatLongList.add(LatLng(19.135, 72.910))
//        polygonLatLongList.add(LatLng(19.135, 72.915))
//        polygonLatLongList.add(LatLng(19.140, 72.915))
//        polygonLatLongList.add(LatLng(19.140, 72.920))


        mapView1.getMapAsync {
            mapboxMap ->

            this@mapBox.mapboxMap = mapboxMap
            enableLocationComponent()

            //mapboxMap.addMarker(MarkerOptions()
            //        .position(LatLng(19.1334, 72.9133))
            //        .title("IITB"))

            mapboxMap.setOnMarkerClickListener {marker ->
                onPlaceSelect(marker)
//                Toast.makeText(this, marker.title, Toast.LENGTH_SHORT).show()
                true
            }

            mapboxMap.addOnMapClickListener {
                disableCard()
            }

            val start_arr = trekInfo?.getJSONArray("start_points")!!
            val start_point = start_arr.getJSONObject(0)

            mapboxMap.addMarker(MarkerOptions()
                    .position(LatLng(start_point.getDouble("lat"), start_point.getDouble("long")))
                    .title("Start point"))

            val places = trekInfo?.getJSONArray("places")!!
            for(i in 0..(places.length()-1))
            {
                val place = places.getJSONObject(i)
                placeMarker(mapboxMap, place.getDouble("lat"), place.getDouble("long"), place.getString("name"))
            }

            //placeMarker(mapboxMap, 19.1334, 72.9133, "IITB")
            val lines = trekInfo?.getJSONArray("paths")!!
            for(i in 0..(lines.length() - 1))
            {
                val segment = lines.getJSONObject(i)
                val lineSegment = ArrayList<LatLng>()
                lineSegment.add(LatLng(segment.getDouble("flat"),segment.getDouble("flong")))
                lineSegment.add(LatLng(segment.getDouble("slat"),segment.getDouble("slong")))
                mapboxMap.addPolyline(PolylineOptions()
                        .addAll(lineSegment)
                        .color(Color.parseColor("#3895D3"))
                        .width(5f))
            }
            //mapboxMap.addPolyline(PolylineOptions()
            //        .addAll(polygonLatLongList)
            //        .color(Color.BLUE)
            //        .width(2f))
//            var latLngBounds = LatLngBounds.Builder()
//                    .include(LatLng(0.0, 0.0))
//                    .include(LatLng(2.0, 2.0))
//                    .build()
            //mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0))
            val center_lat = (trekInfo?.getDouble("ne-lat")!! + trekInfo?.getDouble("sw-lat")!!)/2
            val center_long = (trekInfo?.getDouble("ne-long")!! + trekInfo?.getDouble("sw-long")!!)/2
            val position: CameraPosition = CameraPosition.Builder()
                    .target(LatLng(19.141181, 72.918032))
                    .zoom(17.0)
                    .bearing(0.0)
                    .tilt(30.0)
                    .build()
            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(position), 7000)
        }

    }


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        Toast.makeText(this, "Entered enableLocationComponent", Toast.LENGTH_LONG).show()

        if(PermissionsManager.areLocationPermissionsGranted(this)) {
            val options = LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(this, R.color.mapbox_blue))
                    .build()

//            val position = CameraPosition.Builder()
//                    .target(LatLng(51.50550, -0.07520)) // Sets the new camera position
//                    .zoom(10.0) // Sets the zoom to level 10
//                    .tilt(20) // Set the camera tilt to 20 degrees
//                    .build()

            val locationComponent = mapboxMap?.locationComponent
            locationComponent?.activateLocationComponent(this, options)
            locationComponent?.isLocationComponentEnabled = true
            locationComponent?.cameraMode = CameraMode.TRACKING_GPS_NORTH
            locationComponent?.renderMode = RenderMode.COMPASS
            locationComponent?.tiltWhileTracking(0.0)
            originLocation = locationComponent?.lastKnownLocation
            Toast.makeText(this, originLocation.toString(), Toast.LENGTH_LONG).show()

            locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
            locationEngine?.activate()
            locationEngine?.addLocationEngineListener(this)

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Needed permission for displaying location", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted) {
            enableLocationComponent()
        } else {
            Toast.makeText(this, "Needed permission for displaying location", Toast.LENGTH_LONG).show()
            enableLocationComponent()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Toast.makeText(this, "Connected!!", Toast.LENGTH_LONG).show()
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location?) {
//        Toast.makeText(this, location.toString(), Toast.LENGTH_LONG).show()
    }

    private fun placeMarker(mapboxMap: MapboxMap, lat: Double, lon: Double, title: String){
        mapboxMap.addMarker(MarkerOptions()
                .position(LatLng(lat, lon))
                .title(title)
        )
    }

    /*
    private fun drawPolyLine(mapboxMap: MapboxMap,
    */



    override fun onResume(){
        super.onResume()
        mapView1.onResume()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView1.onStart()
        if(locationEngine != null) {
            locationEngine?.requestLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        if (locationEngine != null) {
            locationEngine?.removeLocationUpdates()
        }
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

    fun onPlaceSelect(marker: Marker){

        if (cardView.visibility.equals(GONE)){
            cardView.visibility = View.VISIBLE
        }
//        else{
//            cardView.visibility = View.GONE
//        }

        card_trek_place.text = marker.title
        card_trek_info.text = "Hey this is a cool place"
    }

    fun disableCard(){
        if (cardView.visibility.equals(VISIBLE)){
            cardView.visibility = View.GONE
        }
    }

    fun displayPlaceInfo(){
        val loginIntent = Intent(this, PlaceInfo::class.java)
        intent.putExtra("place", card_trek_place.text)
        startActivity(loginIntent)
    }

    fun getDirections(){

//        val location = LocationEngine.

    }
}
