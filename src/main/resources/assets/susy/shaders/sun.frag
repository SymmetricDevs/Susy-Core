#version 330 core

// ───────── Uniforms – names must match SuSySpaceRenderer setUniform* calls ─────────
uniform vec3  u_sunDir;
uniform float u_angularRadius;
uniform vec3  u_sunColor;
uniform float u_diskIntensity;
uniform float u_coronaScale;
uniform float u_time;           // gameTime, ticks→seconds
uniform float u_limbDarkening;

// Matrices are used in the vertex shader; declared here so the linker is happy.
uniform mat4  u_view;
uniform mat4  u_projection;

in  vec3 v_rayDir;
out vec4 FragColor;

// ───────── Constants ─────────
#define PI 3.14159265359


// ───────── Noise ─────────

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float noise2(vec2 t) {
    vec2 i = floor(t);
    vec2 f = fract(t);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    vec2 u = smoothstep(0.0, 1.0, f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}


// ───────── Diffraction spike ─────────

float taperedSpike(vec2 pos, vec2 axis, float spikeLen, float baseW) {
    float along = dot(pos, axis);
    if (along <= 0.0) return 0.0;

    float perp  = abs(dot(pos, vec2(-axis.y, axis.x)));
    float t     = clamp(along / spikeLen, 0.0, 1.0);
    float width = baseW * (1.0 - t);

    if (width < 1e-5) return 0.0;

    float inSpike = smoothstep(width, 0.0, perp);
    float falloff = pow(1.0 - t, 1.6);

    return inSpike * falloff;
}


// ───────── Main ─────────

void main() {
    vec3 rayDir = normalize(v_rayDir);
    vec3 sunDir = normalize(u_sunDir);

    float angle = acos(clamp(dot(rayDir, sunDir), -1.0, 1.0));

    float diskR    = u_angularRadius;
    float maxReach = diskR * 60.0 * u_coronaScale;

    if (angle > maxReach) {
        discard;
    }

    // Build a 2-D tangent-plane coordinate system centred on the sun.
    // Guard against sunDir == (0,1,0) by using a fallback up vector.
    vec3 up     = abs(sunDir.y) < 0.999 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 basisX = normalize(cross(sunDir, up));
    vec3 basisY = cross(basisX, sunDir);

    vec2 uv2d = vec2(
        dot(rayDir - sunDir, basisX),
        dot(rayDir - sunDir, basisY)
    );
    float r2d = length(uv2d);

    // Screen-space UV (centred, aspect-corrected) used for lens effects.
    // Derived from the projected sun position rather than gl_FragCoord so
    // the flare follows the sun regardless of resolution.
    vec2 flareUV = uv2d;
    vec2 sunPos  = vec2(0.0); // sun is always the origin of uv2d

    vec3 color = vec3(0.0);


    // ───────── Solar disk + limb darkening ─────────

    float disk      = smoothstep(diskR * 1.05, diskR * 0.90, angle);
    float limbEdge  = 1.0 - clamp(angle / diskR, 0.0, 1.0);
    float limb      = mix(1.0, pow(limbEdge, 0.6), u_limbDarkening);

    float normAngle = angle / diskR;
    float g1 = exp(-normAngle * 3.5) * 1.4;
    float g2 = exp(-normAngle * 1.8) * 0.55;
    float g3 = exp(-normAngle * 0.55 * u_coronaScale) * 0.18;

    float glow = disk * limb * 4.0 + g1 + g2 + g3;

    color += u_sunColor * glow * u_diskIntensity * 0.5;


    // ───────── Diffraction spikes ─────────

    float spikeLen  = diskR * 38.0 * u_coronaScale;
    float baseWidth = diskR * 0.9;
    float spikes    = 0.0;

    for (int i = 0; i < 3; i++) {
        float a   = float(i) * PI / 3.0;
        vec2  dir = vec2(cos(a), sin(a));

        spikes += taperedSpike( uv2d,  dir, spikeLen, baseWidth);
        spikes += taperedSpike(-uv2d,  dir, spikeLen, baseWidth);
    }

    float shimmer   = 0.92 + 0.08 * sin(u_time * 1.5 + r2d * 12.0);
    float tipFrac   = clamp(r2d / (spikeLen * 0.4), 0.0, 1.0);
    vec3  spikeTint = mix(u_sunColor * 1.2, vec3(0.75, 0.88, 1.0), tipFrac);

    color += spikeTint * spikes * u_diskIntensity * 0.35 * shimmer;


    // ───────── Sensor bloom ─────────

    float bloom = exp(-length(flareUV - sunPos) * 10.0);
    color += u_sunColor * bloom * 0.35;


    // ───────── Lens ghosts ─────────

    vec2 ghost1 = sunPos * -0.6;
    vec2 ghost2 = sunPos * -1.2;
    vec2 ghost3 = sunPos * -1.8;

    float gA = exp(-pow(length(flareUV - ghost1) / 0.04, 2.0));
    float gB = exp(-pow(length(flareUV - ghost2) / 0.03, 2.0));
    float gC = exp(-pow(length(flareUV - ghost3) / 0.05, 2.0));

    color += vec3(0.8, 0.9, 1.0) * gA * 0.35;
    color += vec3(0.7, 0.8, 1.0) * gB * 0.25;
    color += vec3(1.0, 0.8, 0.6) * gC * 0.20;


    // ───────── Film grain ─────────

    float grain = noise2(vec2(r2d * 1920.0 + u_time)) * 0.015;
    color -= vec3(grain);


    // ───────── Tonemapping ─────────
    // NOTE: the renderer uses additive blending (GL_ONE / GL_ONE), so we skip
    // the final gamma curve and just clamp – tonemapping is done by the
    // accumulated framebuffer. Remove the comment if you switch to alpha blend.

    color = max(color, vec3(0.0));
    // color = 1.0 - exp(-color * 0.25);
    // color = pow(color, vec3(1.0 / 2.2));

    FragColor = vec4(color, 1.0);
}
