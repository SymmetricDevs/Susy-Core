#version 120

// ── Bloom pass 3 of 3: composite ────────────────────────────────────────────
//
// Additively blends the blurred emissive bloom onto the original scene.
//
// Uniforms
//   u_texture      – the original scene (GL_TEXTURE0)
//   u_bloomTexture – the blurred emissive mask (GL_TEXTURE1)
//   u_intensity    – how strong the bloom glow is, good default: 1.0
//                    Values > 1 produce HDR-style over-exposure.
// ────────────────────────────────────────────────────────────────────────────

uniform sampler2D u_texture;      // scene
uniform sampler2D u_bloomTexture; // blurred bloom
uniform vec2      u_resolution;
uniform float     u_intensity;    // default 1.0

varying vec2 v_texCoord;

void main() {
    vec4 scene = texture2D(u_texture,      v_texCoord);
    vec4 bloom = texture2D(u_bloomTexture, v_texCoord);

    // Additive blend: bright emissive areas add light on top of the scene.
    // Clamped to [0,1] so we don't exceed LDR range (remove clamp for HDR).
    vec3 result = clamp(scene.rgb + bloom.rgb * u_intensity, 0.0, 1.0);

    gl_FragColor = vec4(result, scene.a);
}
