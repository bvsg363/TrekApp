package com.example.gani.trekapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.Polyline
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map_box.*
import kotlinx.android.synthetic.main.activity_map_box.view.*
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.math.sqrt

class mapBox : AppCompatActivity(), PermissionsListener, LocationEngineListener {

//    private lateinit var mapView: MapView

    private var trekInfo: JSONObject? = null
    private var fileName: String? = null
    private var trekId: String? = null
    private var directionsLine: Polyline? = null
    private var index: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    private var adj: MutableMap<Int, MutableList<Pair<Int, Double>>> = mutableMapOf<Int, MutableList<Pair<Int, Double>>>()
    private var latlng: MutableMap<Int, LatLng> = mutableMapOf<Int, LatLng>()
    private var mylocation: LatLng? = null
    private var pressedMarker: Marker? = null

    private var permissionsManager: PermissionsManager? = null
    private var originLocation: Location? = null
    private var mapboxMap: MapboxMap? = null
    private var locationEngine: LocationEngine? = null

    private var locationArray: ArrayList<Location> = ArrayList()

    private var active = 0
    var revisit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map_box)

        trekId = intent.getStringExtra("trekId")          // At present sending the id as a string
        fileName = "$filesDir/trekData_$trekId"
        val file = File(fileName)
        trekInfo = JSONObject(file.readText())
//        Log.i("mapBox The file content", file.readText())

        showAdminAlert()


        val arr : List<String> = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
                .getString("locationArray", "")
                .split(";")

        if (arr.isNotEmpty()){
            for (i in arr){
                Log.i("mapBox1", i)
            }
        }

//        button4.setOnClickListener {
//            Toast.makeText(this, LatLng(19.133081, 72.913458)
//                    .distanceTo(LatLng(19.135400, 72.909873))
//                    .toString(), Toast.LENGTH_SHORT)
//                    .show()
//        }

        startButton.setOnClickListener {
            active = 1
        }

        breakButton.setOnClickListener {

            active = 0

            val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)


            if(locationArray.size <= 0){

                Toast.makeText(this, "No locations recorded, press START", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showRevisitPointAlert()

            val lastPatchPoint = locationArray[locationArray.size - 1]
            val sb = StringBuilder()

            val default = setOf<String>()
            var prevPref = setOf<String>()

            if (sharedPreferences.contains("patchArray")){
                prevPref = sharedPreferences.getStringSet("patchArray", default)
            }

            for (i in locationArray) {
                sb.append(i).append(";")
            }

            if (revisit == 1){
                sb.append(findRevisitPoint(prevPref, lastPatchPoint)).append(";")
            }

            sb.dropLast(1)

            prevPref.plus(sb.toString())
            val sharedPrefEditor =  sharedPreferences.edit()

            sharedPrefEditor.putStringSet("patchArray", prevPref)

            sharedPrefEditor.apply()

            locationArray.clear()
            locationArray.add(lastPatchPoint)
        }

        endBotton.setOnClickListener {

            val default = setOf<String>()
            val patchArray = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
                                                 .getStringSet("patchArray", default)

            val outJsonArr = JsonArray()

            for (patch in patchArray) {

                val locationPoints : List<String> = patch.split(";")
                val inJsonArr = JsonArray()

//                var count = 0
//                var prevLatd = 0.0
//                var prevLongd = 0.0

                for(i in locationPoints) {

                    val latd = i.substring(15, 24).toDouble()
                    val longd = i.substring(25, 34).toDouble()

                    val json = JsonObject()

                    json.addProperty("lat", latd)
                    json.addProperty("long", longd)

                    inJsonArr.add(json)

//                    if(count > 0){
//                        val lineSegment = ArrayList<LatLng>()
//                        lineSegment.add(LatLng(prevLatd, prevLongd))
//                        lineSegment.add(LatLng(latd, longd))
//                        mapboxMap.addPolyline(PolylineOptions()
//                                .addAll(lineSegment)
//                                .color(Color.parseColor("#3895D3"))
//                                .width(5f))
//                    }

//                    count += 1
//                    prevLatd = latd
//                    prevLongd = longd
                }

                outJsonArr.add(inJsonArr)
            }

            val pathJson = JsonObject()
            pathJson.add("path_segment", outJsonArr)

            sendData(pathJson)

        }

        index = mutableMapOf<Int, Int>()

        var cordArr = trekInfo?.getJSONArray("co_ordinates")!!
        for(i in 0..(cordArr.length()-1)){
            var id = cordArr.getJSONObject(i).getInt("id")
            index[i+1] = id
            latlng[i+1] = LatLng(cordArr.getJSONObject(i).getDouble("lat"), cordArr.getJSONObject(i).getDouble("long"))
            adj[i+1] = mutableListOf<Pair<Int, Double>>()
        }

        var paths = trekInfo?.getJSONArray("paths")!!
        for(i in 0..(paths.length() - 1)){
            var segment = paths.getJSONObject(i)
            var first = index[segment.getInt("fco_id")]!!
            var second = index[segment.getInt("sco_id")]!!
            var distance = LatLng(segment.getDouble("flat"), segment.getDouble("flong")).distanceTo(LatLng(segment.getDouble("slat"), segment.getDouble("slong")))

            adj[first]!!.add(Pair(second, distance))
            adj[second]!!.add(Pair(first, distance))
        }

        my_location.setOnClickListener {



        }

        card_get_directions.setOnClickListener {
            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show()
            //if(mylocation)
            //cardView.card_trek_place.text
            if(mylocation != null){
//                drawLine(mapboxMap!!, mylocation!!, LatLng(pressedMarker!!.position.latitude, pressedMarker!!.position.longitude))
                Toast.makeText(this, pressedMarker!!.title, Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, "Please Enable Location", Toast.LENGTH_SHORT)
            }
//            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show()
//            getDirections()
        }
        cardView.setOnClickListener {

        }

        card_get_info.setOnClickListener {
            displayPlaceInfo()
        }

        loadMap(savedInstanceState)


    }



//    fun drawLine(mapboxMap: MapboxMap, spoint: LatLng, epoint: LatLng){
//        var minDistance = 2345678.0
//        var minIndex = 1
//        for(i in 0..(index.size-1)){
//            var curDis = spoint.distanceTo(latlng[i+1])
//            if(curDis < minDistance){
//                minDistance = curDis
//                minIndex = i + 1
//            }
//        }
//        var minDistance2 = 2345678.0
//        var minIndex2 = 1
//        for(i in 0..(index.size-1)){
//            var curDis = epoint.distanceTo(latlng[i+1])
//            if(curDis < minDistance2){
//                minDistance2 = curDis
//                minIndex2 = i + 1
//            }
//        }
//
//        var vis = IntArray(index.size + 1)
//        var dist = DoubleArray(index.size + 1)
//        var parent = IntArray(index.size + 1)
//        latlng[0] = spoint
//        var queue: PriorityQueue<Pair<Double, Pair<Int, Int>>> = PriorityQueue<Pair<Double, Pair<Int, Int>>>(compareBy({it.first}))
//        queue.add(Pair(minDistance, Pair(minIndex, 0)))
//        while (queue.isNotEmpty()){
//            var top = queue.remove()
//            if(vis[top.second.first]==1){
//                continue
//            }
//            vis[top.second.first]=1
//            dist[top.second.first]=top.first
//            parent[top.second.first]=top.second.second
//            for(i in 0..(adj[top.second.first]!!.size - 1)){
//                queue.add(Pair(top.first+adj[top.second.first]!![i].second,Pair(adj[top.second.first]!![i].first,top.second.first)))
//            }
//        }
//        var path = ArrayList<LatLng>()
//        var found = 0
//        var curPoint = minIndex2
//        while(found == 0){
//            path.add(latlng[curPoint]!!)
//            Log.i("Adding point id:", curPoint.toString())
//            if(curPoint == 0){
//                found = 1
//            }
//            curPoint = parent[curPoint]
//        }
//        if(directionsLine != null){
//            directionsLine?.remove()
//        }
//        directionsLine = mapboxMap.addPolyline(PolylineOptions()
//                .addAll(path)
//                .color(Color.parseColor("#ED2939"))
//                .width(5f))
//    }

    fun loadMap(savedInstanceState: Bundle?){

        Log.i("mapBox", "loadmap called")

        mapView1.onCreate(savedInstanceState)


        val latLngBounds = LatLngBounds.Builder()
                //.include(LatLng(19.144813, 72.920681)) // Northeast
                //.include(LatLng(19.136674, 72.913977)) // Southwest
                .include(LatLng(trekInfo?.getDouble("ne-lat")!!, trekInfo?.getDouble("ne-long")!!)) // Northeast
                .include(LatLng(trekInfo?.getDouble("sw-lat")!!, trekInfo?.getDouble("sw-long")!!)) // Southwest
                .build()

//        val polygonLatLongList = ArrayList<LatLng>()
//        polygonLatLongList.add(LatLng(19.130, 72.910))
//        polygonLatLongList.add(LatLng(19.135, 72.910))
//        polygonLatLongList.add(LatLng(19.135, 72.915))
//        polygonLatLongList.add(LatLng(19.140, 72.915))
//        polygonLatLongList.add(LatLng(19.140, 72.920))


        mapView1.getMapAsync {
            mapboxMap ->

            mapboxMap.setMaxZoomPreference(19.0)
            mapboxMap.setMinZoomPreference(16.0)
            mapboxMap.setLatLngBoundsForCameraTarget(latLngBounds)

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
                placeMarker(
                        mapboxMap,
                        place.getDouble("lat"),
                        place.getDouble("long"),
                        place.getString("name")
                )
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
                    .target(latLngBounds.center)
                    .zoom(17.0)
                    .bearing(0.0)
                    .tilt(30.0)
                    .build()
            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(position), 4000)
        }

    }


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
//        Toast.makeText(this, "Entered enableLocationComponent", Toast.LENGTH_LONG).show()

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
//            locationComponent?.cameraMode = CameraMode.TRACKING_GPS_NORTH
            locationComponent?.renderMode = RenderMode.COMPASS
//            locationComponent?.tiltWhileTracking(0.0)
            originLocation = locationComponent?.lastKnownLocation
//            Toast.makeText(this, originLocation.toString(), Toast.LENGTH_LONG).show()

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
//        lastPatchPoint = locationEngine?.lastLocation!!
        Toast.makeText(this, "Connected!!", Toast.LENGTH_LONG).show()
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        if (active == 1) {
            locationArray.add(location)
        }
        mylocation = LatLng(location?.latitude!!, location?.longitude!!)
    }
//        val locationStr = "Latitude : " + location.latitude.toString() +
//                "\nLongitude : " + location.longitude.toString() +
//                "\nAccuracy : " + location.accuracy.toString() +
//                "\nTime : " + location.time.toString()
//
//        Toast.makeText(this, locationStr, Toast.LENGTH_LONG).show()

    private fun placeMarker(mapboxMap: MapboxMap, lat: Double, lon: Double, title: String){
        mapboxMap.addMarker(MarkerOptions()
                .position(LatLng(lat, lon))
                .title(title))
    }

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
        //if(locationEngine != null) {
        //    locationEngine?.requestLocationUpdates()
        //}
        //locationEngine?.lastLocation
        pressedMarker = marker
        card_trek_place.text = marker.title
        card_trek_info.text = "Hey this is a cool place"
    }

    fun disableCard(){
        if (cardView.visibility.equals(VISIBLE)){
            cardView.visibility = View.GONE
        }
    }

    private fun findRevisitPoint(prevRef : Set<String>, lastPatchPoint : Location) : String{
        var returnStr = ""
        var min = 500.0

        for (patch in prevRef) {
            val locationPoints : List<String> = patch.split(";")
            val i = locationPoints[0]
            val latd = i.substring(15, 24).toDouble()
            val longd = i.substring(25, 34).toDouble()

            Toast.makeText(this, LatLng(19.133081, 72.913458)
                    .distanceTo(LatLng(19.135400, 72.909873))
                    .toString(), Toast.LENGTH_SHORT)
                    .show()

            if (LatLng(latd, longd).distanceTo(LatLng(lastPatchPoint.latitude, lastPatchPoint.longitude)) < min){
                min = LatLng(latd, longd).distanceTo(LatLng(lastPatchPoint.latitude, lastPatchPoint.longitude))
                returnStr = i
            }
        }

        return returnStr
    }


    fun displayPlaceInfo(){
        val loginIntent = Intent(this, PlaceInfo::class.java)
        intent.putExtra("place", pressedMarker?.title)
        startActivity(loginIntent)
    }

    fun showAdminAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Admin Verification")

        builder.setMessage("Are u an admin?")

        builder.setPositiveButton("Yes"){_, _ ->
            admin_layout.visibility = View.VISIBLE
        }

        builder.setNegativeButton("No"){_, _ ->
            admin_layout.visibility = View.INVISIBLE
        }

        builder.setCancelable(false)
        builder.create().show()
    }

    fun showRevisitPointAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Resivit verification")

        builder.setMessage("Have you been here before?")

        builder.setPositiveButton("Yes"){_, _ ->
            revisit = 1
        }

        builder.setNegativeButton("No"){_, _ ->
        }

        builder.setCancelable(false)
        builder.create().show()
    }

    fun sendData(json : JsonObject){

        val url = GlobalVariables().sendTrekData

        val finalUrl = "$url?data=$json"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject>{ response ->

            if (response.getString("status") == "true"){
                Toast.makeText(this, "Success sending trek data", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show()
            }

        }, Response.ErrorListener {

            Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()
        })

        requestQueue.add(jsonRequest)
    }
}
