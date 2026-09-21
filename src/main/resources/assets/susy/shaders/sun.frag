// Thank you blackrack, your scatterer project was very useful to me.
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

void main() {
    vec3 rayDir = normalize(v_rayDir);
    vec3 sunDir = normalize(u_sunDir);

    float angle = acos(clamp(dot(rayDir,sunDir),-1.0,1.0));
    float diskR = u_angularRadius;

    float edgeFade = 1.0 - smoothstep(diskR * 4.5, diskR * 6.5, angle);
    if (angle > diskR * 7.0) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec3 color = vec3(0.0);

    float disk     = smoothstep(diskR*1.05, diskR*0.90, angle);
    float limbEdge = 1.0 - clamp(angle/diskR, 0.0, 1.0);
    float limb     = mix(1.0, pow(limbEdge,0.6), u_limbDarkening);
    float normAngle = angle/diskR;
    float corona = exp(-normAngle*4.0)*0.5 + exp(-normAngle*1.5)*0.15;
    color += u_sunColor * (disk*limb*4.0 + corona) * u_diskIntensity * 0.5;

    float dither = hash21(gl_FragCoord.xy) * 2.0 - 1.0;
    color += dither * 0.003;
    color *= edgeFade;
    FragColor = vec4(max(color,vec3(0.0)), 1.0);
}
