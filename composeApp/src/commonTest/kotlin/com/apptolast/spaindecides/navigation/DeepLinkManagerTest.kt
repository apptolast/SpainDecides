package com.apptolast.spaindecides.navigation

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkManagerTest {

    @AfterTest
    fun tearDown() {
        // DeepLinkManager is a global singleton - leave it clean for other tests
        DeepLinkManager.consumeDeepLink()
    }

    @Test
    fun pendingDeepLinkIsNullInitially() {
        assertNull(DeepLinkManager.pendingDeepLink.value)
    }

    @Test
    fun setDeepLinkExposesItAsPending() {
        val deepLink = DeepLink.ProposalDetail(proposalId = "p1", categoryId = "c1")

        DeepLinkManager.setDeepLink(deepLink)

        assertEquals(deepLink, DeepLinkManager.pendingDeepLink.value)
    }

    @Test
    fun consumeDeepLinkClearsPendingState() {
        DeepLinkManager.setDeepLink(DeepLink.ProposalDetail("p1", "c1"))

        DeepLinkManager.consumeDeepLink()

        assertNull(DeepLinkManager.pendingDeepLink.value)
    }

    @Test
    fun settingANewDeepLinkReplacesThePreviousOne() {
        DeepLinkManager.setDeepLink(DeepLink.ProposalDetail("p1", "c1"))
        val second = DeepLink.ProposalDetail("p2", "c2")

        DeepLinkManager.setDeepLink(second)

        assertEquals(second, DeepLinkManager.pendingDeepLink.value)
    }
}
