#version 120

// Simple passthrough – samples the bound texture and outputs it unchanged.
// Useful as a blit / copy step between FBOs.

uniform sampler2D u_texture;
uniform vec2      u_resolution; // (width, height) in pixels – available in all post shaders

varying vec2 v_texCoord;

void main() {
    gl_FragColor = texture2D(u_texture, v_texCoord);
}
