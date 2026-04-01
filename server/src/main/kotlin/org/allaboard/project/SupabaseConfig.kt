package org.allaboard.project

import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.plugins.*
import org.slf4j.LoggerFactory

/**
 * Server-side Supabase client configured with the **service_role** key.
 *
 * The service_role key bypasses Row Level Security (RLS) so the backend
 * can read/write any row in the database. It is loaded from the `.env`
 * file via dotenv-java and must NEVER be exposed to the frontend.
 */
object SupabaseConfig {

    private val env = dotenv {
        directory = "./server"
        filename = ".env"
        ignoreIfMissing = false
    }

    val supabaseUrl: String = env["SUPABASE_URL"]
        ?: error("SUPABASE_URL is not set in .env")

    val supabaseServiceRoleKey: String = env["SUPABASE_SERVICE_ROLE_KEY"]
        ?: error("SUPABASE_SERVICE_ROLE_KEY is not set in .env")

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
            val logger = LoggerFactory.getLogger("SupabaseHttp")

            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}
