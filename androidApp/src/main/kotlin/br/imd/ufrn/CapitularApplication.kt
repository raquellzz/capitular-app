package br.imd.ufrn

import android.app.Application
import br.imd.ufrn.di.modulosDoApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CapitularApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CapitularApplication)
            modules(modulosDoApp)
        }
    }
}
