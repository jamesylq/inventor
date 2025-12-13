package com.example.inventor

import QRCodeData
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.net.URLDecoder

@Composable
fun ViewItemScreen(
    navController: NavController,
    encodedName: String
) {
    val database = FirebaseDatabase.getInstance(
        "https://inventor-36c3a-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    var item by remember { mutableStateOf<QRCodeData?>(null) }
    val decodedName = URLDecoder.decode(encodedName, "UTF-8")

    var showQRDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Fetch item once
    LaunchedEffect(decodedName) {
        database.child("qrcodes").child(decodedName).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedItem = snapshot.getValue(QRCodeData::class.java)
                    if (fetchedItem != null) {
                        item = fetchedItem
                    } else {
                        Log.w("InventorCore", "Item not found: $decodedName")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventorCore", "Failed to fetch item: ${error.message}")
                }
            }
        )
    }

    Scaffold (
        topBar = {
            MainTopAppBar(
                navController = navController,
                dest = "itemsearcher"
            )
        }
    ) {
        if (showQRDialog && qrBitmap != null) {
            QRCodeDialog(
                bitmap = qrBitmap!!,
                filename = "${decodedName}.png",
                onDismiss = { showQRDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(it),
            verticalArrangement = Arrangement.Top
        ) {

            item?.let { i ->

                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(i.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            navController.navigate("modifyitem/${Uri.encode(i.name)}")
                        }
                    ) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Status
                ElevatedCard (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("modifyitem/${Uri.encode(i.name)}")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Text(
                                "Status: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                statusToText(i.status),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(4.dp))

                        // Location
                        Row {
                            Text(
                                "Location: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                i.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(4.dp))

                        // Current With
                        Row {
                            Text(
                                "Current With: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                i.currentWith,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(4.dp))

                        // Current With
                        Row {
                            Text(
                                "Home Location: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                i.homeLocation,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Remarks: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            i.remarks,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))

                ElevatedCard (modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Text(
                                "Created By: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                i.createdBy,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(4.dp))

                        Row {
                            Text(
                                "Created: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatTime(i.createdTime),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Last Updated: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatTime(i.updateTime),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        qrBitmap = generateQRBitmap(i.name)
                        showQRDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show QR Code")
                }


            } ?: run {
                Text(
                    "Loading item...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}