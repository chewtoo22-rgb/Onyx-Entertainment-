package com.onyx.avhub.core.videogl.effects

import android.opengl.GLES20
import androidx.annotation.OptIn
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram

/**
 * A full-screen-quad shader program that copies the input frame to the output unchanged.
 *
 * This is the Phase 3 scaffold proving the decode -> GL effects -> render chain works
 * end-to-end. Phase 4 filters (scaling, sharpen, color grade, tone-map) are implemented as
 * additional [BaseGlShaderProgram]s using this same quad-drawing pattern, with their own
 * fragment shaders reading [VideoEnhancementParams][com.onyx.avhub.core.videogl.pipeline.VideoEnhancementParams]
 * uniforms instead of a plain passthrough.
 */
@OptIn(markerClass = [UnstableApi::class])
class IdentityShaderProgram :
    BaseGlShaderProgram(/* useHighPrecisionColorComponents= */ false, /* texturePoolCapacity= */ 1) {

    private var glProgram: GlProgram? = null

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        if (glProgram == null) {
            glProgram = try {
                GlProgram(VERTEX_SHADER, FRAGMENT_SHADER)
            } catch (e: GlUtil.GlException) {
                throw VideoFrameProcessingException(e)
            }
        }
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
        // Full-screen triangle strip in NDC space, paired with matching texture coordinates.
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
            varying vec2 vTexCoords;
            void main() {
              gl_FragColor = texture2D(uTexSampler, vTexCoords);
            }
        """
    }
}
