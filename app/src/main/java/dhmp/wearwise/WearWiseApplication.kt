package dhmp.wearwise

import android.app.Application
import dhmp.wearwise.data.AppContainer
import dhmp.wearwise.data.DefaultAppContainer

class WearWiseApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}