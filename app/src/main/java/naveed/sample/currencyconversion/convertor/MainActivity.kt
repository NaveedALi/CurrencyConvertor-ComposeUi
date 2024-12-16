package naveed.sample.currencyconversion.convertor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import naveed.sample.currencyconversion.convertor.navigation.NavigationGraph
import naveed.sample.currencyconversion.convertor.presentation.CurrencyConverterScreen
import naveed.sample.currencyconversion.convertor.theme.CurrencyConversionTheme
import naveed.sample.currencyconversion.convertor.presentation.CurrencyViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyConversionTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    PreviewCurrencyConverterScreen(modifier = Modifier.padding(innerPadding))
//                }
                Surface {
                    navHostController = rememberNavController()
                    NavigationGraph(navHostController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCurrencyConverterScreen(currencyViewModel : CurrencyViewModel = koinViewModel()) {
    CurrencyConverterScreen(viewModel = currencyViewModel)
}
