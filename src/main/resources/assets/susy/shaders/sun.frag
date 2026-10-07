// Thank you blackrack, your scatterer project was very useful to me.
#version 330 core

uniform vec3  u_sunDir;
uniform float u_angularRadius;
uniform vec3  u_sunColor;
uniform float u_diskIntensity;
uniform float u_coronaScale;
uniform float u_time;
uniform float u_limbDarkening;
uniform float u_detail;
uniform mat4  u_invView;
uniform mat4  u_invProjection;

in  vec3 v_rayDir;
out vec4 FragColor;

#define CORONA_CUTOFF 22.0

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), u.x),
               mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float sum = 0.0;
    float amp = 0.5;
    float norm = 0.0;
    for (int i = 0; i < 3; i++) {
        sum += amp * vnoise(p);
        p = p * 2.03 + vec2(1.7, -0.9);
        amp *= 0.5;
        norm += amp;
    }
    return sum / norm;
}

void main() {
    float R = u_angularRadius;
    if (R <= 0.0) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    float time = mod(u_time, 65536.0);

    vec3 rd = normalize(v_rayDir);
    vec3 sunDir = normalize(u_sunDir);

    vec3 ref = abs(sunDir.y) > 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(0.0, 1.0, 0.0);
    vec3 sunU = normalize(cross(ref, sunDir));
    vec3 sunV = cross(sunDir, sunU);

    vec2 p = vec2(dot(rd, sunU), dot(rd, sunV)) / sin(R);
    float r = length(p);
    if (r > CORONA_CUTOFF) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    float mu = sqrt(max(0.0, 1.0 - r * r));
    float limb = mix(1.0, 0.32 + 0.68 * mu, u_limbDarkening);
    vec3 tint = mix(vec3(1.00, 0.58, 0.24), vec3(1.00, 0.97, 0.92), pow(mu, 0.40));

    float disk = smoothstep(1.0, 0.982, r);

    float gran = fbm(p * 13.0 + vec2(time * 0.015, -time * 0.010));
    gran += u_detail * 0.45 * (fbm(p * 29.0 + vec2(-time * 0.022, time * 0.031)) - 0.5);
    float cells = 1.0 + (gran - 0.5) * 0.70;

    float spotField = fbm(p * 5.0 + vec2(7.3 + time * 0.004, -4.1 - time * 0.002));
    float belt = exp(-(p.y * p.y) / (2.0 * 0.30 * 0.30));
    float penumbra = smoothstep(0.66, 0.75, spotField) * belt;
    float umbra = smoothstep(0.75, 0.82, spotField) * belt;
    float photosphere = disk * limb * cells * (1.0 - 0.40 * penumbra - 0.34 * umbra);

    float rim = smoothstep(1.030, 0.998, r) * smoothstep(0.986, 1.0, r);

    float theta = atan(p.y, p.x);
    float streamers = 0.80
        + 0.055 * sin(theta * 3.0 + 0.7)
        + 0.045 * sin(theta * 5.0 + 2.9)
        + 0.035 * sin(theta * 7.0 + 1.1)
        + 0.025 * sin(theta * 11.0 + 4.4)
        + 0.020 * sin(theta * 17.0 + 0.3);
    streamers *= 0.80 + 0.34 * fbm(vec2(cos(theta), sin(theta)) * 2.6 + vec2(19.0, -5.0));
    float falloff = 1.0 / (0.30 + r * r * (0.35 + 0.55 * r));
    float corona = falloff * streamers * u_coronaScale;

    float glare = 0.012 / (1.0 + 0.12 * r * r);

    vec3 color = tint * u_sunColor * photosphere * u_diskIntensity;
    color += vec3(1.0, 0.26, 0.10) * rim * u_diskIntensity * 0.55;
    color += u_sunColor * (corona * 0.7 + glare) * u_diskIntensity;

    color += (hash21(gl_FragCoord.xy) * 2.0 - 1.0) * 0.004;
    FragColor = vec4(max(color, vec3(0.0)), 1.0);
}
