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
import org.nikol.roasti.core.di.coreDatabaseModule
import org.nikol.roasti.core.di.coreNetworkModule
import org.nikol.roasti.core.di.postPagingModule
import org.nikol.roasti.core.di.recipePagingModule
import org.nikol.roasti.di.platformModule
import org.nikol.roasti.di.viewModelsModule
import org.nikol.roasti.feature.auth.di.authModule
import org.nikol.roasti.feature.likes.di.likesModule
import org.nikol.roasti.feature.post.di.postModule
import org.nikol.roasti.feature.recipe.di.recipeModule
import org.nikol.roasti.feature.upload.di.uploadModule

@OptIn(ExperimentalCoilApi::class)
class RoastiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoastiApplication)
            modules(
                platformModule,
                coreDatabaseModule,
                coreNetworkModule,
                recipePagingModule,
                postPagingModule,
                authModule,
                uploadModule,
                likesModule,
                recipeModule,
                postModule,
                viewModelsModule
            )
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
