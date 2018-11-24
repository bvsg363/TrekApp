package com.example.gani.trekapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

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

import org.json.JSONObject

/**
 * Download and view an offline map using the Mapbox Android SDK.
 */
class DownloadMap : AppCompatActivity() {

    private var isEndNotified: Boolean = false
    private var progressBar: ProgressBar? = null
    private var mapView: MapView? = null
    private var offlineManager: OfflineManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_download_map)

        mapView = findViewById<View>(R.id.mapView) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync { mapboxMap ->
        //     Set up the OfflineManager
            offlineManager = OfflineManager.getInstance(this)

            // Create a bounding box for the offline region
            val latLngBounds = LatLngBounds.Builder()
                    //.include(LatLng(37.7897, -119.5073)) // Northeast
                    //.include(LatLng(37.6744, -119.6815)) // Southwest
                    .include(LatLng(19.13, 72.96)) // Northeast
                    .include(LatLng(19.08, 72.86)) // Southwest
                    //.include(LatLng(23.36, 85.335)) // Northeast
                    //.include(LatLng(23.31, 85.284)) // Southwest
                    .build()

            // Define the offline region
            Toast.makeText(this, mapboxMap.styleUrl, Toast.LENGTH_SHORT).show()
            val definition = OfflineTilePyramidRegionDefinition(
                    mapboxMap.styleUrl,
//                    "mapbox://styles/mapbox/streets-v10",
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
                    })
        }
    }

    public override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView!!.onPause()
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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
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

