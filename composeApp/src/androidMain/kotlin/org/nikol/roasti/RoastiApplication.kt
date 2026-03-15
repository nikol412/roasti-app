package org.nikol.roasti

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.nikol.roasti.di.sharedModule
import org.nikol.roasti.di.viewModelsModule

@OptIn(ExperimentalCoilApi::class)
class RoastiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoastiApplication)
            modules(sharedModule, viewModelsModule)
        }

        initCoilHttpClient()
    }

    private fun initCoilHttpClient() {
        val httpClient: HttpClient = get()
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this)
                .components {
                    add(KtorNetworkFetcherFactory(httpClient)) // твой HttpClient из shared
                }
                .build()
        }
    }
}
