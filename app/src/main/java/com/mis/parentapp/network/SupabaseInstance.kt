package com.mis.parentapp.network

import com.mis.parentapp.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.client.plugins.HttpTimeout
import kotlin.time.Duration.Companion.seconds

object SupabaseInstance {
    @OptIn(SupabaseInternal::class)
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 60.seconds.inWholeMilliseconds
                connectTimeoutMillis = 60.seconds.inWholeMilliseconds
                socketTimeoutMillis = 60.seconds.inWholeMilliseconds
            }
        }
    }
}
