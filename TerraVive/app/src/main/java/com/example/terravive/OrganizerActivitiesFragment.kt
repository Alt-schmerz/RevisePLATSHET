package com.example.terravive

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terravive.OrganizerActivityAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar


class OrganizerActivitiesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrganizerActivityAdapter
    private val activityList = mutableListOf<ActivityItemModel>()
    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val undoTimers = mutableMapOf<String, Runnable>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizer_activities, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewActivities)

        adapter = OrganizerActivityAdapter(
            activityList,
            onEditClick = { activity -> editActivity(activity) },
            onUndoClick = { activity -> undoActivity(activity) }, // <--- Add this
            onDeleteClick = { activity -> deleteActivity(activity) }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadActivities()

        // Handle FAB or button for adding activity (optional)
        view.findViewById<View>(R.id.btnAddActivity)?.setOnClickListener {
            showCreateActivityDialog()
        }

        return view
    }

    private fun loadActivities() {
        db.collection("activities_pending").get().addOnSuccessListener { result ->
            activityList.clear()
            for (doc in result) {
                val item = doc.toObject(ActivityItemModel::class.java)
                activityList.add(item)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error loading activities: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCreateActivityDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_activity, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val dateInput = dialogView.findViewById<EditText>(R.id.editTextDate)
        val timeInput = dialogView.findViewById<EditText>(R.id.editTextTime)
        val locationInput = dialogView.findViewById<EditText>(R.id.editTextLocation)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Date Picker
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formatted = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                    dateInput.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Picker
        timeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    val formatted = "%02d:%02d".format(hour, minute)
                    timeInput.setText(formatted)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Create Activity")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val id = UUID.randomUUID().toString()
                val newActivity = ActivityItemModel(
                    id = id,
                    title = titleInput.text.toString(),
                    description = descriptionInput.text.toString(),
                    date = dateInput.text.toString(),
                    time = timeInput.text.toString(),
                    location = locationInput.text.toString(),
                    userId = currentUserId,
                    isApproved = false
                )
                submitActivity(newActivity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun submitActivity(activity: ActivityItemModel) {
        db.collection("activities_pending").document(activity.id).set(activity).addOnSuccessListener {
            activityList.add(activity)
            adapter.notifyItemInserted(activityList.size - 1)

            // Allow undo within 90s
            val undoRunnable = Runnable {
                undoTimers.remove(activity.id) // Remove from undo map after time passes
            }

            undoTimers[activity.id] = undoRunnable
            handler.postDelayed(undoRunnable, 90_000) // 1 min 30 sec
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error submitting activity: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteActivity(activity: ActivityItemModel) {
        // Allow undo only if within 90s
        val canUndo = undoTimers.containsKey(activity.id)
        db.collection("activities_approved").document(activity.id).delete().addOnSuccessListener {
            activityList.remove(activity)
            adapter.notifyDataSetChanged()

            if (canUndo) {
                undoTimers[activity.id]?.let { handler.removeCallbacks(it) }
                undoTimers.remove(activity.id)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error deleting activity: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editActivity(activity: ActivityItemModel) {
        if (!undoTimers.containsKey(activity.id)) {
            Toast.makeText(context, "This activity can no longer be edited.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_activity, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val dateInput = dialogView.findViewById<EditText>(R.id.editTextDate)
        val timeInput = dialogView.findViewById<EditText>(R.id.editTextTime)
        val locationInput = dialogView.findViewById<EditText>(R.id.editTextLocation)

        titleInput.setText(activity.title)
        descriptionInput.setText(activity.description)
        dateInput.setText(activity.date)
        timeInput.setText(activity.time)
        locationInput.setText(activity.location)

        // Date Picker
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formatted = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                    dateInput.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Picker
        timeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    val formatted = "%02d:%02d".format(hour, minute)
                    timeInput.setText(formatted)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Activity")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedActivity = activity.copy(
                    title = titleInput.text.toString(),
                    description = descriptionInput.text.toString(),
                    date = dateInput.text.toString(),
                    time = timeInput.text.toString(),
                    location = locationInput.text.toString()
                )
                db.collection("activities_approved").document(updatedActivity.id).set(updatedActivity)
                    .addOnSuccessListener {
                        val index = activityList.indexOfFirst { it.id == updatedActivity.id }
                        if (index != -1) {
                            activityList[index] = updatedActivity
                            adapter.notifyItemChanged(index)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error saving activity: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun undoActivity(activity: ActivityItemModel) {
        if (!undoTimers.containsKey(activity.id)) {
            Toast.makeText(context, "Undo time expired.", Toast.LENGTH_SHORT).show()
            return
        }

        // Remove from Firestore
        db.collection("activities_approved").document(activity.id).delete().addOnSuccessListener {
            // Remove from list
            val index = activityList.indexOfFirst { it.id == activity.id }
            if (index != -1) {
                activityList.removeAt(index)
                adapter.notifyItemRemoved(index)
            }

            // Stop the 90s timer
            undoTimers[activity.id]?.let { handler.removeCallbacks(it) }
            undoTimers.remove(activity.id)

            Toast.makeText(context, "Activity submission undone.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Undo failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
