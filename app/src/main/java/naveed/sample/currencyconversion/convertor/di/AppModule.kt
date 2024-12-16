package naveed.sample.currencyconversion.convertor.di

import android.content.Context
import android.content.SharedPreferences
import naveed.sample.currencyconversion.convertor.domain.CurrencyDataSource
import naveed.sample.currencyconversion.core.data.networking.HttpClientFactory
import naveed.sample.currencyconversion.convertor.presentation.CurrencyViewModel
import io.ktor.client.engine.cio.CIO
import naveed.sample.currencyconversion.convertor.data.networking.RemoteCurrencyDataSource
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { provideSharedPreferences(get()) }
    single { HttpClientFactory.create(CIO.create()) }
    singleOf(::RemoteCurrencyDataSource).bind<CurrencyDataSource>()
    viewModelOf(::CurrencyViewModel)
}
fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE)
}
