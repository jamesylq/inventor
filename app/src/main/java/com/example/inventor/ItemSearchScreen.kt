package com.example.inventor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun ItemSearcherScreen(navController: NavController) {
    Scaffold (
        topBar = {
            MainTopAppBar(
                navController = navController,
                dest = "home"
            )
        }
    ) {
        Column (
            modifier = Modifier.padding(it)
        ) {
            ItemSearcher(navController = navController)
        }
    }
}