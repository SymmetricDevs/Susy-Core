#version 330 core

in vec3 v_rayDir;
out vec4 FragColor;

uniform vec3  u_hostCenter;
uniform float u_hostRadius;
uniform vec3  u_ringNormal;
uniform float u_ringInner;
uniform float u_ringOuter;
uniform float u_ringThickness;
uniform vec3  u_ringColor;
uniform vec3  u_sunDir;
uniform vec3  u_sunColor;
uniform float u_fromSurface;

vec2 raySphere(vec3 ro, vec3 rd, vec3 c, float r) {
    vec3 f = ro - c;
    float b = dot(rd, f);
    float d = b * b - dot(f, f) + r * r;
    if (d < 0.0) return vec2(1e20, -1e20);
    float s = sqrt(d);
    return vec2(-b - s, -b + s);
}

void main() {
    vec3 ro = vec3(0.0);
    vec3 rd = normalize(v_rayDir);

    vec3 hostCenter = u_hostCenter;
    float hostRadius = u_hostRadius;
    vec3 normal = normalize(u_ringNormal);
    float halfThick = 0.5 * u_ringThickness;

    vec3 m = ro - hostCenter;
    float mDotN = dot(m, normal);
    float rdDotN = dot(rd, normal);
    vec3 mPerp = m - normal * mDotN;
    vec3 rdPerp = rd - normal * rdDotN;
    float a = dot(rdPerp, rdPerp);
    float b = 2.0 * dot(mPerp, rdPerp);

    float tSlab0 = -1e20;
    float tSlab1 = 1e20;
    if (abs(rdDotN) > 1e-7) {
        float ta = (-halfThick - mDotN) / rdDotN;
        float tb = (halfThick - mDotN) / rdDotN;
        tSlab0 = min(ta, tb);
        tSlab1 = max(ta, tb);
    } else if (abs(mDotN) > halfThick) {
        discard;
    }

    float tOut0 = -1e20;
    float tOut1 = 1e20;
    float cOut = dot(mPerp, mPerp) - u_ringOuter * u_ringOuter;
    if (a > 1e-12) {
        float disc = b * b - 4.0 * a * cOut;
        if (disc < 0.0) discard;
        float s = sqrt(disc);
        tOut0 = (-b - s) / (2.0 * a);
        tOut1 = (-b + s) / (2.0 * a);
    } else if (cOut > 0.0) {
        discard;
    }

    float t0 = max(tSlab0, tOut0);
    float t1 = min(tSlab1, tOut1);
    if (t0 >= t1) discard;

    float cIn = dot(mPerp, mPerp) - u_ringInner * u_ringInner;
    float tNear = t0;
    float tFar = t1;
    bool hasGap = false;
    float g0 = t0;
    float g1 = t1;
    if (a > 1e-12) {
        float discIn = b * b - 4.0 * a * cIn;
        if (discIn >= 0.0) {
            float sIn = sqrt(discIn);
            g0 = max((-b - sIn) / (2.0 * a), t0);
            g1 = min((-b + sIn) / (2.0 * a), t1);
            hasGap = (g1 > g0);
        }
    } else if (cIn < 0.0) {
        discard;
    }

    const float EPS = 1e-4;
    if (hasGap) {
        bool visFront = (g0 > t0) && (g0 > EPS);
        bool visBack = (t1 > g1) && (t1 > EPS);
        if (visFront) {
            tNear = t0;
            tFar = g0;
        } else if (visBack) {
            tNear = g1;
            tFar = t1;
        } else {
            discard;
        }
    }

    if (tFar <= EPS) discard;
    float tNearClamped = max(tNear, 0.0);
    float tSample = (tNearClamped + tFar) * 0.5;

    vec3 fromCenter = ro + rd * tSample - hostCenter;
    float yOff = dot(fromCenter, normal);
    float dist = length(fromCenter - normal * yOff);

    vec2 hostHit = raySphere(ro, rd, hostCenter, hostRadius);
    if (hostHit.y > EPS && hostHit.x < tNearClamped) {
        discard;
    }

    float radial = (dist - u_ringInner) / (u_ringOuter - u_ringInner);

    float ringDensity = mix(1.0, 0.4, smoothstep(0.0, 1.0, radial));
    ringDensity *= 0.9 + 0.1 * sin(radial * 20.0);

    vec3 toSun = normalize(u_sunDir);

    vec3 f = fromCenter;
    float sb = dot(toSun, f);
    float sc = dot(f, f) - hostRadius * hostRadius;
    float sdisc = sb * sb - sc;
    float shadow = 1.0;
    if (sdisc >= 0.0) {
        float ss = sqrt(sdisc);
        float tShadow = -sb - ss;
        if (tShadow > EPS) {
            shadow = 0.0;
        }
    }

    float light = shadow;
    float ambient = 0.25;

    vec3 color = u_ringColor * (ambient + (1.0 - ambient) * light) * ringDensity * u_sunColor;

    float edgeFade = smoothstep(u_ringInner, u_ringInner + (u_ringOuter - u_ringInner) * 0.05, dist) *
                     (1.0 - smoothstep(u_ringOuter - (u_ringOuter - u_ringInner) * 0.05, u_ringOuter, dist));

    float pathLen = tFar - tNearClamped;
    float depthScale = pathLen / (2.0 * halfThick);

    FragColor = vec4(color, ringDensity * edgeFade * depthScale);
}
