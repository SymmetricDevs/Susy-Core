#version 330 core

out vec4 FragColor;

uniform vec2  u_lutSize;
uniform float u_bottomRadius;
uniform float u_topRadius;
uniform vec3  u_rayleighCoefficients;
uniform float u_rayleighScaleHeight;
uniform vec3  u_mieCoefficients;
uniform float u_mieScaleHeight;
uniform float u_mieAbsorptionMult;
uniform vec3  u_ozoneCoefficients;
uniform float u_ozoneAltitude;
uniform float u_ozoneExtent;

const int STEPS = 40;

vec2 raySphere(vec3 ro, vec3 rd, vec3 c, float r) {
    vec3 f = ro-c; float b = dot(rd,f);
    float d = b*b-dot(f,f)+r*r;
    if (d < 0.0) return vec2(1e20,-1e20);
    float s = sqrt(d); return vec2(-b-s,-b+s);
}

void main() {
    // gl_FragCoord is (0.5..63.5) for a 64px viewport
    // Map to [0,1] coords matching TransmittanceLutUVToPhysicalParams
    vec2 coords = (gl_FragCoord.xy - 0.5) / (u_lutSize - 1.0);
    coords = clamp(coords, 0.0, 1.0);

    // TransmittanceLutUVToPhysicalParams - exact from AtmosphereLuts.glsl
    float maxH    = sqrt(u_topRadius*u_topRadius - u_bottomRadius*u_bottomRadius);
    float radDist = maxH * coords.y;
    float altitude = sqrt(radDist*radDist + u_bottomRadius*u_bottomRadius);
    float minD    = u_topRadius - altitude;
    float maxD    = radDist + maxH;
    float rayD    = minD + coords.x * (maxD - minD);
    float cosZenith = (rayD == 0.0) ? 1.0
        : clamp((maxH*maxH - radDist*radDist - rayD*rayD) / (2.0*altitude*rayD), -1.0, 1.0);

    float sinZ    = sqrt(max(1.0 - cosZenith*cosZenith, 0.0));
    vec3  viewDir = vec3(sinZ, 0.0, cosZenith);
    vec3  planPos = vec3(0.0, 0.0, -altitude);

    float distToTop = raySphere(vec3(0.0), viewDir, planPos, u_topRadius).y;
    if (distToTop <= 0.0) { FragColor = vec4(1.0, 1.0, 1.0, 1.0); return; }

    float stepSize    = distToTop / float(STEPS);
    vec3  rayleighAbs = u_rayleighCoefficients;
    vec3  mieAbs      = u_mieCoefficients * u_mieAbsorptionMult;
    vec3  T           = vec3(1.0);

    for (int i = 0; i < STEPS; i++) {
        vec3  pos  = viewDir * (float(i)+0.5)*stepSize;
        float dist = length(pos - planPos);
        float alt  = max(dist - u_bottomRadius, 0.0);
        float dR   = exp(-alt / u_rayleighScaleHeight);
        float dM   = exp(-alt / u_mieScaleHeight);
        float dO   = clamp(1.0-abs(alt-u_ozoneAltitude)/u_ozoneExtent, 0.0, 1.0);
        T *= exp(-(rayleighAbs*dR + mieAbs*dM + u_ozoneCoefficients*dO) * stepSize);
    }

    FragColor = vec4(T, 1.0);
}
