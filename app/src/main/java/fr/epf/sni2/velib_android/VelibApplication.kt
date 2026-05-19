package fr.epf.sni2.velib_android

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class VelibApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            userAgentValue = packageName
            load(this@VelibApplication, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        }
    }
}
