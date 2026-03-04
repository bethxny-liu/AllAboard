package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Vote, VoteType, and ActivityVoteResult. Verifies Vote creation and all fields;
 * VoteType enum values (YES, NO, SKIP); ActivityVoteResult creation with activity, counts,
 * percentage, completion/confirmation flags, and voter names.
 */
internal class VoteTest {

    private fun createActivity() = Activity(
        id = "a1",
        title = "Temple",
        location = "Tokyo",
        description = "Desc",
        mapPinLabel = "Temple",
        voteCount = 5,
        type = ActivityType.LANDMARK
    )

    @Test
    fun vote_creation_storesProperties() {
        val vote = Vote(
            id = "v1",
            activityId = "a1",
            userId = "u1",
            tripId = "t1",
            voteType = VoteType.YES,
            timestamp = 1704067200000L
        )
        assertEquals("v1", vote.id)
        assertEquals("a1", vote.activityId)
        assertEquals("u1", vote.userId)
        assertEquals("t1", vote.tripId)
        assertEquals(VoteType.YES, vote.voteType)
        assertEquals(1704067200000L, vote.timestamp)
    }

    @Test
    fun voteType_enumValues_exist() {
        assertEquals(VoteType.YES, VoteType.YES)
        assertEquals(VoteType.NO, VoteType.NO)
        assertEquals(VoteType.SKIP, VoteType.SKIP)
    }

    @Test
    fun activityVoteResult_creation_storesProperties() {
        val activity = createActivity()
        val result = ActivityVoteResult(
            activity = activity,
            yesVotes = 3,
            noVotes = 1,
            totalVotes = 4,
            yesPercentage = 75f,
            isComplete = true,
            isConfirmed = true,
            voterNames = listOf("Alice", "Bob", "Carol")
        )
        assertEquals(activity, result.activity)
        assertEquals(3, result.yesVotes)
        assertEquals(1, result.noVotes)
        assertEquals(4, result.totalVotes)
        assertEquals(75f, result.yesPercentage)
        assertEquals(true, result.isComplete)
        assertEquals(true, result.isConfirmed)
        assertEquals(3, result.voterNames.size)
        assertEquals("Alice", result.voterNames[0])
    }
}
