# Onyx AV Hub

The most advanced audio + video enhancement hub for Android: a system-wide audio
equalizer/DSP hub (think Wavelet/Poweramp, but built from the ground up) paired
with a full-featured video player carrying its own GPU-accelerated enhancement
pipeline (codec control, upscaling, color grading, HDR tone-mapping).

## Why the app is split this way

Android lets an app attach audio effects to the **global/system audio session**
(session `0`), so audio enhancement can genuinely apply to every app on the
device without root. There is **no equivalent API for video** — an app can only
post-process video frames it is itself decoding and rendering. So:

- **Audio** = a background hub service enhancing all audio on the device.
- **Video** = a best-in-class built-in player with a GPU enhancement pipeline,
  since that's the only place video pixels can be touched.

## Module map

| Module | Purpose |
|---|---|
| `app` | Compose UI shell, navigation, Hilt wiring |
| `feature-audio-hub` | System-wide audio effects screen + foreground service + Quick Settings Tile |
| `feature-player` | Video/audio player screen, codec settings UI |
| `core-dsp` | Native (C++/NDK) biquad DSP engine + RBJ filter-design math |
| `core-video-gl` | GLSL shader effects plugged into Media3's video effects pipeline |
| `core-media` | ExoPlayer/Media3 setup, custom `MediaCodecSelector`, codec inspection |
| `core-data` | Room (presets, per-app profiles) + DataStore (settings) |
| `core-common` | Compose theme, shared utils, dispatcher abstraction |

## Building

Requires JDK 17, Android SDK (`compileSdk 34`), and the NDK
(`26.3.11579264`) + CMake (`3.22.1`) for `core-dsp`'s native module.

```bash
./gradlew assembleDebug       # build the app
./gradlew testDebugUnitTest   # run unit tests (DSP math, codec selection, etc.)
./gradlew lint                # static analysis
```

## Roadmap

This repo is being built in phases (see the project plan for full detail):

0. **Foundation** — this scaffold: multi-module project, CI, architecture docs.
1. **System-Wide Audio Hub MVP** — global-session platform effects (Equalizer,
   BassBoost, Virtualizer, Reverb, LoudnessEnhancer, DynamicsProcessing),
   Compose EQ screen, presets, Quick Settings Tile.
2. **Advanced Audio DSP** — native convolution/IR engine, crossfeed, per-app
   auto-profiles, spectrum/RTA visualizer, loudness normalization.
3. **Video Player MVP** — full platform codec support, subtitle rendering,
   codec/decoder selection UI, HDR passthrough, GL effect chain proven end-to-end.
4. **Advanced Video Enhancement** — full shader library (scaling, sharpen,
   denoise, color grade, tone-map, deinterlace).
5. **AI Upscaling (stretch)** — on-device super-resolution model as an
   additional GL effect, gated by device capability.
6. **Polish & Ecosystem** — preset import/export, backup/restore, tablet
   layouts, Play Store readiness.

**Current status:** Phase 0 scaffold plus Phase 1/3 building blocks (platform
audio effect chain, ExoPlayer + codec selector, identity GL effect pass) are in
place; the enhancement curves/filters themselves are the next work.
