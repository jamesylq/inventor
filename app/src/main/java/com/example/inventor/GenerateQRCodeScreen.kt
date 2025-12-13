package com.example.inventor

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
    var homeLocation by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    Scaffold(
        topBar = {
            MainTopAppBar(navController = navController)
        }
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

            Spacer(modifier = Modifier.height(12.dp))

            // Home Location
            OutlinedTextField(
                value = homeLocation,
                onValueChange = { homeLocation = it },
                label = { Text("Home Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            // ===== QR CODE GENERATION (UNCHANGED) =====
                            val writer = QRCodeWriter()
                            val bitMatrix = writer.encode(
                                inputText,
                                BarcodeFormat.QR_CODE,
                                512,
                                512
                            )

                            val bmp = android.graphics.Bitmap.createBitmap(
                                512,
                                512,
                                android.graphics.Bitmap.Config.RGB_565
                            )

                            for (x in 0 until 512) {
                                for (y in 0 until 512) {
                                    bmp.setPixel(
                                        x,
                                        y,
                                        if (bitMatrix[x, y])
                                            android.graphics.Color.BLACK
                                        else
                                            android.graphics.Color.WHITE
                                    )
                                }
                            }

                            qrBitmap = bmp

                            // ===== SAVE ITEM TO FIREBASE =====
                            saveNewItem(
                                name = inputText,
                                location = "unknown",
                                homeLocation = homeLocation.ifBlank { "unknown" },
                                remarks = "unknown"
                            )
                        }
                    }
                ) {
                    Text("Generate")
                }

                qrBitmap?.let { bmp ->
                    Button(
                        onClick = {
                            saveBitmapToGallery(context, bmp, "$inputText.png")
                        }
                    ) {
                        Text("Download")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            qrBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Generated QR Code",
                    modifier = Modifier.size(256.dp)
                )
            }
        }
    }
}


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
