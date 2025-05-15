package com.example.terravive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.terravive.databinding.ActivityDashboardBinding

class AdminDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default fragment
        replaceFragment(AdminHomeFragment())

        binding.adminBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_admin_donations -> {
                    replaceFragment(DonationLogFragment())
                    true
                }
                R.id.admin_activities -> {
                    replaceFragment(AdminActivityFragment())
                    true
                }
                R.id.reports -> {
                    replaceFragment(ReportsFragment())
                    true
                }
                R.id.admin_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
