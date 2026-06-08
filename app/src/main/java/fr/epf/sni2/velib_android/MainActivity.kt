package fr.epf.sni2.velib_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import fr.epf.sni2.velib_android.ui.navigation.Routes
import fr.epf.sni2.velib_android.ui.screens.detail.DetailScreen
import fr.epf.sni2.velib_android.ui.screens.favorites.FavoritesScreen
import fr.epf.sni2.velib_android.ui.screens.history.HistoryScreen
import fr.epf.sni2.velib_android.ui.screens.map.MapScreen
import fr.epf.sni2.velib_android.ui.screens.nearby.NearbyScreen
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
                    Triple(Routes.HISTORY, "Trajets", Icons.Default.History),
                )

                // La barre du bas n'apparaît que sur les écrans principaux, pas sur le détail
                val showBottomBar = currentDestination?.route in bottomNavItems.map { it.first }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
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
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.MAP,
                        modifier = Modifier.padding(innerPadding),
                        // Fondu par défaut entre les onglets principaux
                        enterTransition = { fadeIn(tween(250)) },
                        exitTransition = { fadeOut(tween(250)) },
                    ) {
                        composable(Routes.MAP) {
                            MapScreen(
                                onStationClick = { stationId ->
                                    navController.navigate(Routes.detail(stationId))
                                }
                            )
                        }
                        composable(Routes.FAVORITES) {
                            FavoritesScreen(
                                onStationClick = { stationId ->
                                    navController.navigate(Routes.detail(stationId))
                                }
                            )
                        }
                        composable(Routes.NEARBY) {
                            NearbyScreen(
                                onStationClick = { stationId ->
                                    navController.navigate(Routes.detail(stationId))
                                }
                            )
                        }
                        composable(Routes.HISTORY) {
                            HistoryScreen()
                        }
                        composable(
                            route = Routes.DETAIL,
                            arguments = listOf(
                                navArgument(Routes.ARG_STATION_ID) { type = NavType.StringType }
                            ),
                            // Le détail glisse depuis la droite, et ressort vers la droite au retour
                            enterTransition = { slideIntoContainer(SlideDirection.Start, tween(300)) },
                            popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(300)) },
                        ) {
                            DetailScreen(onBack = { navController.navigateUp() })
                        }
                    }
                }
            }
        }
    }
}
