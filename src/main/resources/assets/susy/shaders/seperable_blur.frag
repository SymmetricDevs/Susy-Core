#version 120

// Run this shader TWICE:
//   Pass A (horizontal):  u_direction = (1, 0)
//   Pass B (vertical):    u_direction = (0, 1)
//
// A two-pass separable blur is mathematically identical to a 2-D Gaussian
// but requires only 2*N samples instead of N^2 – much cheaper.
//
// Uniforms
//   u_texture   - input image (emissive mask on pass A, horizontal result on B)
//   u_direction - (1,0) for horizontal, (0,1) for vertical
//   u_radius    - blur radius in pixels, good default: 4.0

uniform sampler2D u_texture;
uniform vec2      u_resolution;
uniform vec2      u_direction; // (1,0) or (0,1)
uniform float     u_radius;    // default 4.0

varying vec2 v_texCoord;

// Gaussian weight for offset x given standard-deviation sigma.
float gaussianWeight(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma));
}

void main() {
    vec2 texel = 1.0 / u_resolution;     // size of one texel in UV space
    vec2 step  = u_direction * texel;    // step vector along the blur axis

    float sigma  = max(u_radius * 0.5, 0.001);
    int   radius = int(u_radius);

    vec4  color     = vec4(0.0);
    float totalW    = 0.0;

    for (int i = -radius; i <= radius; i++) {
        float w    = gaussianWeight(float(i), sigma);
        vec2  uv   = v_texCoord + step * float(i);
        color      += texture2D(u_texture, uv) * w;
        totalW     += w;
    }

    gl_FragColor = color / totalW;
}
