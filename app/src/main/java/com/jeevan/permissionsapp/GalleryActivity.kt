package com.jeevan.permissionsapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeevan.permissionsapp.Adapters.GalleryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryActivity : AppCompatActivity() {

    lateinit var galleryAdapter: GalleryAdapter
    lateinit var galleryRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) {
            var imageUris = getImageUris()
            withContext(Dispatchers.Main) {
                galleryAdapter = GalleryAdapter(imageUris)
                galleryRecyclerView.adapter = galleryAdapter
            }
        }
        galleryAdapter = GalleryAdapter(mutableListOf())
        galleryRecyclerView.adapter = galleryAdapter
        galleryRecyclerView.layoutManager =
            GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false)
    }

    @SuppressLint("Range")
    private fun getImageUris(): List<Uri> {
        var uriList: ArrayList<Uri> = arrayListOf()
        var imageCursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (imageCursor?.count!! > 0) {
            while (imageCursor.moveToNext()) {
                Log.d(
                    ContentValues.TAG, "onStart: Jeevan: ${
                        imageCursor.getString(
                            imageCursor.getColumnIndex(
                                MediaStore.Images.ImageColumns.DATA
                            )
                        )
                    }"
                )
                uriList.add(
                    Uri.fromFile(
                        File(
                            imageCursor.getString(
                                imageCursor.getColumnIndex(
                                    MediaStore.Images.ImageColumns.DATA
                                )
                            )
                        )
                    )
                )
            }
        }
        return uriList
    }
}