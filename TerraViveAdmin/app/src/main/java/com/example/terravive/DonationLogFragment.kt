package com.example.terravive

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// ------------------ Fragment ------------------

class DonationLogFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var donationLogAdapter: DonationLogAdapter
    private lateinit var moneySpinner: Spinner
    private lateinit var noLogsTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_donations, container, false)

        recyclerView = view.findViewById(R.id.donation_logs_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        moneySpinner = view.findViewById(R.id.money_spinner)
        noLogsTextView = view.findViewById(R.id.no_logs_text_view)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.donation_categories,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moneySpinner.adapter = spinnerAdapter

        loadDonationLogs()
        return view
    }

    private fun loadDonationLogs() {
        val donationLogs = getDonationLogs()
        if (donationLogs.isEmpty()) {
            recyclerView.visibility = View.GONE
            noLogsTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noLogsTextView.visibility = View.GONE
            donationLogAdapter = DonationLogAdapter(requireContext(), donationLogs)
            recyclerView.adapter = donationLogAdapter
        }
    }

    private fun getDonationLogs(): List<DonationLog> {
        return listOf(
            DonationLog("Donor 1", "donor1@example.com"),
            DonationLog("Donor 2", "donor2@example.com"),
            DonationLog("Donor 3", "donor3@example.com")
        )
    }
}

// ------------------ Data Class ------------------

data class DonationLog(
    val donorName: String,
    val donorEmail: String
)

// ------------------ Adapter ------------------

class DonationLogAdapter(
    private val context: Context,
    private val donationLogs: List<DonationLog>
) : RecyclerView.Adapter<DonationLogAdapter.DonationLogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_donation, parent, false)
        return DonationLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationLogViewHolder, position: Int) {
        val log = donationLogs[position]
        holder.donorNameTextView.text = log.donorName
        holder.emailTextView.text = log.donorEmail
    }

    override fun getItemCount(): Int = donationLogs.size

    class DonationLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val donorNameTextView: TextView = itemView.findViewById(R.id.donor_name_view)
        val emailTextView: TextView = itemView.findViewById(R.id.donor_email_view)
    }
}
