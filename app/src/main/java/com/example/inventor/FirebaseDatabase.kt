package com.example.inventor

import QRCodeData
import android.util.Log

fun saveNewItem(
    name: String,
    location: String,
    homeLocation: String,
    remarks: String,
    onComplete: (() -> Unit)? = null
) {
    val username = auth.currentUser?.displayName ?: "UnknownUser"

    // Sanitize the name to make it a valid Firebase key
    val safeName = name.replace(".", "_")
        .replace("#", "_")
        .replace("$", "_")
        .replace("[", "_")
        .replace("]", "_")

    val qrData = QRCodeData(
        createdBy = username,            // username instead of UID
        createdTime = System.currentTimeMillis(),
        updateTime = System.currentTimeMillis(),
        currentWith = username,          // initially assigned to creator
        status = 0,                      // 0 = available, 1 = checked out, etc.
        name = name,
        location = location,
        homeLocation = homeLocation,
        remarks = remarks
    )

    database.reference
        .child("qrcodes")
        .child(safeName)
        .setValue(qrData)
        .addOnSuccessListener {
            Log.w("InventorCore", "QR code saved with name $safeName by $username")
            onComplete?.invoke()
        }
        .addOnFailureListener { e ->
            Log.wtf("InventorCore", "Failed to save QR code: ${e.localizedMessage}")
        }
}
