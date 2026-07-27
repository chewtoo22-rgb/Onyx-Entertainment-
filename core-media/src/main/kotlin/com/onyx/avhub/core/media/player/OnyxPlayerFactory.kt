package com.onyx.avhub.core.media.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.core.media.codec.OnyxMediaCodecSelector
import com.onyx.avhub.core.videogl.pipeline.OnyxVideoEffectsFactory
import com.onyx.avhub.core.videogl.pipeline.VideoEnhancementParams
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds [ExoPlayer] instances wired with Onyx's codec preference selector and GL
 * enhancement pipeline, so every player in the app (video screen, mini-player, etc.)
 * gets the same enhancement stack instead of a plain default player.
 */
@Singleton
class OnyxPlayerFactory @Inject constructor(
    private val videoEffectsFactory: OnyxVideoEffectsFactory,
) {

    @OptIn(markerClass = [UnstableApi::class])
    fun create(
        context: Context,
        decoderPreference: (mimeType: String) -> DecoderPreference = { DecoderPreference.AUTO },
        videoEnhancementParams: VideoEnhancementParams = VideoEnhancementParams(),
        enableHiResFloatAudio: Boolean = false,
    ): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context)
            .setMediaCodecSelector(OnyxMediaCodecSelector(decoderPreference))
            .setEnableDecoderFallback(true)
            // Bit-perfect/Hi-Res output: bypasses ExoPlayer's integer audio processing chain
            // (see DefaultRenderersFactory docs) in favor of a float PCM path to the sink.
            .setEnableAudioFloatOutput(enableHiResFloatAudio)

        return ExoPlayer.Builder(context, renderersFactory)
            .build()
            .apply { setVideoEffects(videoEffectsFactory.buildEffects(videoEnhancementParams)) }
    }
}
