#version 330 core

layout(location = 0) in vec2 aPos;

// Pre-inverted on CPU to avoid GLSL inverse() driver issues
uniform mat4 u_invView;
uniform mat4 u_invProjection;

out vec3 v_rayDir;

void main() {
    gl_Position = vec4(aPos, 0.0, 1.0);
    vec4 viewPos = u_invProjection * vec4(aPos, 1.0, 1.0);
    viewPos      = vec4(viewPos.xy, -1.0, 0.0);
    v_rayDir     = normalize((u_invView * viewPos).xyz);
}
