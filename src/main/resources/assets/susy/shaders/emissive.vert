#version 120

// ── Emissive mesh vertex shader ──────────────────────────────────────────────
//
// Transforms geometry with the standard MVP stack.
// Passes the world-space normal and view-space position to the fragment
// shader so it can compute a rim / fresnel falloff.
// ────────────────────────────────────────────────────────────────────────────

varying vec2 v_texCoord;
varying vec3 v_normal;      // world-space normal (normalised)
varying vec3 v_viewPos;     // view-space position (for fresnel / rim)

void main() {
    // Standard MVP transform
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Texture coordinate passthrough
    v_texCoord = gl_MultiTexCoord0.xy;

    // World-space normal (use the normal matrix to handle non-uniform scale)
    v_normal  = normalize(gl_NormalMatrix * gl_Normal);

    // View-space position for per-fragment lighting / fresnel
    v_viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
