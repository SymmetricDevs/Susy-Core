#version 120


attribute vec4 Position;
attribute vec2 UV0;

varying vec2 v_texCoord;

void main() {
    gl_Position = vec4(Position.xy, 0.0, 1.0);
    v_texCoord  = UV0;
}
