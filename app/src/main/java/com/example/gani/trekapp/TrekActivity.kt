package com.example.gani.trekapp

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_trek.*

//import com.mapbox.android.core.location.LocationEngineListener
//import com.mapbox.android.core.location.LocationEngineProvider
//import com.mapbox.android.core.permissions.PermissionsManager
//import com.mapbox.api.directions.v5.models.DirectionsResponse
//import com.mapbox.geojson.Point
//import com.mapbox.mapboxsdk.Mapbox
//import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
//import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response


class TrekActivity : AppCompatActivity() {

//    private var permissionsManager: PermissionsManager? = null
//
//    private var locationEngineListener: LocationEngineListener? = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val navigation = MapboxNavigation(this, getString(R.string.mapbox_access_token))
//
//        val origin = Point.fromLngLat(-77.03613, 38.90992)
//        val destination = Point.fromLngLat(-77.0365, 38.8977)
//
//        val locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
//        navigation.locationEngine = locationEngine
//        locationEngine.activate()
//        locationEngine.addLocationEngineListener(this.locationEngineListener)
//
//        NavigationRoute.builder(this)
//                .accessToken(Mapbox.getAccessToken()!!)
//                .origin(origin)
//                .destination(destination)
//                .build()
//                .getRoute(object : Callback<DirectionsResponse> {
//                    override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
//                        Toast.makeText(this@TrekActivity, "Inside NavigationRoute", Toast.LENGTH_LONG).show()
//                    }
//
//                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//
//                    }
//                })
//    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val myPermissions = 0
    private val requestUpdate = "1"
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = true
    private lateinit var locationRequest: LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trek)

        textView3.text = intent.getStringExtra("trekName")

        start_navigation_button.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        updateValuesFromBundle(savedInstanceState)

        get_location.setOnClickListener {
            Log.d("print", "clicked the button")
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("print", "permission not granted")
                requestingLocationUpdates = false
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            myPermissions)
                }
            } else {
                // Permission has already been granted
                Log.d("print", "permission granted")
                fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                    if(location == null){
                        val text = "Unable to get the location data"
                        val duration = Toast.LENGTH_SHORT

                        val toast = Toast.makeText(applicationContext, text, duration)
                        toast.show()
                    } else{
                        displayLocation(location)
                    }
                }
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...
                    displayLocation(location)
                }
            }
        }

    }

    private fun displayLocation(location: Location) {
        val locationStr = "Latitude : " + location.latitude.toString() +
                "\nLongitude : " + location.longitude.toString() +
                "\nAltitude : " + location.altitude.toString() +
                "\nAccuracy : " + location.accuracy.toString() +
                "\nSpeed : " + location.speed.toString() +
                "\nTime : " + location.time.toString() +
                "\nBearing : " + location.bearing.toString()

        location_text.text = locationStr
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        myPermissions)
            }
        } else {
            // Permission has already been granted
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */)
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(requestUpdate, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            Log.d("print", "successful changes to location settings")
            return@addOnSuccessListener
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@TrekActivity, myPermissions)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(requestUpdate)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    requestUpdate)
        }
        // Update UI to match restored state
        onCreate(savedInstanceState)
    }
}
