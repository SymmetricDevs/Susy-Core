#version 120

uniform sampler2D u_holeMask;
uniform sampler2D u_holeTex;
uniform vec2 u_resolution;
uniform int u_holeCount;
uniform vec2 u_holes[16];

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    float aspect = u_resolution.x / u_resolution.y;
    vec2 centered = uv - 0.5;
    centered.x *= aspect;
    float r = length(centered);

    vec3 visorColor = vec3(0.55, 0.35, 0.12);
    float strength = smoothstep(0.0, 1.0, r * 1.5) * 0.6;
    vec3 color = mix(vec3(1.0), visorColor, strength);

    for (int i = 0; i < 16; i++) {
        if (i >= u_holeCount) break;
        vec2 holeUV = u_holes[i];
        vec2 diff = (uv - holeUV) * vec2(aspect, 1.0);
        float holeDist = length(diff);
        float pixelR = max(24.0, 150.0 - float(i) * 10.0);
        float uvR = pixelR / u_resolution.x * aspect;

        float edge = smoothstep(uvR, uvR * 0.9, holeDist);
        if (edge > 0.0) {
            vec2 holeLocal = diff / (uvR * 2.0) + 0.5;
            float mask = texture2D(u_holeMask, holeLocal).r;
            color = mix(color, vec3(1.0), mask * edge);
        }
    }
    //not a big fan of how this looks, if anyone smarter knows how to make this better, please do
    for (int i = 0; i < 16; i++) {
        if (i >= u_holeCount) break;
        vec2 holeUV = u_holes[i];
        vec2 diff = (uv - holeUV) * vec2(aspect, 1.0);
        float holeDist = length(diff);
        float pixelR = max(24.0, 150.0 - float(i) * 10.0);
        float uvR = pixelR / u_resolution.x * aspect;

        float edge = smoothstep(uvR, uvR * 0.9, holeDist);
        if (edge > 0.0) {
            vec2 holeLocal = diff / (uvR * 2.0) + 0.5;
            vec4 texel = texture2D(u_holeTex, holeLocal);
            color = mix(color, texel.rgb, texel.a * edge);
        }
    }

    gl_FragColor = vec4(color, 1.0);
}
