#version 330 core

layout(location = 0) in vec2 aPos;

uniform mat4 u_view;
uniform mat4 u_projection;

// Per-fragment view ray in world space, interpolated across the quad.
out vec3 v_rayDir;

void main() {
    gl_Position = vec4(aPos, 0.0, 1.0);

    // Reconstruct the world-space ray for this NDC position.
    // Undo projection to get a view-space direction, then undo the
    // rotation part of the view matrix (no translation – sky is at infinity).
    vec4 clipPos  = vec4(aPos, 1.0, 1.0);
    vec4 viewPos  = inverse(u_projection) * clipPos;
    viewPos = vec4(viewPos.xy, -1.0, 0.0); // direction, not position

    vec3 worldDir = (inverse(u_view) * viewPos).xyz;
    v_rayDir = normalize(worldDir);
}
