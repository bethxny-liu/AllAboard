package org.allaboard.project

import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.allaboard.project.auth.configureAuth
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.UpdateUserPreferencesRequest
import org.allaboard.project.domain.User

fun main() {
    // Eagerly initialise the Supabase client so .env errors surface immediately
    println("[server] Supabase URL = ${SupabaseConfig.supabaseUrl}")

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            }
        )
    }

    // Log every request + response body
    install(RequestLoggingMiddleware)

    // JWT authentication — verifies Supabase JWTs with HMAC256
    configureAuth()

    routing {
        // ── Public routes (no auth required) ────────────────────────────
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // ── Protected routes (JWT required) ─────────────────────────────
        authenticate("supabase-jwt") {
            get("/user/me") {
                val userId = call.userId

                val rows = SupabaseConfig.client.from("users")
                    .select { filter { eq("id", userId) } }
                    .decodeList<User>()

                val user = rows.firstOrNull()
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@get
                }
                call.respond(user)
            }
            patch("/user/me/preferences") {
                val userId = call.userId
                val body = call.receive<UpdateUserPreferencesRequest>()
                SupabaseConfig.client.from("users").update({
                    set("budget_level", body.budgetLevel)
                    set("travel_vibe", body.travelVibe)
                    set("interests", body.interests)
                }) {
                    filter { eq("id", userId) }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}