package naveed.sample.currencyconversion.convertor.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import naveed.sample.currencyconversion.convertor.domain.CurrencyDataSource
import naveed.sample.currencyconversion.convertor.presentation.models.CurrencyRateUi
import naveed.sample.currencyconversion.core.domain.util.NetworkError
import naveed.sample.currencyconversion.core.domain.util.Result
import naveed.sample.currencyconversion.core.domain.util.onError
import naveed.sample.currencyconversion.core.domain.util.onSuccess
import java.util.Date
import java.util.concurrent.TimeUnit

class CurrencyViewModel(
    private val currencyDataSource: CurrencyDataSource,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _currencyRateEvents = Channel<CurrencyRateListEvent>()
    val currencyRateEvents = _currencyRateEvents.receiveAsFlow()

    private var lastRequestDateTime: Date? = null

    private val _uiState = MutableStateFlow(CurrencyRatesState())
    private var currenciesMap = emptyMap<String, String>()

    private var currencyRates = emptyMap<String, Double>()


    private val _currencies = MutableStateFlow(emptyList<String>())
    val currencies: StateFlow<List<String>> = _currencies

    private val _amount = MutableStateFlow("0")
    val amount: StateFlow<String> = _amount

    private val _selectedCurrency = MutableStateFlow("USD")
    val selectedCurrency: StateFlow<String> = _selectedCurrency


    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
        loadConvertedCurrencyRates(selectedCurrency.value, newAmount.toDoubleOrNull() ?: 0.0)
    }

    fun updateSelectedCurrency(newCurrency: String) {
        _selectedCurrency.value = newCurrency
        loadConvertedCurrencyRates(newCurrency, amount.value.toDoubleOrNull() ?: 0.0)
    }

    val uiState = _uiState
        .onStart { fetchCurrencyRates() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            CurrencyRatesState()
        )

    suspend fun fetchCurrencies(): Result<Map<String, String>, NetworkError> {
        return currencyDataSource.getCurrencies()
    }

    fun fetchCurrencyRates() {
        currenciesMap = getCurrenciesFromLocal()
        _currencies.value = currenciesMap.keys.toList()
        currencyRates = getCurrencyRatesFromLocal()
        if ((currenciesMap.isEmpty() || currencyRates.isEmpty()) || isDifferenceMoreThan30Minutes(
                lastRequestDateTime,
                Date()
            )
        ) {
            lastRequestDateTime = Date()
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val resultCurrencies = if (currenciesMap.isEmpty()) viewModelScope.async {
                    fetchCurrencies()
                }.await() else null

                val resultRates = viewModelScope.async {
                    currencyDataSource
                        .getCurrenciesRates()
                }.await()

                resultCurrencies?.onSuccess { currencyMap ->
                    saveCurrenciesToLocal(currencyMap)
                }?.onError { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _currencyRateEvents.send(CurrencyRateListEvent.Error(error))
                }

                resultRates.onSuccess { currencyMap ->
                    _uiState.update {
                        saveCurrencyRatesToLocal(currencyMap)
                        currencyRates = currencyMap
                        val ratesUi = convertBasedOnSelectedRate(
                            getRate(
                                selectedCurrency = selectedCurrency.value,
                                amount.value.toDoubleOrNull() ?: 0.0
                            )
                        )
                        it.copy(
                            isLoading = false,
                            currencyRateUi = ratesUi.toList()
                        )
                    }
                }
                    .onError { error ->
                        _uiState.update { it.copy(isLoading = false) }
                        _currencyRateEvents.send(CurrencyRateListEvent.Error(error))
                    }

            }
        } else {
            val ratesUi =
                convertBasedOnSelectedRate(getRate(selectedCurrency.value, amount.value.toDoubleOrNull() ?: 0.0))
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currencyRateUi = ratesUi.toList()
                )
            }

        }
    }

    fun saveCurrenciesToLocal(currencies: Map<String, String>) {
        this.currenciesMap = currencies
        this._currencies.value = currencies.keys.toList()
        val editor = sharedPreferences.edit()
        val jsonString = Json.encodeToString(currencies)
        editor.putString("currencies", jsonString)
        editor.apply()
    }

    fun getCurrenciesFromLocal(): Map<String, String> {
        val jsonString = sharedPreferences.getString("currencies", null) ?: return emptyMap()
        return Json.decodeFromString(jsonString)
    }


    fun saveCurrencyRatesToLocal(currencies: Map<String, Double>) {
        val editor = sharedPreferences.edit()
        val jsonString = Json.encodeToString(currencies)
        editor.putString("Rates", jsonString)
        editor.apply()
    }

    fun getCurrencyRatesFromLocal(): Map<String, Double> {
        val jsonString = sharedPreferences.getString("Rates", null) ?: return emptyMap()
        return Json.decodeFromString(jsonString)
    }

    fun loadConvertedCurrencyRates(selectedCurrency: String, amount: Double) {
        _uiState.update {
            it.copy(
                isLoading = true
            )
            val ratesUi = convertBasedOnSelectedRate(getRate(selectedCurrency, amount))
            it.copy(
                isLoading = false,
                currencyRateUi = ratesUi.toList()
            )
        }
    }

    fun convertBasedOnSelectedRate(
        rate: Double
    ): MutableList<CurrencyRateUi> {
        //Log.d("currency", "selectedCurrency=$selectedCurrency amount=$amount rateInUSD=$rate")
        val ratesUi = mutableListOf<CurrencyRateUi>()
        currencyRates.forEach { (key, value) ->
            ratesUi.add(CurrencyRateUi(key, currenciesMap[key] ?: "", value * rate))
        }
        return ratesUi
    }

    fun getRate(selectedCurrency: String, amount: Double): Double {
        val oneUsdValue = currencyRates.get(selectedCurrency) ?: 1.0
        return amount / (oneUsdValue)
    }

    fun isDifferenceMoreThan30Minutes(oldDate: Date?, newDate: Date): Boolean {
        if (oldDate == null) return true
        val differenceInMillis = kotlin.math.abs(newDate.time - oldDate.time)
        val differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis)
        return differenceInMinutes > 30 // return true if difference is greater than 30 minutes
    }
}
