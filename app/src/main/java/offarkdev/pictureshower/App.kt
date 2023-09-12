package offarkdev.pictureshower

import android.app.Application
import offarkdev.pictureshower.analytics.AnalyticsManager
import offarkdev.pictureshower.analytics.AppOpenedEvent
import offarkdev.pictureshower.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import timber.log.Timber

class App : Application() {

    private val analyticsManager: AnalyticsManager by inject()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
        analyticsManager.sendEvent(AppOpenedEvent())
    }
}