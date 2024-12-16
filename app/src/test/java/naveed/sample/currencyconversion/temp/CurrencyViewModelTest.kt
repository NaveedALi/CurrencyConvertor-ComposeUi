package naveed.sample.currencyconversion.temp

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import naveed.sample.currencyconversion.convertor.domain.CurrencyDataSource
import naveed.sample.currencyconversion.convertor.presentation.CurrencyViewModel
import naveed.sample.currencyconversion.core.domain.util.Result
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()//StandardTestDispatcher()
    private lateinit var viewModel: CurrencyViewModel
    private val mockCurrencyDataSource: CurrencyDataSource = mockk()
    private val mockSharedPreferences: SharedPreferences = mockk()
    private val mockEditor: SharedPreferences.Editor = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs

        viewModel = CurrencyViewModel(
            currencyDataSource = mockCurrencyDataSource,
            sharedPreferences = mockSharedPreferences
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCurrencyRates loads data when local data is empty`() = runTest {
        val mockCurrencies = mapOf("USD" to "United States Dollar", "EUR" to "Euro")
        val mockRates = mapOf("USD" to 1.0, "EUR" to 0.85)
        viewModel.updateAmount("1")
        every { mockSharedPreferences.getString("currencies", null) } returns null
        every { mockSharedPreferences.getString("Rates", null) } returns null
        coEvery { mockCurrencyDataSource.getCurrencies() } returns Result.Success(mockCurrencies)
        coEvery { mockCurrencyDataSource.getCurrenciesRates() } returns Result.Success(mockRates)


        // Launch a coroutine to collect the uiState
        val job = launch {
            viewModel.uiState.collect { state ->
                if (!state.isLoading) {
                    // Perform assertions once loading is complete
                    Assert.assertEquals(2, state.currencyRateUi.size)
                    Assert.assertEquals(1.0, state.currencyRateUi.find { it.currencyCode == "USD" }?.rateInUSD)
                    Assert.assertEquals(0.85, state.currencyRateUi.find { it.currencyCode == "EUR" }?.rateInUSD)
                }
            }
        }

        viewModel.fetchCurrencyRates()
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()
    }

    @Test
    fun `saveCurrenciesToLocal saves currencies to SharedPreferences`() {
        val currencies = mapOf("USD" to "United States Dollar", "EUR" to "Euro")
        val jsonString = Json.encodeToString(currencies)

        viewModel.saveCurrenciesToLocal(currencies)

        verify { mockEditor.putString("currencies", jsonString) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getCurrenciesFromLocal returns saved currencies`() {
        val currencies = mapOf("USD" to "United States Dollar", "EUR" to "Euro")
        val jsonString = Json.encodeToString(currencies)

        every { mockSharedPreferences.getString("currencies", null) } returns jsonString

        val result = viewModel.getCurrenciesFromLocal()

        Assert.assertEquals(currencies, result)
    }

    @Test
    fun `saveCurrencyRatesToLocal saves rates to SharedPreferences`() {
        val rates = mapOf("USD" to 1.0, "EUR" to 0.85)
        val jsonString = Json.encodeToString(rates)

        viewModel.saveCurrencyRatesToLocal(rates)

        verify { mockEditor.putString("Rates", jsonString) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getCurrencyRatesFromLocal returns saved rates`() {
        val rates = mapOf("USD" to 1.0, "EUR" to 0.85)
        val jsonString = Json.encodeToString(rates)

        every { mockSharedPreferences.getString("Rates", null) } returns jsonString

        val result = viewModel.getCurrencyRatesFromLocal()

        Assert.assertEquals(rates, result)
    }

    @Test
    fun `isDifferenceMoreThan30Minutes true check`() {
        val oldDate = Date(System.currentTimeMillis() - 31 * 60 * 1000) // 31 minutes ago
        val newDate = Date()

        val result = viewModel.isDifferenceMoreThan30Minutes(oldDate, newDate)

        Assert.assertTrue(result)
    }

    @Test
    fun `isDifferenceMoreThan30Minutes false check`() {
        val oldDate = Date(System.currentTimeMillis() - 29 * 60 * 1000) // 29 minutes ago
        val newDate = Date()

        val result = viewModel.isDifferenceMoreThan30Minutes(oldDate, newDate)

        Assert.assertFalse(result)
    }

    @Test
    fun `updateAmount and trigger conversion`() = runTest {
        val mockRates = mapOf("USD" to 1.0, "EUR" to 0.85)
        viewModel.saveCurrencyRatesToLocal(mockRates)

        viewModel.updateAmount("100.0")

        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        Assert.assertFalse(uiState.isLoading)
    }
}
