package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for Vote, VoteType, and ActivityVoteResult. Verifies Vote creation and all fields;
 * VoteType enum values (YES, NO, SUPER); ActivityVoteResult creation with activity, counts,
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
            voteType = VoteType.YES
        )
        assertEquals("v1", vote.id)
        assertEquals("a1", vote.activityId)
        assertEquals("u1", vote.userId)
        assertEquals("t1", vote.tripId)
        assertEquals(VoteType.YES, vote.voteType)
    }

    @Test
    fun voteType_enumValues_exist() {
        assertEquals(VoteType.YES, VoteType.YES)
        assertEquals(VoteType.NO, VoteType.NO)
        assertEquals(VoteType.SUPER, VoteType.SUPER)
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

    @Test
    fun vote_idNullable_defaults() {
        val vote = Vote(
            id = null,
            activityId = "a1",
            userId = "u1",
            tripId = "t1",
            voteType = VoteType.SUPER
        )
        assertNull(vote.id)
        assertEquals(VoteType.SUPER, vote.voteType)
    }

    @Test
    fun activityVoteResult_zeroVotes_edgeCase() {
        val activity = createActivity()
        val result = ActivityVoteResult(
            activity = activity,
            yesVotes = 0,
            noVotes = 0,
            totalVotes = 0,
            yesPercentage = 0f,
            isComplete = false,
            isConfirmed = false,
            voterNames = emptyList()
        )
        assertEquals(0, result.totalVotes)
        assertEquals(true, result.voterNames.isEmpty())
    }
}
