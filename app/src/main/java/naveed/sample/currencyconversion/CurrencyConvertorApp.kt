package naveed.sample.currencyconversion

import android.app.Application
import naveed.sample.currencyconversion.convertor.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CurrencyConvertorApp: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CurrencyConvertorApp)
            androidLogger()

            modules(appModule)
        }
    }
}