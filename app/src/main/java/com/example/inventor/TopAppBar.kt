package com.example.inventor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    navController: NavController,
    dest: String? = null,
    rightButton: (@Composable RowScope.() -> Unit)? = null
) {
    val currentUser = auth.currentUser
    val username = currentUser?.displayName ?: currentUser?.email ?: "User"

    TopAppBar(
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.statusBarsPadding().fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(username)
            }
        },
        actions = rightButton?: {
            IconButton(onClick = {
                if (dest != null) {
                    navController.navigate(dest) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigateUp()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Back")
            }
        }
    )
}