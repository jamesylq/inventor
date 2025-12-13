package com.example.inventor

import QRCodeData
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Composable
fun ItemSearcher(
    navController: NavController,
    onClick: (() -> Unit)? = null
) {
    var searchText by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<QRCodeData>()) }

    // Fetch items once and listen for updates
    LaunchedEffect(true) {
        database.reference.child("qrcodes").addValueEventListener(
            object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<QRCodeData>()
                for (child in snapshot.children) {
                    val item = child.getValue(QRCodeData::class.java)
                    if (item != null) list.add(item)
                }
                items = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InventorCore", "Failed to read items: ${error.message}")
            }
        })
    }

    // Filter by search
    val filteredItems = items.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 🔎 Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search items…") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 📋 Scrollable list of items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems.size) { index ->
                val item = filteredItems[index]

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    onClick = onClick?: {
                        val encodedName = Uri.encode(item.name)
                        navController.navigate("viewitem/$encodedName")
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        // Name
                        Text(
                            item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Status field
                        Row {
                            Text(
                                text = "Status: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = statusToText(item.status),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Location
                        Row {
                            Text(
                                text = "Location: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Update Time
                        Row (
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Last Updated: ${formatTime(item.updateTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}
