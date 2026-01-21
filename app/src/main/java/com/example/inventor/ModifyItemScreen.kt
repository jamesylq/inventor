package com.example.inventor

import QRCodeData
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ModifyItemScreen(
    navController: NavController,
    itemName: String,
    prevDestination: String = "itemsearcher"
) {
    var location by remember { mutableStateOf("") }
    var homeLocation by remember { mutableStateOf("") }

    var currentWith by remember { mutableStateOf("") }
    var currentUser by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var status by remember { mutableIntStateOf(0) }
    var recompose by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showCloseDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentWithConfirmation by remember { mutableStateOf(false) }

    var saveLocation by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var otherText by remember { mutableStateOf("") }

    var saveHomeLocation by remember { mutableStateOf(false) }
    var selectedHomeLocation by remember { mutableStateOf<String?>(null) }
    var otherHomeText by remember { mutableStateOf("") }


    BackHandler {
        navController.navigate(prevDestination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Fetch item data from database
    LaunchedEffect(itemName) {
        val itemRef = database.reference.child("qrcodes").child(itemName)
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(QRCodeData::class.java)?.let { data ->
                    location = data.location
                    homeLocation = data.homeLocation
                    currentWith = auth.currentUser!!.displayName!!
                    remarks = data.remarks
                    status = data.status
                }
                loading = false
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to fetch item: ${error.message}"
                loading = false
            }
        })
    }

    val registeredUsers = remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        currentUser = FirebaseAuth.getInstance().currentUser?.displayName!!
        database.reference.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                registeredUsers.value = snapshot.children.mapNotNull { it.key }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch users: ${error.message}")
            }
        })
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("Are you sure you want to leave this page? Unsaved changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseDialog = false
                        navController.navigate(prevDestination) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(
                        text = "Leave",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text(
                        text = "Stay",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item?") },
            text = {
                Text(
                    "This action cannot be undone.\n\n" +
                            "Are you sure you want to permanently delete \"$itemName\"?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        database.reference.child("qrcodes").child(itemName)
                            .removeValue()
                            .addOnSuccessListener {
                                logItemUpdate(
                                    itemName = itemName,
                                    action = Constants.ITEM_DELETION,
                                    details = "Deleted by $currentUser."
                                )

                                navController.navigate(prevDestination) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                            .addOnFailureListener {
                                errorMessage = "Failed to delete item: ${it.localizedMessage}"
                            }
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }


    Scaffold (
        topBar = {
            MainTopAppBar(
                navController = navController
            ) {
                IconButton(onClick = { showCloseDialog = true }) {
                    Icon(Icons.Default.Close, contentDescription = "Log Out")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Modify Item: $itemName", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator()
            } else {
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                EditableTextBox(
                    label = "Location",
                    textValue = location,
                    onValueChange = { location = it },
                    editingComponents = { setValue, onExit ->
                        if (saveLocation) {
                            val sel = otherText.ifBlank { selectedLocation }
                            if (sel != null) {
                                location = sel
                                setValue(sel)
                            }
                            onExit()
                            saveLocation = false
                        }

                        LocationPickerEditor(
                            itemLocations = itemLocations,
                            selectedLocation = selectedLocation,
                            onSelectedLocationChange = { selectedLocation = it },
                            otherText = otherText,
                            onOtherTextChange = { otherText = it },
                            onSaveRequested = { sel ->
                                if (sel != null) {
                                    location = sel
                                    setValue(sel)
                                }
                                onExit()
                            }
                        )
                    },
                    onSaveClicked = { saveLocation = true }
                )

                EditableTextBox(
                    label = "Home Location",
                    textValue = homeLocation,
                    onValueChange = { homeLocation = it },
                    editingComponents = { setValue, onExit ->
                        if (saveHomeLocation) {
                            val sel = otherHomeText.ifBlank { selectedHomeLocation }
                            if (sel != null) {
                                homeLocation = sel
                                setValue(sel)
                            }
                            onExit()
                            saveHomeLocation = false
                        }

                        LocationPickerEditor(
                            itemLocations = itemLocations,
                            selectedLocation = selectedHomeLocation,
                            onSelectedLocationChange = {
                                selectedHomeLocation = it
                                if (it != null) otherHomeText = ""
                            },
                            otherText = otherHomeText,
                            onOtherTextChange = {
                                otherHomeText = it
                                if (it.isNotBlank()) selectedHomeLocation = null
                            },
                            onSaveRequested = { sel ->
                                if (sel != null) {
                                    homeLocation = sel
                                    setValue(sel)
                                }
                                onExit()
                            }
                        )
                    },
                    onSaveClicked = { saveHomeLocation = true }
                )

                EditableTextBox(
                    label = "Last Used By",
                    textValue = currentWith,
                    onValueChange = { currentWith = it },
                    onSaveClicked = { currentWithConfirmation = true },
                    editingComponents = {setValue, onExit ->
                        var searchQuery by remember { mutableStateOf("") }
                        val filteredUsers = remember { mutableStateListOf<String>() }

                        if (currentWithConfirmation) {
                            AlertDialog(
                                onDismissRequest = { currentWithConfirmation = false },
                                title = { Text("Are you sure?") },
                                text = { Text("\"$searchQuery\" is not a recognised member!") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            currentWith = searchQuery
                                            currentWithConfirmation = false
                                            setValue(searchQuery)
                                            onExit()
                                        }
                                    ) {
                                        Text(
                                            text = "Proceed",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { currentWithConfirmation = false }) {
                                        Text(
                                            text = "Cancel",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            )
                        }

                        LaunchedEffect (Unit) {
                            for (user in registeredUsers.value) {
                                filteredUsers.add(user)
                            }
                        }

                        Column (
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                        ) {
                            Row (
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Last Used By",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = onExit,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Close")
                                }
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    filteredUsers.clear()

                                    var containsCurrent = false
                                    for (user in registeredUsers.value) {
                                        if (searchQuery == "" ||
                                            user.lowercase().contains(searchQuery.lowercase())) {
                                            filteredUsers.add(user)
                                            if (user == currentUser) containsCurrent = true
                                        }
                                    }

                                    if (containsCurrent) {
                                        filteredUsers.remove(currentUser)
                                        filteredUsers.add(0, currentUser)
                                    }

                                    recompose = (++recompose) % 10
                                },
                                label = { Text("Search") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(25.dp))
//                            Button(
//                                modifier = Modifier.fillMaxWidth(),
//                                onClick = {
//
//                                },
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = MaterialTheme.colorScheme.surface,
//                                    contentColor = MaterialTheme.colorScheme.onSurface
//                                )
//                            ) {
//                                Text("This Item is not with anyone.")
//                            }
//                            Spacer(modifier = Modifier.height(25.dp))
                            Text(
                                text = "Select Person Below:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                            if (recompose >= 0) {
                                LazyColumn {
                                    item {
                                        for (user in filteredUsers) {
                                            ElevatedCard (
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .clickable {
                                                        currentWith = user
                                                        setValue(user)
                                                        onExit()
                                                    }
                                            ) {
                                                Row (
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = user,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }

                                        if (filteredUsers.isEmpty()) {
                                            Spacer(Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "No Matches Found! To Save Anyway, Click \"Save\".",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Spacer(Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                EditableTextBox(
                    label = "Remarks",
                    textValue = remarks,
                    onValueChange = { remarks = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val updates = mapOf(
                            "location" to location,
                            "homeLocation" to homeLocation,
                            "currentWith" to currentWith,
                            "remarks" to remarks,
                            "updateTime" to System.currentTimeMillis(),
                            "status" to if (location == homeLocation) 1 else 0
                        )

                        database.reference.child("qrcodes").child(itemName)
                            .updateChildren(updates)
                            .addOnSuccessListener {
                                logItemUpdate(
                                    itemName = itemName,
                                    action = Constants.ITEM_EDIT,
                                    details = "Edited by $currentUser. Location: $location,  Home Location: $homeLocation, Last Used By: $currentWith, Remarks: $remarks"
                                )

                                navController.navigate("home")
                            }
                            .addOnFailureListener {
                                errorMessage = "Failed to update item: ${it.localizedMessage}"
                            }

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Item")
                }

            }
        }
    }
}
