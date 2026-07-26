package com.onyx.avhub.core.data

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.data.repository.BuiltInPresets
import org.junit.Test

class BuiltInPresetsTest {

    @Test
    fun `built-in presets have stable non-zero ids so re-seeding is idempotent`() {
        val ids = BuiltInPresets.ALL.map { it.id }
        assertThat(ids).containsNoDuplicates()
        assertThat(ids).doesNotContain(0L)
    }

    @Test
    fun `every preset has one gain per band frequency`() {
        BuiltInPresets.ALL.forEach { preset ->
            assertThat(preset.bandGainsDb.size).isEqualTo(preset.bandCenterFreqHz.size)
        }
    }

    @Test
    fun `all built-in presets are flagged as built-in`() {
        assertThat(BuiltInPresets.ALL.all { it.isBuiltIn }).isTrue()
    }
}
