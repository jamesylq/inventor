package com.example.inventor

import QRCodeData
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        createdBy = username,
        createdTime = System.currentTimeMillis(),
        updateTime = System.currentTimeMillis(),
        currentWith = username,
        status = if (location == homeLocation) 1 else 0,
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

            logItemUpdate(
                itemName = safeName,
                action = Constants.ITEM_CREATION,
                details = "Location: $location, Home: $homeLocation, Remarks: $remarks"
            )

            onComplete?.invoke()
        }
        .addOnFailureListener { e ->
            Log.wtf("InventorCore", "Failed to save QR code: ${e.localizedMessage}")
        }
}

fun logItemUpdate(itemName: String, action: Int, details: String = "") {
    val auth = FirebaseAuth.getInstance()
    val userName = auth.currentUser?.displayName ?: "unknown"

    val safeName = itemName.replace(".", "_")
        .replace("#", "_")
        .replace("$", "_")
        .replace("[", "_")
        .replace("]", "_")

    val timestamp = System.currentTimeMillis()
    val timeString = SimpleDateFormat(
        "MMM dd, yyyy h:mma", Locale.getDefault()
    ).format(Date(timestamp))

    val logEntry = LogEntry(
        user = userName,
        action = action,
        details = details,
        timestamp = timestamp,
        timeString = timeString
    )

    val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
    val timeKey = sdf.format(Date(timestamp))
    val safeUser = userName.replace(Regex("[^A-Za-z0-9]"), "_")
    val logKey = "${timeKey}_$safeUser"

    val logRef = database.reference.child("logs")
        .child("items")
        .child(safeName)
        .child(logKey)

    logRef.setValue(logEntry)
        .addOnSuccessListener {
            // Update lastUpdate timestamp (still in millis for easy comparison)
            database.reference.child("logs").child("lastUpdate").setValue(timestamp)
            Log.d("InventorCore", "Logged update for $safeName at $timeString")
        }
        .addOnFailureListener {
            Log.e("InventorCore", "Failed to log update: ${it.localizedMessage}")
        }
}


fun validateAndCleanLogs() {
    val rootLogsRef = database.reference.child("logs")
    val itemsRef = rootLogsRef.child("items")

    rootLogsRef.child("lastUpdate").get()
        .addOnSuccessListener { snapshot ->
            val lastUpdate = snapshot.getValue(Long::class.java) ?: 0L
            val now = System.currentTimeMillis()
            val weekMillis = 7 * 24 * 60 * 60 * 1000L

            if (now - lastUpdate <= weekMillis) return@addOnSuccessListener

            Log.w("InventorCore", "Cleaning old logs...")

            itemsRef.get()
                .addOnSuccessListener { itemsSnap ->
                    for (itemSnapshot in itemsSnap.children) {
                        val itemKey = itemSnapshot.key ?: continue
                        val itemLogsRef = itemsRef.child(itemKey)

                        val logsList = mutableListOf<Pair<String, Long>>()

                        for (logSnapshot in itemSnapshot.children) {
                            val ts = logSnapshot.child("timestamp").getValue(Long::class.java)
                            if (ts != null) {
                                logsList.add(logSnapshot.key!! to ts)
                            }
                        }

                        logsList.sortByDescending { it.second }

                        val updates = hashMapOf<String, Any?>()
                        for (i in 5 until logsList.size) {
                            val (logKey, timestamp) = logsList[i]
                            if (now - timestamp > weekMillis) {
                                updates[logKey] = null
                            }
                        }

                        if (updates.isNotEmpty()) {
                            itemLogsRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    Log.i(
                                        "InventorCore",
                                        "Deleted ${updates.size} old logs for $itemKey"
                                    )
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "InventorCore",
                                        "Failed deleting logs for $itemKey: ${it.message}"
                                    )
                                }
                        }
                    }

                    rootLogsRef.child("lastUpdate").setValue(now)
                }
                .addOnFailureListener {
                    Log.e("InventorCore", "Failed to read items logs: ${it.message}")
                }
        }
        .addOnFailureListener {
            Log.e("InventorCore", "Failed to fetch lastUpdate: ${it.localizedMessage}")
        }
}
