package com.onyx.avhub.core.media.codec

/** Per-codec hardware/software decode preference, exposed in the player's codec settings UI. */
enum class DecoderPreference {
    /** Let the platform pick (its own priority order, usually hardware-first). */
    AUTO,

    /** Prefer hardware decoders but fall back to software if none can play the format. */
    PREFER_HARDWARE,

    /** Prefer software decoders but fall back to hardware if none can play the format. */
    PREFER_SOFTWARE,

    /** Only use hardware decoders; playback fails if none support the format. */
    FORCE_HARDWARE,

    /** Only use software decoders; playback fails if none support the format. */
    FORCE_SOFTWARE,
}
