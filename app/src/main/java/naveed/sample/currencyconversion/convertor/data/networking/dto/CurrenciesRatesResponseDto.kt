package naveed.sample.currencyconversion.convertor.data.networking.dto

import kotlinx.serialization.Serializable


@Serializable
data class CurrenciesRatesResponseDto(
    var disclaimer: String? = null,
    var license: String? = null,
    var timestamp: Int? = null,
    var base: String? = null,
    var rates: Map<String, Double>

)