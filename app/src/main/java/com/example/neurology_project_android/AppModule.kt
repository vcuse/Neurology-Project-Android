package com.example.neurology_project_android

// In: app/src/main/java/com/example/neurology_project_android/di/AppModule.kt
// (It's good practice to put modules in a 'di' package)

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.neurology_project_android.SessionManager
import com.example.neurology_project_android.SignalingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // These dependencies will live as long as the app
object AppModule {

    @Provides
    @Singleton // Use @Singleton to ensure only one instance is ever created
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        // Hilt provides the application context for us
        return SessionManager(context).client
    }



    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Provides // <-- ADD THIS: Tells Hilt this function PROVIDES a dependency.
    @Singleton
    fun provideSignalingClient(@ApplicationContext context: Context): SignalingClient {
        return SignalingClient(context)
    }

}