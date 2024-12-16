package naveed.sample.currencyconversion.convertor.presentation

import androidx.compose.runtime.Immutable
import naveed.sample.currencyconversion.convertor.presentation.models.CurrencyRateUi

@Immutable
data class CurrencyRatesState(
    val isLoading: Boolean = false,
    val currencyRateUi: List<CurrencyRateUi> = emptyList(),
)
