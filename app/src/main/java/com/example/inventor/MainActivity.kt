package com.example.inventor

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventor.ui.theme.InventorTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

lateinit var auth: FirebaseAuth
lateinit var database: FirebaseDatabase

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            InventorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {_ ->
                    auth = FirebaseAuth.getInstance()
                    database = FirebaseDatabase.getInstance(
                        "https://inventor-36c3a-default-rtdb.asia-southeast1.firebasedatabase.app"
                    )
                    val currentUser = auth.currentUser

                    NavHost(
                        navController = navController,
                        startDestination = if (currentUser == null) "login" else "home"
                    ) {
                        // Authentication screens
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignUpScreen(navController) }

                        // Main app screens
                        composable("home") { HomeScreen(navController) }

                        // QR functionality
                        composable("generateqr") { GenerateQRScreen(navController) }
                        composable("qrscanner") { QRScannerScreen(navController) }

                        composable("modifyitem/{name}",
                            arguments = listOf(navArgument("name") {
                                type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            ModifyItemScreen(navController, Uri.decode(name))
                        }

                        composable("viewitem/{name}",
                            arguments = listOf(navArgument("name") {
                                type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            ViewItemScreen(navController, Uri.decode(name))
                        }

                        composable("itemsearcher") { ItemSearcherScreen(navController) }
                    }
                }
            }
        }
    }
}
