package cl.duoc.agromatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import cl.duoc.agromatch.repository.AgroMatchContainer
import cl.duoc.agromatch.ui.AgroMatchApp
import cl.duoc.agromatch.ui.theme.AgroMatchTheme
import cl.duoc.agromatch.viewmodel.AgroMatchViewModelFactory
import cl.duoc.agromatch.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = AgroMatchContainer(applicationContext)
        val authViewModel = ViewModelProvider(
            this,
            AgroMatchViewModelFactory(container.authRepository)
        )[AuthViewModel::class.java]

        setContent {
            AgroMatchTheme {
                AgroMatchApp(authViewModel)
            }
        }
    }
}
