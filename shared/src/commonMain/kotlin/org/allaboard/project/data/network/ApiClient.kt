package org.allaboard.project.data.network

import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.allaboard.project.data.repository.SupabaseClientProvider

/**
 * Reusable backend API client using Ktor.
 *
 * All frontend → backend requests should go through here.
 * Automatically injects `Authorization: Bearer <supabase JWT>` when available.
 */
object ApiClient {

    /** Override for emulator use (Android emulator needs 10.0.2.2 instead of localhost). */
    var baseUrl: String = "http://10.0.2.2:8080"


    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    /**
     * Retrieves Supabase JWT for Authorization header.
     */
    @PublishedApi
    internal fun authHeaderOrNull(): String? {
        val session = SupabaseClientProvider.client.auth.currentSessionOrNull() ?: return null
        return "Bearer ${session.accessToken}"
    }

    @PublishedApi
    internal val client: HttpClient = HttpClient {

        install(ContentNegotiation) {
            json(json)
        }

        /**
         * Base request configuration
         */
        defaultRequest {
            url(baseUrl)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }

    }

    suspend inline fun <reified Res> get(path: String): Res {
        return client.get {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
        }.body()
    }

    suspend inline fun <reified Req : Any, reified Res> post(path: String, body: Req): Res {
        return client.post {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
            setBody(body)
        }.body()
    }

    /** Use for POST requests with no body (e.g. join trip). Server may return 200/204 or empty body. */
    suspend fun postNoBody(path: String) {
        client.post {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
        }.body<String>()
    }

    /**
     * Use for POST requests that have a JSON body but no response body.
     * (Ktor still expects some payload when calling .body(), so we avoid decoding.)
     */
    suspend inline fun <reified Req : Any> postNoResponse(path: String, body: Req) {
        client.post {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
            setBody(body)
        }
    }

    suspend inline fun <reified Req : Any, reified Res> put(path: String, body: Req): Res {
        return client.put {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
            setBody(body)
        }.body()
    }

    suspend inline fun <reified Req : Any, reified Res> patch(path: String, body: Req): Res {
        return client.patch {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
            setBody(body)
        }.body()
    }

    suspend inline fun <reified Res> delete(path: String): Res {
        return client.delete {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
        }.body()
    }

    suspend fun deleteNoBody(path: String) {
        val response = client.delete {
            url(path)
            authHeaderOrNull()?.let { header(HttpHeaders.Authorization, it) }
        }
        if (response.status.value != 204) response.body<String>()
    }
}