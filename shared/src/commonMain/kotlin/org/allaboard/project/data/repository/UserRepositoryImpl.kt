package org.allaboard.project.data.repository

import org.allaboard.project.data.network.ApiClient
import org.allaboard.project.domain.BudgetLevel
import org.allaboard.project.domain.UpdateUserPreferencesRequest
import org.allaboard.project.domain.TravelVibe
import org.allaboard.project.domain.User

/**
 * Real UserRepository implementation backed by your backend server.
 *
 * All requests automatically include the Supabase JWT (if logged in).
 */
class UserRepositoryImpl : UserRepository {

    private var currentUserId: String? = null
    private var cachedUser: User? = null

    override suspend fun getCurrentUser(): User? {
        // Backend validates the JWT and returns the user row from your custom users table.
        // Returns null when there is no valid session or the call fails.
        return try {
            val user: User = ApiClient.get("/user/me")
            cachedUser = user
            currentUserId = user.id
            return user
        } catch (e: Throwable) {
            println(e.message)
            throw e
        }
    }

    override suspend fun setCurrentUserId(userId: String) {
        // NOT NEEDED IN REAL IMPL. Only used in mock
    }

    override suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    ) {
        val body = UpdateUserPreferencesRequest(
            budgetLevel = budget.name,
            travelVibe = vibe.name,
            interests = interests.toList()
        )
        ApiClient.patch<UpdateUserPreferencesRequest, Unit>("/user/me/preferences", body)
    }
}
