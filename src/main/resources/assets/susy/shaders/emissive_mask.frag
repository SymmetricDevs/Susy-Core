#version 120

// ── Bloom pass 1 of 3: emissive mask ────────────────────────────────────────
//
// Extracts pixels whose perceived luminance exceeds u_threshold.
// Pixels below the threshold are output as black (vec4(0)), which lets the
// downstream blur passes naturally fall off to nothing without a hard edge.
//
// Uniforms
//   u_texture    – the full rendered scene (GL_TEXTURE0)
//   u_threshold  – luminance cutoff  [0, 1],  good default: 0.6
//   u_knee       – soft-knee width   [0, 0.5], good default: 0.1
//                  Controls how gently bright areas transition into the mask.
// ────────────────────────────────────────────────────────────────────────────

uniform sampler2D u_texture;
uniform vec2      u_resolution;
uniform float     u_threshold; // default 0.6
uniform float     u_knee;      // default 0.1

varying vec2 v_texCoord;

// Perceptual luminance weights (Rec. 709)
const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

void main() {
    vec4 color = texture2D(u_texture, v_texCoord);
    float luma = dot(color.rgb, LUMA);

    // Soft-knee curve: smoothly ramp in brightness above the threshold.
    // remap = clamp((luma - (threshold - knee)) / (2.0 * knee), 0, 1)
    float lo    = u_threshold - u_knee;
    float hi    = u_threshold + u_knee;
    float remap = clamp((luma - lo) / max(hi - lo, 0.0001), 0.0, 1.0);

    // Smooth-step the remap so the transition has no visible kink
    remap = remap * remap * (3.0 - 2.0 * remap);

    gl_FragColor = vec4(color.rgb * remap, color.a);
}
