package com.example.inventor

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.userProfileChangeRequest

@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                errorMessage = null

                if (username.isBlank()) {
                    errorMessage = "Username cannot be empty"
                    return@Button
                }

                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task2 ->
                                    if (task2.isSuccessful) {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = task.exception?.localizedMessage
                                    }
                                }

                            val user = auth.currentUser!!
                            val uid = user.uid

                            // Update FirebaseAuth displayName
                            val profileUpdate = userProfileChangeRequest {
                                displayName = username.trim()
                            }

                            user.updateProfile(profileUpdate).addOnCompleteListener {
                                // Save to Realtime Database
                                val userRecord = mapOf(
                                    "uid" to uid,
                                    "email" to email.trim(),
                                    "username" to username.trim(),
                                    "createdTime" to System.currentTimeMillis()
                                )

                                database.reference.child("users")
                                    .child(username.trim())
                                    .setValue(userRecord)
                                    .addOnCompleteListener {
                                        Log.w("InventorCore", "Signed Up $username")

                                        // Navigate after saving
                                        navController.navigate("home") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                            }

                        } else {
                            errorMessage = task.exception?.localizedMessage
                        }
                    }
            }
        ) {
            Text("Sign Up")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Log in")
        }

        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
