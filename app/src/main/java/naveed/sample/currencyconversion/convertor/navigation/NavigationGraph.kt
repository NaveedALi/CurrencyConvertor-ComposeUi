package naveed.sample.currencyconversion.convertor.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import naveed.sample.currencyconversion.convertor.presentation.CurrencyConverterScreen

@Composable
fun NavigationGraph(navController:NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HomeScreen) {
        composable(Routes.HomeScreen) {
            CurrencyConverterScreen()
        }
    }
}