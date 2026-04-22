#version 120

/* reentry_plasma.frag
 *
 * Renders an animated plasma/heat-shield glow that wraps around the bottom half
 * of the screen, intensifying as the drop pod descends through the atmosphere.
 *
 * Uniforms
 *   u_intensity  – overall brightness   [0, 1]
 *   u_time       – seconds since start  (for animation)
 *   u_descent    – descent progress     [0, 1]  (0 = orbit burn, 1 = near ground)
 */

uniform float u_intensity;
uniform float u_time;
uniform float u_descent;

varying vec2 v_uv;

/* ---- Noise helpers ---- */
float hash(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float smoothNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i),             hash(i + vec2(1,0)), u.x),
        mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), u.x),
        u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * smoothNoise(p);
        p  = p * 2.3 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

void main() {
    /* Screen UV: (0,0) = bottom-left, (1,1) = top-right */
    vec2 uv = v_uv;

    /* Plasma is concentrated around the screen edges and bottom
       (simulating the heat shield glowing on the pod's belly). */
    float edgeDist = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    /* Normalised edge factor: 1 at the very edge, 0 at centre */
    float edge = 1.0 - smoothstep(0.0, 0.25, edgeDist);

    /* Additional glow concentrated on the bottom third of screen */
    float bottomGlow = 1.0 - smoothstep(0.0, 0.4, uv.y);

    /* Animated plasma noise */
    vec2 noiseUV = uv * 3.0 + vec2(u_time * 0.3, -u_time * 0.5);
    float noise  = fbm(noiseUV);

    /* Combine */
    float plasma = (edge * 0.6 + bottomGlow * 0.4) * noise;

    /* Colour: orange-white core fading to deep red */
    vec3 hotColour  = vec3(1.0,  0.85, 0.4);
    vec3 coolColour = vec3(0.9,  0.2,  0.0);
    vec3 colour = mix(coolColour, hotColour, noise);

    /* During deep descent the plasma turns brighter / whiter */
    colour = mix(colour, vec3(1.0, 0.95, 0.8), u_descent * 0.4);

    float alpha = plasma * u_intensity;
    /* Clamp so it never fully blacks out the scene */
    alpha = clamp(alpha, 0.0, 0.75);

    gl_FragColor = vec4(colour, alpha);
}
