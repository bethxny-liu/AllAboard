package org.allaboard.project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.launch
import org.allaboard.project.data.repository.GoogleOAuthTokenStore
import org.allaboard.project.data.repository.SupabaseClientProvider
import org.allaboard.project.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Handle Supabase OAuth deep link on cold start
        handleAuthCallback(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle Supabase OAuth deep link when activity is already running
        handleAuthCallback(intent)
    }

    /**
     * Parses the OAuth callback deep link and imports the session into Supabase Auth.
     * The redirect URL (org.allaboard.project://callback) returns tokens in the URI fragment
     * e.g. ...#access_token=xxx&refresh_token=yyy&expires_in=3600&token_type=bearer
     */
    private fun handleAuthCallback(intent: Intent) {
        val uri: Uri = intent.data ?: return
        // Only handle our OAuth callback scheme+host
        if (uri.scheme != "org.allaboard.project" || uri.host != "callback") return

        // The tokens are in the fragment (after #)
        val fragment = uri.fragment ?: return
        val params = fragment.split("&").mapNotNull { entry ->
            val parts = entry.split("=", limit = 2)
            val key = parts.getOrNull(0) ?: return@mapNotNull null
            val value = parts.getOrNull(1).orEmpty()
            key to Uri.decode(value)
        }.toMap()

        // Supabase callback may include Google provider tokens when available.
        GoogleOAuthTokenStore.providerAccessToken = params["provider_token"]
        GoogleOAuthTokenStore.providerRefreshToken = params["provider_refresh_token"]

        val accessToken = params["access_token"] ?: return
        val refreshToken = params["refresh_token"] ?: return
        val expiresIn = params["expires_in"]?.toLongOrNull() ?: 3600
        val tokenType = params["token_type"] ?: "bearer"

        if (GoogleOAuthTokenStore.providerAccessToken.isNullOrBlank()) {
            Log.w(
                "auth",
                "Missing provider_token in OAuth callback. Google Calendar sync may require re-login with proper scopes."
            )
        }

        lifecycleScope.launch {
            SupabaseClientProvider.client.auth.importSession(
                UserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn,
                    tokenType = tokenType,
                    user = null
                ),
                source = io.github.jan.supabase.auth.status.SessionSource.External
            )
            // After importing, retrieve the full user info
            val user = SupabaseClientProvider.client.auth.retrieveUserForCurrentSession()
            Log.v("user","Logged in user: ${user?.email}")
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
