package naveed.sample.currencyconversion.convertor.presentation

import naveed.sample.currencyconversion.core.domain.util.NetworkError


sealed interface CurrencyRateListEvent {
    data class Error(val error: NetworkError): CurrencyRateListEvent
}