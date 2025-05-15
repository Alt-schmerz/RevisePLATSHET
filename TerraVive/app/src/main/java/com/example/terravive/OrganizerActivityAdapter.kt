package com.example.terravive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class OrganizerActivityAdapter(
    private var activityList: MutableList<ActivityItemModel>,
    private val onEditClick: (ActivityItemModel) -> Unit,
    private val onDeleteClick: (ActivityItemModel) -> Unit,
    private val onUndoClick: (ActivityItemModel) -> Unit // <-- Added this parameter
) : RecyclerView.Adapter<OrganizerActivityAdapter.OrganizerActivityViewHolder>() {

    inner class OrganizerActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tvActivityTitle)
        val dateText: TextView = itemView.findViewById(R.id.tvActivityDate)
        val descriptionText: TextView = itemView.findViewById(R.id.tvActivityDescription)
        val editButton: Button = itemView.findViewById(R.id.btnEditActivity)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteActivity)
        val undoButton: Button = itemView.findViewById(R.id.btnUndoActivity) // <-- Added Undo button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizerActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_organizer_activity, parent, false)
        return OrganizerActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganizerActivityViewHolder, position: Int) {
        val activity = activityList[position]
        holder.titleText.text = "Title: ${activity.title}"
        holder.dateText.text = "Date: ${activity.date}"
        holder.descriptionText.text = "Description: ${activity.description}"

        // Set up listeners for Edit, Delete, and Undo
        holder.editButton.setOnClickListener { onEditClick(activity) }
        holder.deleteButton.setOnClickListener { onDeleteClick(activity) }
        holder.undoButton.setOnClickListener { onUndoClick(activity) } // <-- Undo button click handler
    }

    override fun getItemCount(): Int = activityList.size

    fun updateActivities(newActivities: List<ActivityItemModel>) {
        activityList.clear()
        activityList.addAll(newActivities)
        notifyDataSetChanged()
    }
}
