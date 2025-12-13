package com.example.inventor

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

val itemLocations = listOf("Hall", "Audi", "Media Room", "Hall Control Room", "Audi Control Room")

fun statusToText(status: Int): String {
    return when (status) {
        0 -> "Checked Out"
        1 -> "Available"
        else -> "Unknown"
    }
}

fun formatTime(time: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy h:mma", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(time))
}

fun generateQRBitmap(
    content: String,
    size: Int = 512
): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val bitmap = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.RGB_565
    )

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            )
        }
    }

    return bitmap
}
