package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AdDetailScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PostAdScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BotswanaBlue
import com.example.ui.theme.BotswanaBlueDark
import com.example.ui.theme.GoldenAmber

@Composable
fun MaranathaApp(
    viewModel: MaranathaViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUserFlow.collectAsState()

    // Monitor current back stack entry to decide bottom bar item selection
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define bottom navigation tabs
    val tabs = listOf(
        TabNavigationItem("Browse", "home", Icons.Default.Explore),
        TabNavigationItem("Post Ad", "post", Icons.Default.AddCircle),
        TabNavigationItem("Profile", "profile", Icons.Default.AccountCircle),
        TabNavigationItem("Admin", "admin", Icons.Default.Security)
    )

    // Determine whether to show the Bottom Navigation Bar (hide on details and login screen)
    val shouldShowBottomBar = currentRoute in listOf("home", "post", "profile", "admin")

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("app_bottom_bar")
                ) {
                    tabs.forEach { tab ->
                        val isSelected = currentRoute == tab.route || 
                                (tab.route == "post" && currentRoute?.startsWith("edit") == true)

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (tab.route in listOf("post", "profile") && currentUser == null) {
                                    // Secure redirect: Send unauthenticated users to login with a back-target parameter
                                    navController.navigate("login/${tab.route}") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(tab.route) {
                                        // Pop up to the start destination of the graph to avoid building up a large stack
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { 
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    color = if (isSelected) BotswanaBlueDark else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BotswanaBlueDark,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = BotswanaBlue.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // HOMEPAGE
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onAdClick = { adId ->
                        navController.navigate("detail/$adId")
                    }
                )
            }

            // AD LISTING DETAIL VIEW
            composable(
                route = "detail/{adId}",
                arguments = listOf(navArgument("adId") { type = NavType.LongType })
            ) { backStackEntry ->
                val adId = backStackEntry.arguments?.getLong("adId") ?: 0L
                AdDetailScreen(
                    adId = adId,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // POST NEW AD FOR FREE
            composable("post") {
                // Double check authenticated session
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("login/post") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                } else {
                    PostAdScreen(
                        viewModel = viewModel,
                        adIdToEdit = null,
                        onSuccess = {
                            // Take back to home and refresh flow
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }

            // EDIT EXISTING AD
            composable(
                route = "edit/{adId}",
                arguments = listOf(navArgument("adId") { type = NavType.LongType })
            ) { backStackEntry ->
                val adId = backStackEntry.arguments?.getLong("adId") ?: 0L
                PostAdScreen(
                    viewModel = viewModel,
                    adIdToEdit = adId,
                    onSuccess = {
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                )
            }

            // USER PROFILE SCREEN
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onEditAd = { adId ->
                        navController.navigate("edit/$adId")
                    },
                    onAdDetail = { adId ->
                        navController.navigate("detail/$adId")
                    },
                    onLoggedOut = {
                        // Secure redirection on logout
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            // ADMIN DASHBOARD
            composable("admin") {
                AdminDashboardScreen(
                    viewModel = viewModel
                )
            }

            // SECURE REGISTER / LOGIN FORM
            composable(
                route = "login/{redirect}",
                arguments = listOf(navArgument("redirect") { type = NavType.StringType })
            ) { backStackEntry ->
                val redirectTarget = backStackEntry.arguments?.getString("redirect") ?: "home"
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(redirectTarget) {
                            popUpTo("login/{redirect}") { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    // Capture system back buttons for secondary screens to route back nicely
    if (currentRoute != "home" && currentRoute != null) {
        BackHandler {
            if (currentRoute.startsWith("detail") || currentRoute.startsWith("edit") || currentRoute.startsWith("login")) {
                navController.popBackStack()
            } else {
                navController.navigate("home") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}

private data class TabNavigationItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
