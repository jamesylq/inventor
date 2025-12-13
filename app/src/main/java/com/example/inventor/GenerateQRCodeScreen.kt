package com.example.inventor

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.OutputStream

@Composable
fun GenerateQRScreen(navController: NavController) {
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }

    // Location picker state
    var homeLocation by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var otherText by remember { mutableStateOf("") }

    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showQRDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MainTopAppBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Generate QR Code", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Equipment ID
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Equipment ID or Info") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Home Location (Picker)
            LocationPickerEditor(
                itemLocations = itemLocations,
                selectedLocation = selectedLocation,
                onSelectedLocationChange = {
                    selectedLocation = it
                    if (it != null) homeLocation = it
                },
                otherText = otherText,
                onOtherTextChange = {
                    otherText = it
                    if (it.isNotBlank()) homeLocation = it
                },
                onSaveRequested = { selected ->
                    homeLocation = selected ?: otherText
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Generate Button
            Button(
                onClick = {
                    if (inputText.isNotBlank() && homeLocation.isNotBlank()) {
                        // Generate QR bitmap
                        qrBitmap = generateQRBitmap(inputText)

                        // Save item to Firebase
                        saveNewItem(
                            name = inputText,
                            location = homeLocation,
                            homeLocation = homeLocation,
                            remarks = "No Remarks Found"
                        )

                        // Show dialog
                        showQRDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = inputText.isNotBlank() && homeLocation.isNotBlank()
            ) {
                Text("Generate")
            }
        }
    }

    // =========================
    // QR Code AlertDialog
    // =========================
    if (showQRDialog && qrBitmap != null) {
        QRCodeDialog(
            bitmap = qrBitmap!!,
            filename = "$inputText.png",
            onDismiss = {
                showQRDialog = false
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}

// =========================
// Save bitmap to gallery
// =========================
fun saveBitmapToGallery(context: android.content.Context, bitmap: android.graphics.Bitmap, filename: String) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/InventorQR")
        }
    }

    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    imageUri?.let {
        val outputStream: OutputStream? = resolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            Toast.makeText(context, "QR saved to gallery", Toast.LENGTH_SHORT).show()
        }
    } ?: run {
        Toast.makeText(context, "Failed to save QR", Toast.LENGTH_SHORT).show()
    }
}
