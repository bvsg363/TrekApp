package com.example.gani.trekapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

//import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import kotlinx.android.synthetic.main.activity_download_map.*

import org.json.JSONObject
import java.io.File

/**
 * Download and view an offline map using the Mapbox Android SDK.
 */
class DownloadMap : AppCompatActivity() {

    private var isEndNotified: Boolean = false
    private var trekName: String? = null
    private var fileName: String? = null
    private var trekId: String? = null
    private var progressBar: ProgressBar? = null
    private var trekData: JSONObject? = null
//    private var mapView: MapView? = null
    private var offlineManager: OfflineManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trekId = intent.getStringExtra("trekName")          // At present sending the id as a string
        fileName = "${filesDir}/trekData_$trekId"


        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_download_map)
        imageView.setImageResource(R.drawable.hi)

        var file = File(fileName)
        if(!file.exists()){
            start_button.visibility = View.INVISIBLE
        }
        start_button.setOnClickListener {
            getTrekData(true, false, false)
        }

        //     Set up the OfflineManager
        offlineManager = OfflineManager.getInstance(this)

        download_button.setOnClickListener {
            getTrekData(false, true, false, 19.13, 72.96, 19.08, 72.86)
        }

    }

    private fun downloadMap(latmin: Double, lonmin: Double, latmax: Double, lonmax: Double){
        val latLngBounds = LatLngBounds.Builder()
                //.include(LatLng(37.7897, -119.5073)) // Northeast
                //.include(LatLng(37.6744, -119.6815)) // Southwest
                //.include(LatLng(19.13, 72.96)) // Northeast
                //.include(LatLng(19.08, 72.86)) // Southwest
                .include(LatLng(latmax, lonmax)) // Northeast
                .include(LatLng(latmin, lonmin)) // Southwest
                //.include(LatLng(23.36, 85.335)) // Northeast
                //.include(LatLng(23.31, 85.284)) // Southwest
                .build()

        // Define the offline region
//            Toast.makeText(this, mapboxMap.styleUrl, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "mapbox://styles/mapbox/streets-v10", Toast.LENGTH_SHORT).show()
        val definition = OfflineTilePyramidRegionDefinition(
//                    mapboxMap.styleUrl,
                "mapbox://styles/mapbox/streets-v10",
                latLngBounds,
                10.0,
                20.0,
                this.resources.displayMetrics.density)

        // Set the metadata
        var metadata: ByteArray?
        try {
            val jsonObject = JSONObject()
            jsonObject.put(JSON_FIELD_REGION_NAME, "Yosemite National Park")
            val json = jsonObject.toString()
            metadata = json.toByteArray(charset(JSON_CHARSET))
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.message)
            metadata = null
        }

        // Create the region asynchronously

        offlineManager!!.createOfflineRegion(
                definition,
                metadata!!,
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(offlineRegion: OfflineRegion) {
//                            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                        // Display the download progress bar
                        progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
                        startProgress()

                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                            override fun onStatusChanged(status: OfflineRegionStatus) {

                                // Calculate the download percentage and update the progress bar
                                val percentage = if (status.requiredResourceCount >= 0)
                                    100.0 * status.completedResourceCount / status.requiredResourceCount
                                else
                                    0.0

                                if (status.isComplete) {
                                    // Download complete
                                    endProgress("Success")
                                    //getTrekData(false)
                                    start_button.visibility = View.VISIBLE
                                } else if (status.isRequiredResourceCountPrecise) {
                                    // Switch to determinate state
                                    setPercentage(Math.round(percentage).toInt())
                                }
                            }

                            override fun onError(error: OfflineRegionError) {
                                // If an error occurs, print to logcat
                                Log.e(TAG, "onError reason: " + error.reason)
                                Log.e(TAG, "onError message: " + error.message)
                            }

                            override fun mapboxTileCountLimitExceeded(limit: Long) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e(TAG, "Mapbox tile count limit exceeded: $limit")
                            }
                        })

                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                    }

                    override fun onError(error: String) {
                        Log.e(TAG, "Error: $error")
                    }
                }
        )
    }

    private fun getTrekData(gotoMapFlag: Boolean, downloadMap: Boolean, calcBound: Boolean, latmin: Double=0.0, lonmin: Double=0.0, latmax: Double=0.0, lonmax: Double=0.0):Boolean{
        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
        //val uid = sharedPreferences.getInt("uid", 0)
        val url = GlobalVariables().trekDataUrl
        val requestQueue = Volley.newRequestQueue(this)
        val finalUrl = "$url?trek_id=$trekId"

        val jsonRequest = JsonObjectRequest(Request.Method.GET, finalUrl, null, Response.Listener<JSONObject>{ response ->

            print(response)
            trekData = response
            Log.i("TrekData", response.getString("status"))

            if (response.getString("status") == "success"){
//                Toast.makeText(this, "Success getting treks", Toast.LENGTH_SHORT).show()
                saveTrekData(response)
                if(downloadMap){
                    downloadMap(latmin, lonmin, latmax, lonmax)
                }
                else if(gotoMapFlag){
                    val mapStartIntent = Intent(this, mapBox::class.java)
                    mapStartIntent.putExtra("trekId", trekId)
                    startActivity(mapStartIntent)
                }
                else{
                    start_button.visibility = View.VISIBLE
                }
//                    displayTreks(response)
            }
            else{

//                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()

                Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show()
                if(gotoMapFlag){
                    val mapStartIntent = Intent(this, mapBox::class.java)
                    mapStartIntent.putExtra("trekId", trekId)
                    startActivity(mapStartIntent)
                }
            }

        }, Response.ErrorListener {
            //val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

            Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()
            if(gotoMapFlag){
                val mapStartIntent = Intent(this, mapBox::class.java)
                mapStartIntent.putExtra("trekId", trekId)
                startActivity(mapStartIntent)
            }

            //if (sharedPreferences.contains("treksAvailable")) {
                //displayTreks(JSONObject(sharedPreferences.getString("treksAvailable", "")))

            //}
        })

        requestQueue.add(jsonRequest)

        return true;
    }

    private fun saveTrekData(response: JSONObject) {
        var file = File(fileName)
        file.writeText(response.toString())
    }

    public override fun onPause() {
        super.onPause()
//        mapView!!.onPause()
        if (offlineManager != null) {
            offlineManager!!.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
                override fun onList(offlineRegions: Array<OfflineRegion>) {
                    if (offlineRegions.size > 0) {
                        // delete the last item in the offlineRegions list which will be yosemite offline map
                        offlineRegions[offlineRegions.size - 1].delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                            override fun onDelete() {
                                Toast.makeText(
                                        this@DownloadMap,
                                        "Offline deleted",
                                        Toast.LENGTH_LONG
                                ).show()

                            }

                            override fun onError(error: String) {
                                Log.e(TAG, "On Delete error: $error")
                            }
                        })
                    }
                }

                override fun onError(error: String) {
                    Log.e(TAG, "onListError: $error")
                }
            })
        }
    }

    // Progress bar methods
    private fun startProgress() {

        // Start and show the progress bar
        isEndNotified = false
        progressBar!!.isIndeterminate = true
        progressBar!!.visibility = View.VISIBLE
//        progressBar!!.progress = 25
    }

    private fun setPercentage(percentage: Int) {
        progressBar!!.isIndeterminate = false
        progressBar!!.progress = percentage
    }

    private fun endProgress(message: String) {
        // Don't notify more than once
        if (isEndNotified) {
            return
        }

        // Stop and hide the progress bar
        isEndNotified = true
//        progressBar!!.isIndeterminate = false
//        progressBar!!.progress = 75
        progressBar!!.visibility = View.GONE

        // Show a toast
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {

        private val TAG = "SimpOfflineMapActivity"

        // JSON encoding/decoding
        val JSON_CHARSET = "UTF-8"
        val JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME"
    }
}

