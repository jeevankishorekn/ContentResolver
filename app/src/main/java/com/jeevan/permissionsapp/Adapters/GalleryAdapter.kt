package com.jeevan.permissionsapp.Adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jeevan.permissionsapp.R

class GalleryAdapter(private var data: List<Uri>) :
    RecyclerView.Adapter<GalleryAdapter.GalleryAdapterViewHolder>() {
    inner class GalleryAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var galleryImage: ImageView

        init {
            galleryImage = view.findViewById(R.id.gallery_img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryAdapterViewHolder {
        return GalleryAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GalleryAdapterViewHolder, position: Int) {
        holder.galleryImage.setImageURI(data[position])
    }

    override fun getItemCount(): Int = data.size
}