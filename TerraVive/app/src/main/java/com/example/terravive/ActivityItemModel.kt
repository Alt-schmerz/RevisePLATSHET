package com.example.terravive

import com.google.firebase.auth.FirebaseAuth

data class ActivityItemModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var time: String = "",
    var location: String = "",   // Newly supported in dialog_edit_activity.xml
    var userId: String = "",
    var isApproved: Boolean = false,
    var status: String = ""      // Can be used to represent "approved", "denied", etc.
) {
    // Utility to check if the activity belongs to the current Firebase user
    fun isOwner(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.uid == userId
    }
}
