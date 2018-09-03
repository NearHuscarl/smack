package com.nearhuscarl.smack.Controllers

import android.app.Application
import com.nearhuscarl.smack.Utilities.SharedPrefs

class App : Application() {

    companion object {
        lateinit var sharedPrefs: SharedPrefs
    }

    override fun onCreate() {
        sharedPrefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}