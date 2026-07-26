package com.onyx.avhub.app

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.app.navigation.OnyxDestination
import org.junit.Test

class OnyxDestinationTest {

    @Test
    fun `every destination has a unique route`() {
        val routes = OnyxDestination.entries.map { it.route }
        assertThat(routes).containsNoDuplicates()
    }
}
