package org.nikol.roasti

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.nikol.roasti.di.sharedModule
import org.nikol.roasti.di.viewModelsModule

class RoastiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoastiApplication)
            modules(sharedModule, viewModelsModule)
        }
    }
}
