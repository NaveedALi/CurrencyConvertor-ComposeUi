package naveed.sample.currencyconversion.convertor.domain


import naveed.sample.currencyconversion.core.domain.util.NetworkError
import naveed.sample.currencyconversion.core.domain.util.Result

interface CurrencyDataSource {
    suspend fun getCurrencies(): Result<Map<String,String>, NetworkError>
    suspend fun getCurrenciesRates(): Result<Map<String,Double>, NetworkError>
}