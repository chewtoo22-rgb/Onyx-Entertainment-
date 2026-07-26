package com.onyx.avhub.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Placeholder instrumented test proving the androidTest harness (device/emulator-backed
 * checks like the AudioEffect attach/detach lifecycle called for in the project plan) is
 * wired up; expand this suite as those device-level checks are implemented.
 */
@RunWith(AndroidJUnit4::class)
class ApplicationIdTest {

    @Test
    fun appContextHasExpectedPackageName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(context.packageName).isEqualTo("com.onyx.avhub")
    }
}
