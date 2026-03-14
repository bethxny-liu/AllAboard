package org.allaboard.project.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.allaboard.project.SupabaseConfig
import java.security.interfaces.ECPublicKey
import java.util.concurrent.TimeUnit

/**
 * Convenience extension to read the authenticated Supabase user id (`sub` claim)
 * from the [JWTPrincipal] that Ktor's auth layer stores on the call.
 */
val ApplicationCall.userId: String
    get() = principal<JWTPrincipal>()!!.payload.subject

/**
 * Installs Ktor JWT authentication that verifies tokens against Supabase's
 * **JWKS endpoint** using **ES256 (P-256 ECDSA)**.
 *
 * Supabase publishes its public keys at `<project-url>/auth/v1/jwks`.
 * The `jwks-rsa` library fetches and caches them so we don't hit the
 * endpoint on every request.
 *
 * The provider is registered as **"supabase-jwt"** — wrap protected routes with
 * `authenticate("supabase-jwt") { … }`.
 */
fun Application.configureAuth() {

    val issuer = "${SupabaseConfig.supabaseUrl}/auth/v1"

    // Fetch and cache JWKS from Supabase
    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)      // cache up to 10 keys for 24 h
        .rateLimited(10, 1, TimeUnit.MINUTES) // max 10 requests per minute
        .build()

    install(Authentication) {
        jwt("supabase-jwt") {
            realm = "allaboard"

            verifier(jwkProvider, issuer) {
                // Supabase sets aud to "authenticated" for logged-in users
                withAudience("authenticated")
            }

            validate { credential ->
                val sub = credential.payload.subject
                if (sub != null) JWTPrincipal(credential.payload) else null
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            }
        }
    }
}
