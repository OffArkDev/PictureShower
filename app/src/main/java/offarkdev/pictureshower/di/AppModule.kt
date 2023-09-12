package offarkdev.pictureshower.di

import offarkdev.pictureshower.InternetChecker
import offarkdev.pictureshower.analytics.AnalyticsManager
import offarkdev.pictureshower.analytics.AppsflyerAnalyticsSender
import offarkdev.pictureshower.analytics.FirebaseAnalyticsSender
import offarkdev.pictureshower.picture.GetDogPicture
import offarkdev.pictureshower.network.PictureApi
import offarkdev.pictureshower.picture.PictureViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { PictureViewModel(get(), get()) }
    factory { PictureApi() }
    factory { GetDogPicture(get(), get()) }
    factory { InternetChecker(androidApplication()) }
    factory { AnalyticsManager(listOf(get<FirebaseAnalyticsSender>(), get<AppsflyerAnalyticsSender>())) }
    factory { FirebaseAnalyticsSender(androidApplication()) }
    factory { AppsflyerAnalyticsSender() }
}
