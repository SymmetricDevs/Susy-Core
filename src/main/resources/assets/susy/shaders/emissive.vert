#version 120

// Transforms geometry with the standard MVP stack.
// Passes the world-space normal and view-space position to the fragment
// shader so it can compute a rim / fresnel falloff.

varying vec2 v_texCoord;
varying vec3 v_normal;      // world-space normal (normalised)
varying vec3 v_viewPos;     // view-space position (for fresnel / rim)

void main() {
    // Standard MVP transform
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Texture coordinate passthrough
    v_texCoord = gl_MultiTexCoord0.xy;

    v_normal  = normalize(gl_NormalMatrix * gl_Normal);

    v_viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
