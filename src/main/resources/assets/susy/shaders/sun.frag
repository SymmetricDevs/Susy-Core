#version 330 core

uniform vec3  u_sunDir;
uniform float u_angularRadius;
uniform vec3  u_sunColor;
uniform float u_diskIntensity;
uniform float u_coronaScale;
uniform float u_time;
uniform float u_limbDarkening;
uniform mat4  u_invView;
uniform mat4  u_invProjection;
uniform vec2  u_sunScreenPos;

in  vec3 v_rayDir;
out vec4 FragColor;

#define PI 3.14159265359

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx)*0.1031);
    p3 += dot(p3,p3.yzx+33.33);
    return fract((p3.x+p3.y)*p3.z);
}

float noise2(vec2 t) {
    vec2 i=floor(t); vec2 f=fract(t);
    float a=hash21(i), b=hash21(i+vec2(1,0)),
          c=hash21(i+vec2(0,1)), d=hash21(i+vec2(1,1));
    vec2 u=smoothstep(0.,1.,f);
    return mix(mix(a,b,u.x),mix(c,d,u.x),u.y);
}

void main() {
    vec3 rayDir = normalize(v_rayDir);
    vec3 sunDir = normalize(u_sunDir);

    float angle = acos(clamp(dot(rayDir,sunDir),-1.0,1.0));
    float diskR = u_angularRadius;
    float maxReach = diskR * 80.0; // wide enough for full corona falloff
    if(angle > maxReach) discard;
    // Fade out at edges so no hard seam
    float edgeFade = 1.0 - smoothstep(maxReach * 0.7, maxReach, angle);

    vec3 up     = abs(sunDir.y)<0.999 ? vec3(0,1,0) : vec3(1,0,0);
    vec3 basisX = normalize(cross(sunDir,up));
    vec3 basisY = cross(basisX,sunDir);
    vec2 uv2d   = vec2(dot(rayDir-sunDir,basisX), dot(rayDir-sunDir,basisY));
    float r2d   = length(uv2d);

    vec4 viewDir = u_invProjection * vec4(uv2d * 2.0, -1.0, 1.0);
    vec2 fragNDC = uv2d; // approximation good enough for ghost placement

    vec3 color = vec3(0.0);

    // ── Solar disk + limb darkening ──────────────────────────────────────
    float disk     = smoothstep(diskR*1.05, diskR*0.90, angle);
    float limbEdge = 1.0 - clamp(angle/diskR, 0.0, 1.0);
    float limb     = mix(1.0, pow(limbEdge,0.6), u_limbDarkening);
    float normAngle = angle/diskR;
    float g1 = exp(-normAngle*3.5)*1.4;
    float g2 = exp(-normAngle*1.8)*0.55;
    float g3 = exp(-normAngle*0.3)*0.4; // wide corona halo
    color += u_sunColor * (disk*limb*4.0 + g1 + g2 + g3) * u_diskIntensity * 0.5;


    color += u_sunColor * exp(-r2d * 3.0) * 0.5; // wider bloom

    float ndcScale = 2.0; // uv2d to NDC approximate scale
    vec2 fragScreen = u_sunScreenPos + uv2d * ndcScale;

    // Only show ghosts when sun is off-centre (otherwise they pile on the sun)
    float sunOffCentre = length(u_sunScreenPos);
    if(sunOffCentre > 0.05) {
        vec2 ghost1 = -u_sunScreenPos * 0.6;
        vec2 ghost2 = -u_sunScreenPos * 1.2;
        vec2 ghost3 = -u_sunScreenPos * 1.8;
        color += vec3(0.8,0.9,1.0) * exp(-pow(length(fragScreen-ghost1)/0.04,2.0)) * 0.35;
        color += vec3(0.7,0.8,1.0) * exp(-pow(length(fragScreen-ghost2)/0.03,2.0)) * 0.25;
        color += vec3(1.0,0.8,0.6) * exp(-pow(length(fragScreen-ghost3)/0.05,2.0)) * 0.20;
    }

    color -= vec3(noise2(vec2(r2d*1920.0+u_time))*0.015);

    color *= edgeFade;
    FragColor = vec4(max(color,vec3(0.0)), 1.0);
}
