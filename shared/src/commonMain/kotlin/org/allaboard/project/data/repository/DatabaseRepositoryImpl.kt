package org.allaboard.project.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.createSupabaseClient

/**
 * Singleton that holds the configured Supabase client instance.
 *
 * Note: The frontend should NOT access your domain tables directly. We only use Supabase
 * here for OAuth + session/JWT management.
 */
object SupabaseClientProvider {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://carrfulbtryqypkcdynd.supabase.co",
        supabaseKey = "sb_publishable_1qvUUxZrU743akoFaYDo3w_CJ2tC-ne"
    ) {
        install(Auth) {
            scheme = "org.allaboard.project"
            host = "callback"
        }
    }
}

class DatabaseRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : DatabaseRepository {

    override suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Google, redirectUrl = "org.allaboard.project://callback")
    }
}
