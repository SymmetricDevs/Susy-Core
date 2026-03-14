#version 120

// Full-screen quad vertex shader.
// Accepts NDC positions (-1..1) and passes UVs to the fragment stage.
// All post-process shaders share this single vertex program.

attribute vec4 Position;
attribute vec2 UV0;

varying vec2 v_texCoord;

void main() {
    gl_Position = vec4(Position.xy, 0.0, 1.0);
    v_texCoord  = UV0;
}
