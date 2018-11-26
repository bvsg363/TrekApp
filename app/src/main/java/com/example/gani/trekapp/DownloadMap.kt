package com.example.gani.trekapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
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
import java.nio.charset.Charset

/**
 * Download and view an offline map using the Mapbox Android SDK.
 */
class DownloadMap : AppCompatActivity() {

    private var isEndNotified: Boolean = false
    private var trekName: String? = null
    private var fileName: String? = null
    private var imageFile: String? = null
    private var trekId: String? = null
    private var progressBar: ProgressBar? = null
    private var trekData: JSONObject? = null
    private var trekImageJ: JSONObject? = null

    private var offlineManager: OfflineManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trekId = intent.getStringExtra("trekId")          // At present sending the id as a string
        fileName = "${filesDir}/trekData_$trekId"
        imageFile = "${filesDir}/trekImge_$trekId.jpg"


        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_download_map)

        //     Set up the OfflineManager
        offlineManager = OfflineManager.getInstance(this)

        progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
        progressBar?.visibility = View.INVISIBLE

        val file = File(fileName)
        if(!file.exists()){
            start_button.visibility = View.INVISIBLE
        }

        downloadImage()

        var imgFile = File(imageFile)
        if(imgFile.exists()){
            var imgStr = imgFile.readText()
            //Toast.makeText(this, imgStr, Toast.LENGTH_LONG).show()
            //var bitmap: Bitmap? = BitmapFactory.decodeFile(imgFile.absolutePath)!!
            //imageView.setImageBitmap(bitmap)
        }

        start_button.setOnClickListener {
            getTrekData(true, false, false)
        }

        download_button.setOnClickListener {
            getTrekData(false, true, true, 19.136674, 72.913977, 19.144813, 72.920681)
        }

        delete_button.setOnClickListener {
            deleteMap()
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
//                .include(LatLng(19.144813, 72.920681)) // Northeast // main latts
//                .include(LatLng(19.136674, 72.913977)) // Southwest
                //.include(LatLng(23.36, 85.335)) // Northeast
                //.include(LatLng(23.31, 85.284)) // Southwest
                .build()

        // Define the offline region
//            Toast.makeText(this, mapboxMap.styleUrl, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, getString(R.string.mapBox_styleUrl_satellite), Toast.LENGTH_SHORT).show()
        val definition = OfflineTilePyramidRegionDefinition(
//                    mapboxMap.styleUrl,
                getString(R.string.mapBox_styleUrl_satellite),
                latLngBounds,
                16.0,
                19.0,
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
//                        progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
                        startProgress()

                        Log.i("DownloadMap", "entered create offline map")

//                        offlineRegion.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
//
//
//
//                            override fun onStatus(offlineRegionStatus: OfflineRegionStatus) {
//                                Log.i("DownloadMap", offlineRegionStatus.requiredResourceCount.toString())
//
//                                if (offlineRegionStatus.isComplete) {
//                                    Log.e("DownloadMap", "Offline Map Data found, not downloading it again")
//                                } else {
//                                    if (offlineRegionStatus.downloadState == OfflineRegion.STATE_ACTIVE) {
//                                        Log.e("DownloadMap", "Offline Map Data download still running, not downloading it twice")
//                                    } else {
//                                        Log.e("DownloadMap", "Map Data found but incomplete, resume download")
////                                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
//                                    }
//                                }
//                            }
//
//                            override fun onError(error: String) {
//                                Log.e("DownloadMap", "onStatusError: $error, try downloading map")
//                            }
//
//                        })

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
                                    Log.i("DownloadMap", status.requiredResourceCount.toString())

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

    private fun downloadImage(){
        val url = GlobalVariables().trekImageUrl
        val finalUrl = "$url?trek_id=$trekId"
        val requestQueue = Volley.newRequestQueue(this)
        val jsonRequest = JsonObjectRequest(Request.Method.GET, finalUrl, null, Response.Listener<JSONObject>{ response ->

            print(response)
            trekImageJ = response
            Log.i("TrekImage", response.getString("status"))

            if (response.getString("status") == "success"){
                //Toast.makeText(this, imageFile, Toast.LENGTH_SHORT).show()
                var imgFile = File(imageFile)

                var stringRec: String = response.getString("img_data")
                Log.i("originalDAta: ", stringRec)
                stringRec.replace("data:image/jpg;base64,", "").replace("data:image/jpeg;base64,", "")
                var decodedString: ByteArray = Base64.decode(stringRec, Base64.URL_SAFE)
                Log.i("ByteArray", decodedString.toString())
                //var bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                //imageView.setImageBitmap(bitmap)
//                imgFile.writeText(response.getString("img_data").toString())
//                    displayTreks(response)
                //var bitmap: Bitmap = BitmapFactory.decodeByteArray(response.getString("img_data").toByteArray(),0,response.getString("img_data").toByteArray().size)
                //var imgReadFile = File(imageFile)
                /*var hexString = response.getString("img_data")
                val string: CharArray = CharArray((hexString.length-3)/2)
                for(i in 3..(hexString.length - 1))
                {
                    if(i % 2 == 0){
                        if(i > 2){
                            var char1 = hexString.get(i-1)
                            var char2 = hexString.get(i)
                            var byteData: String = "$char1$char2"
                            //Log.i("the string : ", byteData)
                            //Log.i("The number is", Integer.parseInt(byteData, 16).toString())

                            var ch = Integer.parseInt(byteData, 16).toChar()
                            //Log.i("The index", byteData.toString())
                            string[(i/2) - 2] = ch
                        }
                    }
                }
                Toast.makeText(this, string.toString(), Toast.LENGTH_SHORT).show()
                Log.i("the image data ******", string.size.toString())
                Log.i("the image data ******", string.toString())
                imgFile.writeText(string.toString(), Charsets.US_ASCII)*/
                //var charData = hexString.toCharArray()
                //var data: ByteArray = response.getString("img_data").toByteArray()
                //var data: ByteArray = string.toString().toByteArray()
                //Toast.makeText(this, data.size.toString(), Toast.LENGTH_LONG)
//                var bitmap: Bitmap = BitmapFactory.decodeFile(imageFile)
                //var bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

            }
            else{

//                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()

                Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show()

            }

        }, Response.ErrorListener {
            //val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)

            Toast.makeText(this, "Error Connecting to server for Trek_image", Toast.LENGTH_SHORT).show()

            //if (sharedPreferences.contains("treksAvailable")) {
            //displayTreks(JSONObject(sharedPreferences.getString("treksAvailable", "")))

            //}
        })

        requestQueue.add(jsonRequest)
    }

    private fun getTrekData(gotoMapFlag: Boolean, downloadMap: Boolean, calcBound: Boolean, latmin: Double=0.0, lonmin: Double=0.0, latmax: Double=0.0, lonmax: Double=0.0):Boolean{

//        val sharedPreferences = getSharedPreferences("TrekApp", Context.MODE_PRIVATE)
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
                    if(calcBound){
                        Toast.makeText(this, trekData?.toString(), Toast.LENGTH_LONG).show()
                        val Latmin = trekData?.getDouble("sw-lat")!!
                        val Lonmin = trekData?.getDouble("sw-long")!!
                        val Latmax = trekData?.getDouble("ne-lat")!!
                        val Lonmax = trekData?.getDouble("ne-long")!!
                        downloadMap(Latmin, Lonmin, Latmax, Lonmax)
                    }
                    else {
                        downloadMap(latmin, lonmin, latmax, lonmax)
                    }
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

        return true
    }

    private fun saveTrekData(response: JSONObject) {
        val file = File(fileName)
        file.writeText(response.toString())
    }

    public override fun onPause() {
        super.onPause()
//        mapView!!.onPause()
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

    fun deleteMap(){

        if (offlineManager != null) {
            offlineManager!!.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
                override fun onList(offlineRegions: Array<OfflineRegion>) {

                    Log.i("DownloadMap", offlineRegions.size.toString())

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
}