package com.example.inventor

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.zxing.integration.android.IntentIntegrator

@Composable
fun QRScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var scannedResult by remember { mutableStateOf<String?>(null) }

    BackHandler {
        navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true } // clear scanner from backstack
            launchSingleTop = true
        }
    }

    // Launcher for ZXing scanner
    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, intent)

        if (scanResult == null || scanResult.contents == null) {
            // User cancelled the scan (pressed back while scanner was active)
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // User successfully scanned a QR code
            val equipmentName = scanResult.contents
            scannedResult = equipmentName
            Toast.makeText(context, "Scanned: $equipmentName", Toast.LENGTH_LONG).show()

            navController.navigate("modifyitem/$equipmentName")
        }
    }


    // Launcher for camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            activity?.let { startScanner(it, scannerLauncher) }
        } else {
            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
        }
    }

    // Check permission on first composition
    LaunchedEffect(Unit) {
        activity?.let {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) -> startScanner(it, scannerLauncher)
                else -> permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("QR Scanner", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        scannedResult?.let { name ->
            Text("Scanned: $name", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Helper function to start ZXing scanner
private fun startScanner(activity: Activity, launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
    val integrator = IntentIntegrator(activity)
    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
    integrator.setBeepEnabled(true)
    integrator.setPrompt("Scan equipment QR code")
    launcher.launch(integrator.createScanIntent())
}
