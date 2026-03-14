package org.allaboard.project

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.slf4j.LoggerFactory

/**
 * Ktor server plugin that logs every request and its response body.
 *
 * Logs:
 *  → REQUEST:  method, path, query params, Authorization presence
 *  ← RESPONSE: status code, response body text
 *
 * Uses SLF4J (logback) so output appears in the normal server console.
 */
val RequestLoggingMiddleware = createApplicationPlugin(name = "RequestLogging") {

    val logger = LoggerFactory.getLogger("RequestLogging")

    // ── Log the incoming request ────────────────────────────────────────
    onCall { call ->
        val request = call.request
        val method = request.httpMethod.value
        val uri = request.uri
        val hasAuth = request.headers["Authorization"] != null

        logger.info("→ $method $uri (auth=${if (hasAuth) "yes" else "no"})")
        logger.info("request jwt" + request.headers["Authorization"])
    }

    // ── Log the outgoing response (including body) ──────────────────────
    onCallRespond { call, body ->
        val status = call.response.status()?.value ?: "?"
        val method = call.request.httpMethod.value
        val uri = call.request.uri

        // Serialise the response body to a loggable string.
        // `body` is the object passed to `call.respond(...)`.
        val bodyText = when (body) {
            is OutgoingContent.NoContent -> "<no body>"
            is String -> body
            else -> body.toString()
        }

        logger.info("← $status $method $uri")
        logger.info("← body: $bodyText")
    }
}
