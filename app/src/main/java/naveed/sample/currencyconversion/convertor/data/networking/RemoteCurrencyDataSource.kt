package naveed.sample.currencyconversion.convertor.data.networking


import io.ktor.client.HttpClient
import io.ktor.client.request.get
import naveed.sample.currencyconversion.BuildConfig
import naveed.sample.currencyconversion.core.domain.util.Result
import naveed.sample.currencyconversion.convertor.domain.CurrencyDataSource
import naveed.sample.currencyconversion.convertor.data.networking.dto.CurrenciesRatesResponseDto
import naveed.sample.currencyconversion.core.domain.util.NetworkError
import naveed.sample.currencyconversion.core.domain.util.map
import naveed.sample.currencyconversion.core.data.networking.constructUrl
import naveed.sample.currencyconversion.core.data.networking.safeCall

class RemoteCurrencyDataSource(
    private val httpClient: HttpClient
): CurrencyDataSource {

    override suspend fun getCurrencies(): Result<Map<String,String>, NetworkError> {
        return safeCall<Map<String,String>> {
            httpClient.get(
                urlString = constructUrl("currencies.json?show_alternative=1")
            )
        }.map { response ->
            response
        }
    }


    override suspend fun getCurrenciesRates(): Result<Map<String,Double>, NetworkError> {
        return safeCall<CurrenciesRatesResponseDto> {
            httpClient.get(
                urlString = constructUrl("latest.json?app_id=${BuildConfig.API_KEY}")
            )
        }.map { response ->
            response.rates
        }
    }
}