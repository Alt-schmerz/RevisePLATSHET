package com.example.terravive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terravive.adapter.ActivityAdapter
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var activitiesList: MutableList<ActivityItemModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_activity, container, false)

        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        activitiesList = mutableListOf()
        activityAdapter = ActivityAdapter(
            activitiesList,
            ::onApproveClick,
            ::onDenyClick,
            null,       // onDeleteClick is optional and not needed for Admin
            true        // isAdmin = true for AdminActivityFragment
        )

        recyclerView.adapter = activityAdapter

        loadActivities()

        return view
    }

    private fun loadActivities() {
        val db = FirebaseFirestore.getInstance()
        db.collection("activities_pending")
            .get()
            .addOnSuccessListener { documents ->
                activitiesList.clear()
                for (document in documents) {
                    val activity = document.toObject(ActivityItemModel::class.java)
                    activity.id = document.id  // Ensure 'id' is assigned for transfer
                    activitiesList.add(activity)
                }
                activityAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching activities: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onApproveClick(activity: ActivityItemModel) {
        val db = FirebaseFirestore.getInstance()

        // Change the status to "approved"
        activity.isApproved = true

        // Move the activity to 'activities_approved'
        db.collection("activities_approved")
            .document(activity.id)
            .set(activity)
            .addOnSuccessListener {
                // Remove from 'activities_pending'
                db.collection("activities_pending")
                    .document(activity.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Activity approved!", Toast.LENGTH_SHORT).show()
                        loadActivities()  // Reload activities after update
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete from pending: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to approve: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onDenyClick(activity: ActivityItemModel) {
        val db = FirebaseFirestore.getInstance()

        // Change the status to "denied"
        activity.isApproved = false

        db.collection("activities_denied")
            .document(activity.id)
            .set(activity)
            .addOnSuccessListener {
                // Remove from 'activities_pending'
                db.collection("activities_pending")
                    .document(activity.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Activity denied!", Toast.LENGTH_SHORT).show()
                        loadActivities()  // Reload activities after update
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete from pending: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to deny: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
