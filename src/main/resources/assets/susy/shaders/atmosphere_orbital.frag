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

uniform sampler2D u_multipleScatteringLut;
uniform vec2      u_msLutSize;

const int V_STEPS = 20;
const int L_STEPS = 8;

vec2 raySphere(vec3 ro, vec3 rd, vec3 c, float r) {
    vec3 f = ro-c; float b = dot(rd,f);
    float d = b*b - dot(f,f) + r*r;
    if (d < 0.0) return vec2(1e20,-1e20);
    float s = sqrt(d); return vec2(-b-s,-b+s);
}

float RayleighPhase(float c) { return (3.0/(16.0*PI))*(1.0+c*c); }

vec3 MiePhase(float c, vec3 g) {
    vec3 g2 = g*g;
    vec3 denom = 1.0+g2-2.0*g*c;
    return 1.5/(4.0*PI) * (1.0-g2) * (1.0+c*c)
           * pow(max(denom,vec3(1e-4)), vec3(-1.5)) / (2.0+g2);
}

vec3 GetTransmittanceToSun(vec3 posSI, vec3 planSI) {
    vec3  rel    = posSI - planSI;
    float dist   = length(rel);
    vec3  zenith = rel / dist;
    float cosZ   = dot(zenith, u_sunDir);
    float sinH   = u_bottomRadius / dist;
    float cosH   = -sqrt(max(1.0-sinH*sinH, 0.0));
    float softShadow = smoothstep(cosH - sinH*0.01, cosH + sinH*0.01, cosZ);

    vec2  lh    = raySphere(posSI, u_sunDir, planSI, u_topRadius);
    float lLen  = max(lh.y, 0.0);
    float lStep = lLen / float(L_STEPS);
    vec3  T     = vec3(1.0);
    for (int j = 0; j < L_STEPS; j++) {
        vec3  lp  = posSI + u_sunDir*(float(j)+0.5)*lStep;
        float la  = max(length(lp-planSI)-u_bottomRadius, 0.0);
        float dR  = exp(-la/u_rayleighScaleHeight);
        float dM  = exp(-la/u_mieScaleHeight);
        float dO  = clamp(1.0-abs(la-u_ozoneAltitude)/u_ozoneExtent, 0.0, 1.0);
        T *= exp(-(u_rayleighCoefficients*dR + u_mieCoefficients*u_mieAbsorptionMult*dM + u_ozoneCoefficients*dO)*lStep);
    }
    return T * softShadow;
}

vec2 GetMultiScatteringUV(float sunCosZenith, float altitude) {
    vec2 coords = clamp(vec2(
        sunCosZenith * 0.5 + 0.5,
        altitude / (u_topRadius - u_bottomRadius)
    ), 0.0, 1.0);
    return (coords * (u_msLutSize - 1.0) + 0.5) / u_msLutSize;
}

void main() {
    vec3 rd = normalize(v_rayDir);

    float ruToM = u_bottomRadius / u_renderUnitRadius;
    float topRU = u_topRadius / ruToM;
    float botRU = u_bottomRadius / ruToM;

    vec2 aHit = raySphere(u_cameraPos, rd, u_planetPos, topRU);
    if (aHit.x > aHit.y) discard;

    vec2  pH    = raySphere(u_cameraPos, rd, u_planetPos, botRU);
    bool  hitPlt = pH.x > 0.0 && pH.x < pH.y;

    float tStart = max(aHit.x, 0.0);
    float tEnd   = hitPlt ? min(pH.x, aHit.y) : aHit.y;
    if (tStart >= tEnd) discard;

    float tStartM = tStart * ruToM;
    float stepM   = (tEnd - tStart) * ruToM / float(V_STEPS);

    float cosL = dot(rd, u_sunDir);
    float pR   = RayleighPhase(cosL);
    vec3  pM   = MiePhase(cosL, u_mieAsymmetry);

    vec3 rayleighAbs = u_rayleighCoefficients;
    vec3 mieAbs      = u_mieCoefficients * u_mieAbsorptionMult;
    vec3 planSI      = u_planetPos * ruToM;

    vec3 totalT  = vec3(1.0);
    vec3 accumR  = vec3(0.0);
    vec3 accumM  = vec3(0.0);
    vec3 accumMS = vec3(0.0);

    for (int i = 0; i < V_STEPS; i++) {
        vec3  absSI = u_cameraPos*ruToM + rd*(tStartM + (float(i)+0.5)*stepM);
        vec3  relSI = absSI - planSI;
        float dist  = length(relSI);
        float alt   = max(dist - u_bottomRadius, 0.0);

        vec2  dens = exp(-alt / vec2(u_rayleighScaleHeight, u_mieScaleHeight));
        float dO   = clamp(1.0-abs(alt-u_ozoneAltitude)/u_ozoneExtent, 0.0, 1.0);

        vec3 localAbs  = rayleighAbs*dens.x + mieAbs*dens.y + u_ozoneCoefficients*dO;
        vec3 stepT     = exp(-localAbs * stepM);

        vec3 weight = totalT * (vec3(1.0)-stepT) / max(localAbs, vec3(1e-10));

        vec3 lightT = GetTransmittanceToSun(absSI, planSI);

        vec3 curR = dens.x * rayleighAbs;
        vec3 curM = dens.y * u_mieCoefficients;

        accumR += weight * lightT * curR;
        accumM += weight * lightT * curM;

        float sunCosZ = dot(relSI / dist, u_sunDir);
        vec2  msUV    = GetMultiScatteringUV(sunCosZ, alt);
        vec3  msLight = textureLod(u_multipleScatteringLut, msUV, 0.0).rgb;
        accumMS += weight * msLight * (curR + curM);

        totalT *= stepT;
    }

    vec3 inscatter = u_sunIntensity * (accumR * pR + accumM * pM + accumMS);

    float avgT  = dot(totalT, vec3(1.0/3.0));
    float alpha = 1.0 - avgT;

    inscatter = (inscatter * 2.0) / (1.0 + inscatter * 2.0);  // Reinhard with 2x exposure

    FragColor = vec4(inscatter, alpha);
}
