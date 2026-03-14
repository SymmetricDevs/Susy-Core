#version 330 core

in  vec3 v_rayDir;
out vec4 FragColor;

const float PI = 3.14159265359;

uniform vec3  u_rayleighCoefficients;
uniform float u_rayleighScaleHeight;
uniform vec3  u_mieCoefficients;
uniform float u_mieScaleHeight;
uniform vec3  u_mieAsymmetry;
uniform float u_mieAbsorptionMult;
uniform vec3  u_ozoneCoefficients;
uniform float u_ozoneAltitude;
uniform float u_ozoneExtent;
uniform float u_bottomRadius;
uniform float u_topRadius;

uniform vec3  u_cameraPos;
uniform vec3  u_planetPos;
uniform vec3  u_sunDir;
uniform float u_sunIntensity;
uniform float u_renderUnitRadius;
uniform mat4  u_invProjection;
uniform mat4  u_invView;

uniform sampler2D u_transmittanceLut;
uniform vec2      u_lutSize;

const int V_STEPS = 16;

vec2 raySphere(vec3 ro, vec3 rd, vec3 c, float r) {
    vec3 f=ro-c; float b=dot(rd,f);
    float d=b*b-dot(f,f)+r*r;
    if(d<0.0) return vec2(1e20,-1e20);
    float s=sqrt(d); return vec2(-b-s,-b+s);
}

float RayleighPhase(float c) { return (3.0/(16.0*PI))*(1.0+c*c); }
vec3  MiePhase(float c, vec3 g) {
    vec3 g2=g*g, den=1.0+g2-2.0*g*c;
    return 1.5/(4.0*PI)*(1.0-g2)*(1.0+c*c)*pow(max(den,vec3(1e-4)),vec3(-1.5))/(2.0+g2);
}

vec2 GetTransmittanceLutUV(float altitude, float cosZenith) {
    float maxH    = sqrt(u_topRadius*u_topRadius - u_bottomRadius*u_bottomRadius);
    float radDist = sqrt(max(0.0, altitude*altitude - u_bottomRadius*u_bottomRadius));
    float disc    = altitude*altitude*(cosZenith*cosZenith-1.0) + u_topRadius*u_topRadius;
    float rayDist = max(0.0, -altitude*cosZenith + sqrt(max(disc,0.0)));
    float minD    = u_topRadius - altitude;
    float maxD    = radDist + maxH;
    vec2  coords  = vec2(
        (maxD > minD) ? (rayDist-minD)/(maxD-minD) : 0.0,
        (maxH > 0.0)  ? radDist/maxH : 0.0
    );
    coords = clamp(coords, 0.0, 1.0);
    return (coords*(u_lutSize-1.0) + 0.5) / u_lutSize;
}

vec3 GetTransmittanceFromLut(float altitude, float cosZenith) {
    vec2  uv     = GetTransmittanceLutUV(altitude, cosZenith);
    vec3  T      = textureLod(u_transmittanceLut, uv, 0.0).rgb;
    float sinH   = clamp(u_bottomRadius / altitude, 0.0, 1.0);
    float cosH   = -sqrt(max(1.0-sinH*sinH, 0.0));
    float shadow = smoothstep(cosH - sinH*0.01, cosH + sinH*0.01, cosZenith);
    return T * shadow;
}

void main() {
    vec3 rd = normalize(v_rayDir);

    float ruToM = u_bottomRadius / u_renderUnitRadius;
    float topRU = u_topRadius  / ruToM;
    float botRU = u_bottomRadius / ruToM;

    vec2 aHit = raySphere(u_cameraPos, rd, u_planetPos, topRU);

    // DEBUG: if ray hits atmosphere at all, show red. Remove once working.
    if (aHit.x <= aHit.y && aHit.y > 0.0) {
        // Ray hits atmosphere - continue with full scatter
    } else {
        discard;
    }

    vec2  pH    = raySphere(u_cameraPos, rd, u_planetPos, botRU);
    bool  hitPlt = pH.x > 0.0 && pH.x < pH.y;

    float tStart = max(aHit.x, 0.0);
    float tEnd   = hitPlt ? min(pH.x, aHit.y) : aHit.y;
    if (tStart >= tEnd) {
        // DEBUG: show green if intersection exists but tStart>=tEnd
        FragColor = vec4(0.0, 1.0, 0.0, 0.5);
        return;
    }

    float tStartM = tStart * ruToM;
    float stepM   = (tEnd - tStart) * ruToM / float(V_STEPS);

    float cosL = dot(rd, u_sunDir);
    float pR   = RayleighPhase(cosL);
    vec3  pM   = MiePhase(cosL, u_mieAsymmetry);

    vec3 rayleighAbs = u_rayleighCoefficients;
    vec3 mieAbs      = u_mieCoefficients * u_mieAbsorptionMult;
    vec3 planSI      = u_planetPos * ruToM;

    vec3 totalT = vec3(1.0);
    vec3 accumR = vec3(0.0);
    vec3 accumM = vec3(0.0);

    for (int i = 0; i < V_STEPS; i++) {
        vec3  absSI = u_cameraPos*ruToM + rd*(tStartM + (float(i)+0.5)*stepM);
        vec3  relSI = absSI - planSI;
        float dist  = length(relSI);
        float alt   = max(dist - u_bottomRadius, 0.0);

        vec2  dens = exp(-alt / vec2(u_rayleighScaleHeight, u_mieScaleHeight));
        float dO   = clamp(1.0-abs(alt-u_ozoneAltitude)/u_ozoneExtent, 0.0, 1.0);

        vec3 localAbs = rayleighAbs*dens.x + mieAbs*dens.y + u_ozoneCoefficients*dO;
        vec3 stepT    = exp(-localAbs * stepM);
        vec3 weight   = totalT * (vec3(1.0)-stepT) / max(localAbs, vec3(1e-10));

        float sunCosZ = dot(relSI/dist, u_sunDir);
        vec3  lightT  = GetTransmittanceFromLut(dist, sunCosZ);

        accumR += weight * lightT * dens.x * u_rayleighCoefficients;
        accumM += weight * lightT * dens.y * u_mieCoefficients;
        totalT *= stepT;
    }

    vec3 inscatter = u_sunIntensity * (accumR * pR + accumM * pM);

    // DEBUG: show blue if inscatter is near zero
    if (dot(inscatter, vec3(1.0)) < 1e-6) {
        FragColor = vec4(0.0, 0.0, 1.0, 0.5);
        return;
    }

    float avgT  = dot(totalT, vec3(1.0/3.0));
    float alpha = 1.0 - avgT;
    inscatter = pow(max(1.0 - exp(-inscatter), vec3(0.0)), vec3(1.0/2.2));
    FragColor = vec4(inscatter, alpha);
}
