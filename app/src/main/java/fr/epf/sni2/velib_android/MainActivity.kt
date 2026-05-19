package fr.epf.sni2.velib_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.epf.sni2.velib_android.ui.navigation.Routes
import fr.epf.sni2.velib_android.ui.screens.FavoritesScreen
import fr.epf.sni2.velib_android.ui.screens.MapScreen
import fr.epf.sni2.velib_android.ui.screens.NearbyScreen
import fr.epf.sni2.velib_android.ui.theme.VelibTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VelibTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomNavItems = listOf(
                    Triple(Routes.MAP, "Carte", Icons.Default.Map),
                    Triple(Routes.FAVORITES, "Favoris", Icons.Default.Favorite),
                    Triple(Routes.NEARBY, "Proximité", Icons.Default.LocationOn),
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { (route, label, icon) ->
                                NavigationBarItem(
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.MAP,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.MAP) { MapScreen() }
                        composable(Routes.FAVORITES) { FavoritesScreen() }
                        composable(Routes.NEARBY) { NearbyScreen() }
                    }
                }
            }
        }
    }
}
