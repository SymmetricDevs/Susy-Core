#version 120

// ── Emissive mesh fragment shader ────────────────────────────────────────────
//
// Renders geometry as fully unlit / self-illuminated.  Designed to be drawn
// into the scene BEFORE the bloom post-process pass so bright pixels feed
// naturally into the emissive mask threshold.
//
// Uniforms
//   u_texture     – diffuse / emission texture (GL_TEXTURE0)
//   u_emissiveColor – base emission tint, multiplied with the texture sample
//   u_emissivePower – overall brightness scalar (>1 blows out into bloom),
//                     good default: 1.5
//   u_fresnelPower  – rim-lighting exponent, 0 disables, good default: 2.0
//   u_time          – game time in seconds, drives the pulse animation
//   u_pulseSpeed    – pulse oscillation speed, 0 disables, good default: 1.0
//   u_pulseAmp      – fraction of brightness added by the pulse [0, 1],
//                     good default: 0.2
// ────────────────────────────────────────────────────────────────────────────

uniform sampler2D u_texture;
uniform vec2      u_resolution;

uniform vec3  u_emissiveColor; // tint, e.g. (1.0, 0.4, 0.0) for orange
uniform float u_emissivePower; // default 1.5

uniform float u_fresnelPower;  // default 2.0  (0 = off)

uniform float u_time;          // seconds
uniform float u_pulseSpeed;    // default 1.0  (0 = off)
uniform float u_pulseAmp;      // default 0.2

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewPos;

void main() {
    // ── base emission ────────────────────────────────────────────────────────
    vec4 texSample = texture2D(u_texture, v_texCoord);
    vec3 emission  = texSample.rgb * u_emissiveColor * u_emissivePower;

    // ── fresnel / rim ────────────────────────────────────────────────────────
    // Dot of the surface normal with the view direction.
    // Edges facing away from the camera get a stronger glow.
    if (u_fresnelPower > 0.0) {
        vec3  viewDir  = normalize(-v_viewPos);       // towards camera
        float nDotV    = max(dot(v_normal, viewDir), 0.0);
        float fresnel  = pow(1.0 - nDotV, u_fresnelPower);
        emission      += u_emissiveColor * fresnel * u_emissivePower;
    }

    // ── animated pulse ───────────────────────────────────────────────────────
    if (u_pulseSpeed > 0.0) {
        float pulse = 0.5 + 0.5 * sin(u_time * u_pulseSpeed * 6.2832);
        emission   *= 1.0 + pulse * u_pulseAmp;
    }

    // ── output ───────────────────────────────────────────────────────────────
    // Alpha comes from the texture so translucent emissive meshes work.
    // The caller should enable GL_BLEND (additive or alpha) before drawing.
    gl_FragColor = vec4(emission, texSample.a);
}
