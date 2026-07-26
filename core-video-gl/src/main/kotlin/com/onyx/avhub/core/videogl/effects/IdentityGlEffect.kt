package com.onyx.avhub.core.videogl.effects

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram

/** [GlEffect] wrapper for [IdentityShaderProgram]. */
@OptIn(markerClass = [UnstableApi::class])
class IdentityGlEffect : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram =
        IdentityShaderProgram()
}
