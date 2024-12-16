package naveed.sample.currencyconversion.convertor.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import naveed.sample.currencyconversion.R
import naveed.sample.currencyconversion.core.presentation.utils.ObserveAsEvents
import naveed.sample.currencyconversion.core.presentation.utils.toString
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    viewModel: CurrencyViewModel = koinViewModel()
) {
    var amount = viewModel.amount.collectAsState()
    val context = LocalContext.current
    val currencies = viewModel.currencies.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()


    ObserveAsEvents(events = viewModel.currencyRateEvents) { event ->
        when (event) {
            is CurrencyRateListEvent.Error -> {
                Toast.makeText(
                    context,
                    event.error.toString(context),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isDropdownExpanded by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.currency_conversion)) },
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Row containing the Text Input and Dropdown
            Column (
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    value = amount.value,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                            viewModel.updateAmount(it)
                        }
                    },
                    label = { Text("") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(56.dp)
                            .padding(8.dp)
                    ) {
                        Text(selectedCurrency)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.select_currency)
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        currencies.value.forEach { currency ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.updateSelectedCurrency(currency)
                                    isDropdownExpanded = false
                                },
                                text = { Text(text = currency) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.currencyRateUi.isNotEmpty()) {
                // Converted currencies Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    state.currencyRateUi.forEach { (currencyCode, _, amount) ->
                        item {
                            CurrencyConversionCard(
                                currency = currencyCode,
                                amount = amount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyConversionCard(currency: String, amount: Double) {

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = currency, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f", amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
