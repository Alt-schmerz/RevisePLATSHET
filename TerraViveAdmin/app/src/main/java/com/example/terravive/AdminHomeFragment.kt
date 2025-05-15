package com.example.terravive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Intent

class AdminHomeFragment : Fragment() {

    private lateinit var tvAdminProfile: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        tvAdminProfile = view.findViewById(R.id.tvAdminProfile)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvAdminProfile.setOnClickListener {
            val intent = Intent(requireContext(), AdminProfileActivityscrap::class.java)
            startActivity(intent)
        }
    }
}
