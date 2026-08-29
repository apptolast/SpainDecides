package com.apptolast.spaindecides.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProposalTest {

    private fun proposal(upvotes: Int = 0, downvotes: Int = 0) = Proposal(
        id = "id",
        title = "A valid title",
        description = "A valid description",
        categoryId = "cat",
        userId = "user",
        upvotes = upvotes,
        downvotes = downvotes
    )

    @Test
    fun netVotesIsUpvotesMinusDownvotes() {
        assertEquals(7, proposal(upvotes = 10, downvotes = 3).netVotes)
        assertEquals(-5, proposal(upvotes = 0, downvotes = 5).netVotes)
    }

    @Test
    fun formattedVotesBelowThousandIsPlainNumber() {
        assertEquals("999", proposal(upvotes = 999).formattedVotes)
        assertEquals("0", proposal().formattedVotes)
        assertEquals("-12", proposal(downvotes = 12).formattedVotes)
    }

    @Test
    fun formattedVotesAboveThousandUsesKSuffix() {
        assertEquals("1.0K", proposal(upvotes = 1000).formattedVotes)
        assertEquals("1.5K", proposal(upvotes = 1500).formattedVotes)
        assertEquals("12.3K", proposal(upvotes = 12345).formattedVotes)
    }

    @Test
    fun decodesSupabaseRowWithSnakeCaseAndUnknownKeys() {
        // Same Json settings as the app's Ktor/Supabase clients
        val json = Json { ignoreUnknownKeys = true }
        val row = """
            {
              "id": "3f7c",
              "title": "Mejorar el transporte",
              "description": "Descripción detallada",
              "short_description": "Resumen",
              "category_id": "cat-1",
              "user_id": "user-1",
              "upvotes": 4,
              "downvotes": 1,
              "created_at": "2026-08-29T10:00:00Z",
              "some_future_column": "ignored"
            }
        """.trimIndent()

        val proposal = json.decodeFromString<Proposal>(row)

        assertEquals("3f7c", proposal.id)
        assertEquals("Resumen", proposal.shortDescription)
        assertEquals("cat-1", proposal.categoryId)
        assertEquals("user-1", proposal.userId)
        assertEquals(3, proposal.netVotes)
        assertEquals("2026-08-29T10:00:00Z", proposal.createdAt)
    }

    @Test
    fun optionalFieldsDefaultWhenAbsent() {
        val json = Json { ignoreUnknownKeys = true }
        val row = """
            {
              "id": "1",
              "title": "t",
              "description": "d",
              "category_id": "c",
              "user_id": "u"
            }
        """.trimIndent()

        val proposal = json.decodeFromString<Proposal>(row)

        assertNull(proposal.shortDescription)
        assertNull(proposal.createdAt)
        assertEquals(0, proposal.upvotes)
        assertEquals(0, proposal.downvotes)
    }
}
