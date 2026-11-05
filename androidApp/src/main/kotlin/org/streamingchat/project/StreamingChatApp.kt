package org.streamingchat.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.streamingchat.project.di.initKoin

class StreamingChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@StreamingChatApp)
        }
    }
}
