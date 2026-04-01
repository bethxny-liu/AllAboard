package org.allaboard.project.data.repository

/**
 * Holds Google OAuth provider tokens captured from the Supabase OAuth callback.
 *
 * These are required for privileged Google APIs (e.g., Calendar events.insert).
 */
object GoogleOAuthTokenStore {
    var providerAccessToken: String? = null
    var providerRefreshToken: String? = null

    fun clear() {
        providerAccessToken = null
        providerRefreshToken = null
    }
}
