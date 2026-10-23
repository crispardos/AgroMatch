package cl.duoc.agromatch.ui

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
            state.user?.let { user -> RoleHomeScreen(user, authViewModel::logout) }
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
