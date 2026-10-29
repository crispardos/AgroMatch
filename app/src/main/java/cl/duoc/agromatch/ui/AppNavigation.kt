package cl.duoc.agromatch.ui

import android.R.attr.type
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.UserRole
import cl.duoc.agromatch.ui.auth.LoginScreen
import cl.duoc.agromatch.ui.auth.RegisterScreen
import cl.duoc.agromatch.viewmodel.AuthViewModel

@Composable
fun AgroMatchApp(authViewModel: AuthViewModel) {
    val state by authViewModel.state.collectAsStateWithLifecycle()
    if (state.isCheckingSession) {
        LoadingScreen()
        return
    }

    val navController = rememberNavController()
    val initialRoute = remember { state.user?.let(::homeRoute) ?: LOGIN_ROUTE }

    LaunchedEffect(state.user?.id) {
        val destination = state.user?.let(::homeRoute) ?: LOGIN_ROUTE
        val currentRoute = navController.currentDestination?.route ?: return@LaunchedEffect
        if (currentRoute != destination) {
            val routeToPop = if (state.user != null) {
                LOGIN_ROUTE
            } else {
                currentRoute
            }
            navController.navigate(destination) {
                routeToPop?.let { route ->
                    popUpTo(route) { inclusive = true }
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = initialRoute) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                isWorking = state.isWorking,
                error = state.error,
                onLogin = authViewModel::login,
                onRegisterClick = {
                    authViewModel.clearError()
                    navController.navigate(REGISTER_ROUTE)
                }
            )
        }
        composable(REGISTER_ROUTE) {
            RegisterScreen(
                isWorking = state.isWorking,
                error = state.error,
                onRegister = authViewModel::register,
                onLoginClick = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )
        }
        composable(FARMER_HOME_ROUTE) {
            state.user?.let { user -> val farmerViewModel: cl.duoc.agromatch.viewmodel.FarmerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = cl.duoc.agromatch.viewmodel.AgroMatchViewModelFactory(
                    authRepository = cl.duoc.agromatch.repository.AgroMatchContainer(androidx.compose.ui.platform.LocalContext.current).authRepository,
                    farmerRepository = cl.duoc.agromatch.repository.AgroMatchContainer(androidx.compose.ui.platform.LocalContext.current).farmerRepository
                )
            )

                cl.duoc.agromatch.ui.farmer.FarmerHomeScreen(
                    viewModel = farmerViewModel,
                    onNavigateToCreateNeed = { navController.navigate(NEED_FORM_ROUTE) },
                    onNavigateToMatching = { need ->
                        navController.navigate("$MATCHING_ROUTE/${need.id}")
                    },
                    onLogout = authViewModel::logout
                )
            }
        }
        composable(NEED_FORM_ROUTE) {
            state.user?.let { user ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val container = remember { cl.duoc.agromatch.repository.AgroMatchContainer(context) }
                val farmerViewModel: cl.duoc.agromatch.viewmodel.FarmerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = cl.duoc.agromatch.viewmodel.AgroMatchViewModelFactory(
                        authRepository = container.authRepository,
                        farmerRepository = container.farmerRepository
                    )
                )

                cl.duoc.agromatch.ui.farmer.NeedFormScreen(
                    farmerId = user.id,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = { need ->
                        farmerViewModel.createNeed(need)
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(
            route = "$MATCHING_ROUTE/{needId}",
            arguments = listOf(
                androidx.navigation.navArgument("needId") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val needId = backStackEntry.arguments?.getLong("needId") ?: 0L

            state.user?.let { user ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val container = remember { cl.duoc.agromatch.repository.AgroMatchContainer(context) }
                val farmerViewModel: cl.duoc.agromatch.viewmodel.FarmerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = cl.duoc.agromatch.viewmodel.AgroMatchViewModelFactory(
                        authRepository = container.authRepository,
                        farmerRepository = container.farmerRepository
                    )
                )

                val need = farmerViewModel.state.collectAsState().value.needs.find { it.id == needId }

                if (need != null) {
                    cl.duoc.agromatch.ui.farmer.MatchingResultsScreen(
                        viewModel = farmerViewModel,
                        need = need,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRating = { interestId, pilotAlias ->
                            navController.navigate("rating/$interestId/$pilotAlias")
                        }
                    )
                } else {
                    Text("Cargando necesidad...")
                }
            }
        }
        composable(
            route = RATING_ROUTE,
            arguments = listOf(
                androidx.navigation.navArgument("interestId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("pilotAlias") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val interestId = backStackEntry.arguments?.getLong("interestId") ?: 0L
            val pilotAlias = backStackEntry.arguments?.getString("pilotAlias") ?: "el piloto"

            state.user?.let { user ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val container = remember { cl.duoc.agromatch.repository.AgroMatchContainer(context) }
                val farmerViewModel: cl.duoc.agromatch.viewmodel.FarmerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = cl.duoc.agromatch.viewmodel.AgroMatchViewModelFactory(
                        authRepository = container.authRepository,
                        farmerRepository = container.farmerRepository
                    )
                )
                LaunchedEffect(interestId) {
                    farmerViewModel.setActiveInterestId(interestId)
                }

                cl.duoc.agromatch.ui.farmer.RatingScreen(
                    viewModel = farmerViewModel,
                    pilotAlias = pilotAlias,
                    onNavigateBack = { navController.popBackStack() },
                    onRatingSaved = {
                        navController.popBackStack(FARMER_HOME_ROUTE, inclusive = false)
                    }
                )
            }
        }
        composable(PILOT_HOME_ROUTE) {
            state.user?.let { user -> RoleHomeScreen(user, authViewModel::logout) }
        }
        composable(ADMIN_HOME_ROUTE) {
            state.user?.let { user -> RoleHomeScreen(user, authViewModel::logout) }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun RoleHomeScreen(user: User, onLogout: () -> Unit) {
    val roleTitle = when (UserRole.fromValue(user.role)) {
        UserRole.FARMER -> stringResource(R.string.role_farmer_home)
        UserRole.PILOT -> stringResource(R.string.role_pilot_home)
        UserRole.ADMIN -> stringResource(R.string.role_admin_home)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.welcome_alias, user.alias), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(roleTitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.home_placeholder))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onLogout) {
                Text(stringResource(R.string.logout_action))
            }
        }
    }
}

private fun homeRoute(user: User): String = when (UserRole.fromValue(user.role)) {
    UserRole.FARMER -> FARMER_HOME_ROUTE
    UserRole.PILOT -> PILOT_HOME_ROUTE
    UserRole.ADMIN -> ADMIN_HOME_ROUTE
}

private const val LOGIN_ROUTE = "login"
private const val REGISTER_ROUTE = "register"
private const val FARMER_HOME_ROUTE = "farmer-home"
private const val PILOT_HOME_ROUTE = "pilot-home"
private const val ADMIN_HOME_ROUTE = "admin-home"
private const val NEED_FORM_ROUTE = "need-form"
private const val MATCHING_ROUTE = "matching"
private const val RATING_ROUTE = "rating/{interestId}/{pilotAlias}"