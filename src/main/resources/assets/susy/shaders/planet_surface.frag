#version 330 core

// Planet surface shader - renders the cubemap with a physically correct terminator.
// Used for bodies WITHOUT an atmosphere (moon, mars, etc).
// For bodies WITH an atmosphere, this pass is skipped and the atmosphere
// shader handles terminator darkening itself.
//
// The terminator is a smooth penumbra based on the sun's angular radius,
// matching how real planetary terminators look from orbit.

in  vec3 v_rayDir;
out vec4 FragColor;

// Face order: +X, -X, +Y, -Y, +Z, -Z
uniform sampler2D u_face0; // +X
uniform sampler2D u_face1; // -X
uniform sampler2D u_face2; // +Y
uniform sampler2D u_face3; // -Y
uniform sampler2D u_face4; // +Z
uniform sampler2D u_face5; // -Z

uniform vec3  u_planetPos;       // render units
uniform float u_planetRadius;    // render units
uniform vec3  u_sunDir;          // normalised toward sun
uniform float u_sunAngularRadius; // angular radius of sun (radians, ~0.00935 for Earth)
uniform mat4  u_invView;
uniform mat4  u_invProjection;

// Orbital rotation matrix applied to cubemap lookup
// (accounts for planet's current rotation angle)
uniform mat4  u_planetRotation;

vec2 raySphere(vec3 ro, vec3 rd, vec3 c, float r) {
    vec3 f=ro-c; float b=dot(rd,f);
    float d=b*b-dot(f,f)+r*r;
    if(d<0.0) return vec2(1e20,-1e20);
    float s=sqrt(d); return vec2(-b-s,-b+s);
}

// Convert a direction to a cubemap face UV
// Returns face index (0-5) and UV in [0,1]
int dirToFaceUV(vec3 d, out vec2 uv) {
    vec3 ad = abs(d);
    int face;
    vec2 raw;
    if (ad.x >= ad.y && ad.x >= ad.z) {
        face = d.x > 0.0 ? 0 : 1;
        raw  = d.x > 0.0 ? vec2(-d.z, -d.y) / ad.x : vec2(d.z, -d.y) / ad.x;
    } else if (ad.y >= ad.x && ad.y >= ad.z) {
        face = d.y > 0.0 ? 2 : 3;
        raw  = d.y > 0.0 ? vec2(d.x, d.z) / ad.y : vec2(d.x, -d.z) / ad.y;
    } else {
        face = d.z > 0.0 ? 4 : 5;
        raw  = d.z > 0.0 ? vec2(d.x, -d.y) / ad.z : vec2(-d.x, -d.y) / ad.z;
    }
    uv = raw * 0.5 + 0.5;
    return face;
}

vec4 sampleCubemap(vec3 dir) {
    // Apply planet rotation to the lookup direction
    vec3 d = normalize((u_planetRotation * vec4(dir, 0.0)).xyz);
    vec2 uv;
    int face = dirToFaceUV(d, uv);
    if      (face == 0) return texture(u_face0, uv);
    else if (face == 1) return texture(u_face1, uv);
    else if (face == 2) return texture(u_face2, uv);
    else if (face == 3) return texture(u_face3, uv);
    else if (face == 4) return texture(u_face4, uv);
    else                return texture(u_face5, uv);
}

void main() {
    vec3 rd = normalize(v_rayDir);

    vec2 hit = raySphere(vec3(0.0), rd, u_planetPos, u_planetRadius);
    if (hit.x > hit.y || hit.y < 0.0) discard;

    float t = hit.x > 0.0 ? hit.x : hit.y;
    vec3  hitPos = rd * t;

    // Surface normal in world space
    vec3 normal = normalize(hitPos - u_planetPos);

    // Sample cubemap using surface normal as lookup direction
    vec4 surfaceColor = sampleCubemap(normal);

    // Terminator: smooth penumbra using sun's angular radius
    // cosHorizon = cos of angle between normal and sun at the terminator
    float cosTheta = dot(normal, u_sunDir);

    // Penumbra width based on sun angular radius (umbra to full light)
    // smoothstep from -sunAngularRadius to +sunAngularRadius around terminator
    float light = smoothstep(-u_sunAngularRadius, u_sunAngularRadius, cosTheta);

    // Night side: very dark (ambient ~2% for earthshine/starlight)
    float ambient = 0.02;
    float illumination = ambient + (1.0 - ambient) * light;

    vec3 color = surfaceColor.rgb * illumination;

    // Output fully opaque - this is the base layer under atmosphere
    FragColor = vec4(color, surfaceColor.a);
}
