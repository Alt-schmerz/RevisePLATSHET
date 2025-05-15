package com.example.terravive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terravive.R
import com.example.terravive.ActivityItemModel

class ActivityAdapter(
    private val activityList: MutableList<ActivityItemModel>,
    private val onApproveClick: (ActivityItemModel) -> Unit,
    private val onDenyClick: (ActivityItemModel) -> Unit,
    private val onDeleteClick: ((ActivityItemModel) -> Unit)? = null,
    private val isAdmin: Boolean = false
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.activityNameTextView)
        val dateText: TextView = itemView.findViewById(R.id.activityDateTextView)
        val statusText: TextView = itemView.findViewById(R.id.activityStatusTextView)
        val approveButton: Button = itemView.findViewById(R.id.approveButton)
        val denyButton: Button = itemView.findViewById(R.id.denyButton)
        val deleteButton: Button = itemView.findViewById(R.id.delButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activityList[position]

        holder.nameText.text = "Name: ${activity.title}"
        holder.dateText.text = "Date: ${activity.date}"
        holder.statusText.text = "Status: ${if (activity.isApproved) "Approved" else "Pending"}"

        if (isAdmin) {
            holder.approveButton.visibility = View.VISIBLE
            holder.denyButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.GONE

            holder.approveButton.setOnClickListener { onApproveClick(activity) }
            holder.denyButton.setOnClickListener { onDenyClick(activity) }

        } else {
            // Organizer
            holder.approveButton.visibility = View.GONE
            holder.denyButton.visibility = View.GONE

            if (activity.isOwner()) {
                holder.deleteButton.visibility = View.VISIBLE
                holder.deleteButton.setOnClickListener { onDeleteClick?.invoke(activity) }
            } else {
                holder.deleteButton.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = activityList.size

    fun updateActivities(newActivities: List<ActivityItemModel>) {
        activityList.clear()
        activityList.addAll(newActivities)
        notifyDataSetChanged()
    }
}
