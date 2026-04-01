package org.allaboard.project.data.repository

interface DatabaseRepository {
    /** Starts the Supabase Google OAuth flow (opens browser on Android). */
    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun logout()
}
