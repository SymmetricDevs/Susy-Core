#version 120

/* reentry_plasma.vert
 * Full-screen triangle-strip vertex shader for the heat-shield plasma overlay.
 * Receives NDC positions from the VBO (-1/-1 to +1/+1 quad).
 */

attribute vec2 a_pos;

varying vec2 v_uv;

void main() {
    v_uv        = a_pos * 0.5 + 0.5;   // remap [-1,1] → [0,1]
    gl_Position = vec4(a_pos, 0.0, 1.0);
}
