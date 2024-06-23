package com.example.ssc_project

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File

data class ScanItem(val title: String, val description: String, val filePath: String)


class ScanHistoryAdapter(private val context: Context, private val scanList: List<ScanItem>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<ScanHistoryAdapter.ScanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_scan_history, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scanItem = scanList[position]
        holder.titleTextView.text = scanItem.title
        holder.descriptionTextView.text = scanItem.description

        // Load and display the image
        val bitmap = BitmapFactory.decodeFile(scanItem.filePath)
        holder.iconImageView.setImageBitmap(bitmap)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(scanItem)
        }


        holder.downloadImageView.setOnClickListener {
            // Handle download action
            Toast.makeText(context, "Download ${scanItem.title}", Toast.LENGTH_SHORT).show()
        }
        holder.deleteImageView.setOnClickListener {
            // Handle delete action
            val file = File(scanItem.filePath)
            if (file.exists()) {
                file.delete()
                Toast.makeText(context, "Deleted ${scanItem.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(scanItem: ScanItem)
    }

    override fun getItemCount() = scanList.size

    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.scanTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.scanDescription)
        val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
        val downloadImageView: ImageView = itemView.findViewById(R.id.downloadIcon)
        val deleteImageView: ImageView = itemView.findViewById(R.id.deleteIcon)
    }
}
