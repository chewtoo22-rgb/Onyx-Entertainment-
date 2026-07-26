#include "onyx_dsp_engine.h"

#include <algorithm>

namespace onyx {

DspEngine::DspEngine(int sampleRateHz, int channelCount, int bandCount)
    : sample_rate_hz_(sampleRateHz),
      channel_count_(std::max(1, channelCount)),
      band_count_(std::max(0, bandCount)),
      bands_(static_cast<size_t>(std::max(0, bandCount)),
             std::vector<Biquad>(static_cast<size_t>(std::max(1, channelCount)))) {}

void DspEngine::SetBand(int bandIndex, double b0, double b1, double b2, double a1, double a2) {
    if (bandIndex < 0 || bandIndex >= band_count_) {
        return;
    }
    for (auto& channelFilter : bands_[static_cast<size_t>(bandIndex)]) {
        // Coefficients change, but existing filter state (x1/x2/y1/y2) is preserved so
        // live EQ adjustments don't click/pop mid-playback.
        channelFilter.b0 = b0;
        channelFilter.b1 = b1;
        channelFilter.b2 = b2;
        channelFilter.a1 = a1;
        channelFilter.a2 = a2;
    }
}

void DspEngine::Process(float* interleavedBuffer, int frameCount) {
    for (int frame = 0; frame < frameCount; ++frame) {
        for (int channel = 0; channel < channel_count_; ++channel) {
            int index = frame * channel_count_ + channel;
            float sample = interleavedBuffer[index];
            for (int band = 0; band < band_count_; ++band) {
                sample = bands_[static_cast<size_t>(band)][static_cast<size_t>(channel)]
                             .ProcessSample(sample);
            }
            interleavedBuffer[index] = sample;
        }
    }
}

}  // namespace onyx
