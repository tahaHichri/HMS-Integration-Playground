package com.hishri.hmsplayground

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import kotlinx.android.synthetic.main.toolbar.*
import java.util.HashMap

class MainActivity : AppCompatActivity() {


    // permissions for accessing storage and camera
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    companion object {
        private val MIME_TYPE_MAP: MutableMap<String, String> = HashMap()
        private const val REQUEST_SIGN_IN_LOGIN = 999
        private const val TAG = "MainActivity"

        // accepted MIME types
        init {
            MIME_TYPE_MAP.apply {
                put(".doc", "application/msword")
                put(".jpg", "image/jpeg")
                put(".mp3", "audio/x-mpeg")
                put(".mp4", "video/mp4")
                put(".pdf", "application/pdf")
                put(".png", "image/png")
                put(".txt", "text/plain")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setting up the action bar
        setSupportActionBar(toolbar)
        toolbar.title = "HMS Playground"
        toolbar.subtitle = "HUAWEI ID and DriveKIT"



        // Accessing the storage require explicit permission grant for API >= 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_STORAGE, 1)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}