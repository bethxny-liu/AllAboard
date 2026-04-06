package org.allaboard.project

import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.plugins.*

/**
 * Server-side Supabase client configured with the **service_role** key.
 *
 * The service_role key bypasses Row Level Security (RLS) so the backend
 * can read/write any row in the database. It is loaded from the `.env`
 * file via dotenv-java and must NEVER be exposed to the frontend.
 */
object SupabaseConfig {

    private fun envOrDotenv(key: String): String {
        // In Docker/production, env vars are the standard.
        // For local development, fall back to loading server/.env (or DOTENV_DIR override).
        val fromEnv = System.getenv(key)
        if (!fromEnv.isNullOrBlank()) return fromEnv

        val envFile = dotenv {
            directory = System.getenv("DOTENV_DIR") ?: "./server"
            filename = ".env"
            ignoreIfMissing = false
        }
        return envFile[key] ?: error("$key is not set (checked environment variables and .env)")
    }

    val supabaseUrl: String = envOrDotenv("SUPABASE_URL")

    val supabaseServiceRoleKey: String = envOrDotenv("SUPABASE_SERVICE_ROLE_KEY")

    /**
     * Singleton Supabase client for server-side use.
     * Only Postgrest is installed — JWT verification is handled separately
     * via JWKS in AuthMiddleware.
     */
    @OptIn(SupabaseInternal::class)
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseServiceRoleKey
    ) {
        install(Postgrest)

        httpConfig {
            install(HttpTimeout) {
                // Trip creation can trigger many PostgREST writes (activity suggestions, members, etc.).
                // 10s is sometimes too tight under load, so give it more headroom.
                connectTimeoutMillis = 45_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }
    }
}
