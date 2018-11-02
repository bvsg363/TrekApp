package com.example.gani.trekapp

import android.content.Intent
import android.Manifest
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_trek.*
import android.widget.Toast
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class TrekActivity : AppCompatActivity() {
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
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            myPermissions)

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
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
                        val locationStr = "Latitude : " + location.latitude.toString() +
                                "\nLongitude : " + location.longitude.toString() +
                                "\nAltitude : " + location.altitude.toString() +
                                "\nAccuracy : " + location.accuracy.toString() +
                                "\nSpeed : " + location.speed.toString() +
//                                "\nVertical Accuracy" + location.verticalAccuracyMeters.toString() +
                                "\nTime : " + location.time.toString() +
                                "\nBearing : " + location.bearing.toString()
//                        val duration = Toast.LENGTH_SHORT
//
//                        val toast = Toast.makeText(applicationContext, locationStr, duration)
//                        toast.show()
                        location_text.text = locationStr
                    }
                    // Got last known location. In some rare situations this can be null.
                }
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...
                    val locationStr = "Latitude : " + location.latitude.toString() +
                            "\nLongitude : " + location.longitude.toString() +
                            "\nAltitude : " + location.altitude.toString() +
                            "\nAccuracy : " + location.accuracy.toString() +
                            "\nSpeed : " + location.speed.toString() +
//                                "\nVertical Accuracy" + location.verticalAccuracyMeters.toString() +
                            "\nTime : " + location.time.toString() +
                            "\nBearing : " + location.bearing.toString()
//                        val duration = Toast.LENGTH_SHORT
//
//                        val toast = Toast.makeText(applicationContext, locationStr, duration)
//                        toast.show()
                    location_text.text = locationStr
                }
            }
        }

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
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        myPermissions)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
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
            interval = 10000
            fastestInterval = 5000
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
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
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

        // ...

        // Update UI to match restored state
        onCreate(savedInstanceState)
    }
}

//class Location : AppCompatActivity() {
//    internal lateinit var locationManager: LocationManager
//    internal lateinit var mContext: Context
//
//    internal var locationListenerGPS: LocationListener = object : LocationListener {
//        override fun onLocationChanged(location: android.location.Location) {
//            val latitude = location.latitude
//            val longitude = location.longitude
//            val msg = "New Latitude: " + latitude + "New Longitude: " + longitude
//            Toast.makeText(this@Location, msg, Toast.LENGTH_LONG).show()
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
//
//        }
//
//        override fun onProviderEnabled(provider: String) {
//
//        }
//
//        override fun onProviderDisabled(provider: String) {
//
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_trek)
//        mContext = this
//        locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        isLocationEnabled()
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                2000,
//                10f, locationListenerGPS)
//    }
//
//
//    override fun onResume() {
//        super.onResume()
//        isLocationEnabled()
//    }
//
//    private fun isLocationEnabled() {
//
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            val alertDialog = AlertDialog.Builder(mContext)
//            alertDialog.setTitle("Enable Location")
//            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.")
//            alertDialog.setPositiveButton("Location Settings", DialogInterface.OnClickListener { dialog, which ->
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)
//            })
//            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
//            val alert = alertDialog.create()
//            alert.show()
//        } else {
//            val alertDialog = AlertDialog.Builder(mContext)
//            alertDialog.setTitle("Confirm Location")
//            alertDialog.setMessage("Your Location is enabled, please enjoy")
//            alertDialog.setNegativeButton("Back to interface", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
//            val alert = alertDialog.create()
//            alert.show()
//        }
//    }
//}
