package cl.duoc.agromatch.ui

import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import cl.duoc.agromatch.ui.pilot.PilotHomeScreen
import cl.duoc.agromatch.ui.pilot.PilotProfileScreen
import cl.duoc.agromatch.viewmodel.AgroMatchViewModelFactory
import cl.duoc.agromatch.viewmodel.AuthViewModel
import cl.duoc.agromatch.viewmodel.FarmerViewModel
import cl.duoc.agromatch.viewmodel.PilotViewModel

@Composable
fun AgroMatchApp(authViewModel: AuthViewModel, factory: AgroMatchViewModelFactory) {
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
            navController.navigate(destination) {
                // No conservar pantallas ni ViewModels de la sesión anterior.
                popUpTo(navController.graph.id) { inclusive = true }
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
            state.user?.let { user ->
                val farmerFactory = remember(user.id) { factory.forFarmer(user.id) }
                val farmerViewModel: FarmerViewModel = viewModel(factory = farmerFactory)

                cl.duoc.agromatch.ui.farmer.FarmerHomeScreen(
                    viewModel = farmerViewModel,
                    onNavigateToCreateNeed = { navController.navigate(NEED_FORM_ROUTE) },
                    onNavigateToMatching = { need ->
                        navController.navigate("$MATCHING_ROUTE/${need.id}")
                    },
                    onNavigateToNeedDetails = { needId ->
                        navController.navigate("$NEED_DETAILS_ROUTE/$needId")
                    },
                    onLogout = authViewModel::logout
                )
            }
        }
        composable(NEED_FORM_ROUTE) {
            state.user?.let { user ->
                val farmerFactory = remember(user.id) { factory.forFarmer(user.id) }
                val farmerViewModel: FarmerViewModel = viewModel(factory = farmerFactory)
                val farmerState by farmerViewModel.state.collectAsStateWithLifecycle()

                cl.duoc.agromatch.ui.farmer.NeedFormScreen(
                    farmerId = user.id,
                    isSaving = farmerState.isLoading,
                    error = farmerState.error,
                    onClearError = farmerViewModel::clearError,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = { need ->
                        farmerViewModel.createNeed(need) { navController.popBackStack() }
                    }
                )
            }
        }
        composable(
            route = "$NEED_DETAILS_ROUTE/{needId}",
            arguments = listOf(
                androidx.navigation.navArgument("needId") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val needId = backStackEntry.arguments?.getLong("needId") ?: 0L
            state.user?.let { user ->
                val farmerFactory = remember(user.id) { factory.forFarmer(user.id) }
                val farmerViewModel: FarmerViewModel = viewModel(factory = farmerFactory)
                val farmerState by farmerViewModel.state.collectAsStateWithLifecycle()
                val overview = farmerState.needOverviews.firstOrNull { it.need.id == needId }

                when {
                    overview != null -> cl.duoc.agromatch.ui.farmer.NeedDetailsScreen(
                        overview = overview,
                        onNavigateBack = { navController.popBackStack() },
                        onFindAnotherPilot = { navController.navigate("$MATCHING_ROUTE/$needId") }
                    )
                    farmerState.isLoading -> LoadingScreen()
                    else -> Text("No se encontró el terreno.")
                }
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
                val farmerFactory = remember(user.id) { factory.forFarmer(user.id) }
                val farmerViewModel: FarmerViewModel = viewModel(factory = farmerFactory)

                val need = farmerViewModel.state.collectAsState().value.needs.find { it.id == needId }

                if (need != null) {
                    cl.duoc.agromatch.ui.farmer.MatchingResultsScreen(
                        viewModel = farmerViewModel,
                        need = need,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRating = { interestId, pilotAlias ->
                            navController.navigate("rating/$interestId/${Uri.encode(pilotAlias)}")
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
                val farmerFactory = remember(user.id) { factory.forFarmer(user.id) }
                val farmerViewModel: FarmerViewModel = viewModel(factory = farmerFactory)
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
            state.user?.takeIf { it.role == UserRole.PILOT.value }?.let { user ->
                val pilotViewModel: PilotViewModel = viewModel(factory = factory)
                val pilotState by pilotViewModel.state.collectAsStateWithLifecycle()
                PilotHomeScreen(
                    alias = user.alias,
                    state = pilotState,
                    onEditProfile = {
                        pilotViewModel.clearFeedback()
                        authViewModel.clearError()
                        navController.navigate(PILOT_PROFILE_ROUTE)
                    },
                    onRetry = pilotViewModel::loadProfile,
                    onLogout = authViewModel::logout
                )
            }
        }
        composable(PILOT_PROFILE_ROUTE) { entry ->
            if (state.user?.role == UserRole.PILOT.value) {
                // Compartir el ViewModel del inicio conserva el borrador al volver.
                val homeEntry = remember(entry) { navController.getBackStackEntry(PILOT_HOME_ROUTE) }
                val pilotViewModel: PilotViewModel = viewModel(homeEntry, factory = factory)
                val pilotState by pilotViewModel.state.collectAsStateWithLifecycle()
                PilotProfileScreen(
                    state = pilotState,
                    authState = state,
                    onUpdateAlias = authViewModel::updateAlias,
                    onClearAliasFeedback = authViewModel::clearError,
                    onExperienceChange = pilotViewModel::updateExperience,
                    onHectaresChange = pilotViewModel::updateHectares,
                    onWorkingHoursChange = pilotViewModel::updateWorkingHours,
                    onAvailabilityFromChange = pilotViewModel::updateAvailableFrom,
                    onAvailabilityToChange = pilotViewModel::updateAvailableTo,
                    onClearAvailability = pilotViewModel::clearAvailability,
                    onToggleZone = pilotViewModel::toggleZone,
                    onToggleSpecialty = pilotViewModel::toggleSpecialty,
                    onSave = pilotViewModel::saveProfile,
                    onBack = { navController.popBackStack() }
                )
            }
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
private const val PILOT_PROFILE_ROUTE = "pilot-profile"
private const val ADMIN_HOME_ROUTE = "admin-home"
private const val NEED_FORM_ROUTE = "need-form"
private const val NEED_DETAILS_ROUTE = "need-details"
private const val MATCHING_ROUTE = "matching"
private const val RATING_ROUTE = "rating/{interestId}/{pilotAlias}"
