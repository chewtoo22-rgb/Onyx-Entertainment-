package com.onyx.avhub.core.videogl.effects

import android.opengl.GLES20
import androidx.annotation.OptIn
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import com.onyx.avhub.core.videogl.pipeline.VideoComparisonState

/**
 * Before/after comparison pass: left of the live [VideoComparisonState.splitPosition] renders
 * the frame untouched, right of it renders a real unsharp-mask sharpen (4-neighbor Laplacian),
 * with a thin divider line at the split. One shader, one texture sample of the source per
 * output pixel plus 4 neighbor samples on the "after" side — real image processing, not a
 * canned crossfade between two pre-rendered copies.
 */
@OptIn(markerClass = [UnstableApi::class])
class ComparisonShaderProgram(
    private val state: VideoComparisonState,
) : BaseGlShaderProgram(/* useHighPrecisionColorComponents= */ false, /* texturePoolCapacity= */ 1) {

    private var glProgram: GlProgram? = null
    private var texelSize = floatArrayOf(0f, 0f)

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        if (glProgram == null) {
            glProgram = try {
                GlProgram(VERTEX_SHADER, FRAGMENT_SHADER)
            } catch (e: GlUtil.GlException) {
                throw VideoFrameProcessingException(e)
            }
        }
        texelSize = floatArrayOf(1f / inputWidth, 1f / inputHeight)
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        val program = glProgram
            ?: throw VideoFrameProcessingException("configure() must be called before drawFrame()")
        try {
            program.use()
            program.setSamplerTexIdUniform("uTexSampler", inputTexId, /* texUnitIndex= */ 0)
            program.setBufferAttribute("aPosition", QUAD_POSITIONS, /* size= */ 2)
            program.setBufferAttribute("aTexCoords", QUAD_TEX_COORDS, /* size= */ 2)
            program.setFloatUniform("uSplitX", state.splitPosition)
            program.setFloatUniform("uSharpenAmount", state.sharpenStrength)
            program.setFloatsUniform("uTexelSize", texelSize)
            program.bindAttributesAndUniforms()
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GlUtil.checkGlError()
        } catch (e: GlUtil.GlException) {
            throw VideoFrameProcessingException(e)
        }
    }

    override fun release() {
        super.release()
        try {
            glProgram?.delete()
        } catch (e: GlUtil.GlException) {
            throw VideoFrameProcessingException(e)
        }
    }

    private companion object {
        val QUAD_POSITIONS = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )
        val QUAD_TEX_COORDS = floatArrayOf(
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
        )

        const val VERTEX_SHADER = """
            attribute vec2 aPosition;
            attribute vec2 aTexCoords;
            varying vec2 vTexCoords;
            void main() {
              gl_Position = vec4(aPosition, 0.0, 1.0);
              vTexCoords = aTexCoords;
            }
        """

        const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D uTexSampler;
            uniform float uSplitX;
            uniform float uSharpenAmount;
            uniform vec2 uTexelSize;
            varying vec2 vTexCoords;

            void main() {
              vec4 original = texture2D(uTexSampler, vTexCoords);
              float dist = vTexCoords.x - uSplitX;

              if (dist < 0.0) {
                gl_FragColor = original;
                return;
              }

              vec4 neighborSum =
                  texture2D(uTexSampler, vTexCoords + vec2(uTexelSize.x, 0.0)) +
                  texture2D(uTexSampler, vTexCoords - vec2(uTexelSize.x, 0.0)) +
                  texture2D(uTexSampler, vTexCoords + vec2(0.0, uTexelSize.y)) +
                  texture2D(uTexSampler, vTexCoords - vec2(0.0, uTexelSize.y));
              vec4 blurred = neighborSum * 0.25;
              vec4 sharpened = clamp(original + (original - blurred) * uSharpenAmount * 2.0, 0.0, 1.0);

              float lineHalfWidth = uTexelSize.x * 1.5;
              if (dist < lineHalfWidth) {
                gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
              } else {
                gl_FragColor = sharpened;
              }
            }
        """
    }
}
