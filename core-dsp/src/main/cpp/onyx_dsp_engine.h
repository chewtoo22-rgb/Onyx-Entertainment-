#ifndef ONYX_DSP_ENGINE_H
#define ONYX_DSP_ENGINE_H

#include <cstddef>
#include <cstdint>
#include <vector>

namespace onyx {

// Direct Form I biquad, coefficients normalized so a0 == 1 (matches
// BiquadCoefficients.normalized() on the Kotlin side).
struct Biquad {
    double b0 = 1.0, b1 = 0.0, b2 = 0.0;
    double a1 = 0.0, a2 = 0.0;

    // Per-channel filter state (x[n-1], x[n-2], y[n-1], y[n-2]).
    double x1 = 0.0, x2 = 0.0, y1 = 0.0, y2 = 0.0;

    float ProcessSample(float inputSample) {
        double x0 = static_cast<double>(inputSample);
        double y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
        x2 = x1;
        x1 = x0;
        y2 = y1;
        y1 = y0;
        return static_cast<float>(y0);
    }
};

// A cascade of `bandCount` biquads per channel, applied in series per sample.
// Coefficients are shared across channels for a given band; filter state (x1/x2/y1/y2)
// is kept separately per channel so stereo (or multi-channel) audio filters correctly.
class DspEngine {
public:
    DspEngine(int sampleRateHz, int channelCount, int bandCount);

    void SetBand(int bandIndex, double b0, double b1, double b2, double a1, double a2);
    void Process(float* interleavedBuffer, int frameCount);

private:
    int sample_rate_hz_;
    int channel_count_;
    int band_count_;
    // bands_[bandIndex][channel]
    std::vector<std::vector<Biquad>> bands_;
};

}  // namespace onyx

#endif  // ONYX_DSP_ENGINE_H
