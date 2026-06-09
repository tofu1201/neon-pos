package com.yourcompany.pos

import android.app.Application
import com.yourcompany.pos.di.PosAppContainer

class PosApplication : Application() {
    lateinit var container: PosAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = PosAppContainer(this)
    }
}
