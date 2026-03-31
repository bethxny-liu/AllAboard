package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import org.allaboard.project.data.repository.UserRepository
import org.allaboard.project.domain.*

class MockUserRepository : UserRepository {
    private var currentUserId = "user-1"

    private val users = mutableMapOf(
        "user-1" to User(
            id = "user-1",
            displayName = "Daniel",
            email = "daniel@allaboard.com",
            budget = BudgetLevel.MEDIUM,
            travelVibe = TravelVibe.BALANCED,
            interests = setOf("Culture", "Food", "Adventure")
        ),
        "user-2" to User(
            id = "user-2",
            displayName = "Rachael",
            email = "rachael@allaboard.com",
            budget = BudgetLevel.HIGH,
            travelVibe = TravelVibe.RELAXED,
            interests = setOf("Culture", "Fine Dining")
        ),
        "user-3" to User(
            id = "user-3",
            displayName = "Sarah",
            email = "sarah@allaboard.com",
            budget = BudgetLevel.MEDIUM,
            travelVibe = TravelVibe.ADVENTUROUS,
            interests = setOf("Adventure", "Nature")
        ),
        "user-4" to User(
            id = "user-4",
            displayName = "Bethany",
            email = "bethany@allaboard.com",
            budget = BudgetLevel.LOW,
            travelVibe = TravelVibe.BALANCED,
            interests = setOf("History", "Photography")
        )
    )

    override suspend fun getCurrentUser(): User? {
        delay(50)
        return users[currentUserId]
    }

    override suspend fun setCurrentUserId(userId: String) {
        delay(50)
        if (users.containsKey(userId)) {
            currentUserId = userId
        }
    }

    override suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    ) {
        delay(100)
        users[userId]?.let { user ->
            users[userId] = user.copy(
                budget = budget,
                travelVibe = vibe,
                interests = interests
            )
        }
    }
    override suspend fun clearCache() {
        // No caching in mock, so nothing to clear
    }
}
