package org.allaboard.project.data.repository

import org.allaboard.project.domain.BudgetLevel
import org.allaboard.project.domain.TravelVibe
import org.allaboard.project.domain.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getUser(userId: String): User?
    suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    )
    suspend fun updateUserProfile(user: User)
}
