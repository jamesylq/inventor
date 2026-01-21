package com.example.inventor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class HomeCardItem(val title: String, val enabled: Boolean = true, val onClick: () -> Unit)

@Composable
fun HomeScreen(navController: NavController) {
    // Define 4 sample cards
    val cards = listOf(
        HomeCardItem("Search Item") {
            navController.navigate("itemsearcher")
        },
        HomeCardItem("Check In / Out") {
            navController.navigate("qrscanner")
        },
        HomeCardItem("New Item") {
            navController.navigate("generateqr")
        },
        HomeCardItem("Coming Soon", enabled = false) {
            /* handle click */
        }
    )

    Scaffold(
        topBar = {
            MainTopAppBar(
                navController = navController,
                rightButton = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Default.ExitToApp, contentDescription = "Log Out")
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cards) { card ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        onClick = card.onClick,
                        enabled = card.enabled,
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(card.title, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
