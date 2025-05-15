package com.example.terravive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateActivityFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSubmit: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_activity, container, false)

        etName = view.findViewById(R.id.etActivityTitle)
        etDate = view.findViewById(R.id.etActivityDate)
        etTime = view.findViewById(R.id.etActivityTime)
        etLocation = view.findViewById(R.id.etActivityLocation)
        btnSubmit = view.findViewById(R.id.btnSubmitActivity)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString()
            val date = etDate.text.toString()
            val time = etTime.text.toString()
            val location = etLocation.text.toString()
            val email = auth.currentUser?.email ?: "unknown"

            if (name.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = db.collection("activities_approved").document().id
            val activity = hashMapOf(
                "id" to id,
                "name" to name,
                "date" to date,
                "time" to time,
                "location" to location,
                "organizer" to email,
                "status" to "pending" // Assume activities are initially pending approval
            )

            db.collection("activities_approved").document(id).set(activity)
                .addOnSuccessListener {
                    Toast.makeText(context, "Activity submitted for approval!", Toast.LENGTH_SHORT).show()

                    // Navigate back to the previous fragment (OrganizerActivitiesFragment)
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
