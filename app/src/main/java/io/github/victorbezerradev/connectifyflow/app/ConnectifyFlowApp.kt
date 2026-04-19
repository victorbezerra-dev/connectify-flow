package io.github.victorbezerradev.connectifyflow.app
import android.app.Application
import app.rive.runtime.kotlin.core.Rive
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ConnectifyFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Rive.init(this)
    }
}
