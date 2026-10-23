#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D OpaqueDepthSampler;
uniform vec2 FovProj;
uniform float LowLightVisionAmount;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    float transparentDepth = texture(MainDepthSampler, texCoord).r;
    float opaqueDepth = texture(OpaqueDepthSampler, texCoord).r;

    float isSky = step(0.999999, opaqueDepth);
    float isTransparent = 1.0 - step(opaqueDepth, transparentDepth);

    // Depth
    float near = 0.05;
    float far = 1024;
    float linearDepth = near * far / (far - opaqueDepth * (far - near));
    float lineartransparentDepth = near * far / (far - transparentDepth * (far - near));

    vec2 viewPlane = FovProj * (texCoord * 2.0 - 1.0);
    float viewPlaneLength = sqrt(1.0 + dot(viewPlane, viewPlane));
    float distance = linearDepth * viewPlaneLength;
    float transparentDist = lineartransparentDepth * viewPlaneLength;
    float transparentDistDelta = distance - transparentDist;
    float transparentColorBlend = smoothstep(0.0, 3.0, transparentDistDelta);

    // Range
    float rangeMin = 1.5;
    float rangeMax = 14.0;
    float nearby = pow(1.0 - smoothstep(rangeMin, rangeMax, distance), 4.0);
    float extendedRange = pow(1.0 - smoothstep(rangeMin, 32.0, distance), 4.0);
    float nearbyTransparent = pow(1.0 - smoothstep(rangeMin, rangeMax, transparentDist), 4.0);

    // Luma
    float luminance = dot(color, vec3(0.3333, 0.3333, 0.3333));
    float percievedLuminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float luminanceMin = 0.01;
    float luminanceMax = luminanceMin * 6.0;
    float preserve = max(pow(smoothstep(luminanceMin, luminanceMax, luminance), 2.0), isSky);
    float desaturate = 1.0 - pow(smoothstep(0.0, luminanceMax, luminance), 4.0);
    vec3 black = vec3(0.0);

    // Low light vision
    vec3 lowLightVision = mix(color * 3.0, vec3(percievedLuminance) * vec3(0.8, 0.8, 1.0) * 4.0, desaturate * 0.75);

    // Fade distance to black
    lowLightVision = mix(black, lowLightVision, nearby);

    // Preserve distant lights
    lowLightVision = mix(lowLightVision, color, preserve * (1.0 - nearby));

    // Preserve sky
    lowLightVision = mix(lowLightVision, color, isSky);

    // Mix in transparent surfaces
    vec3 transparentColor = lowLightVision + mix(black, vec3(0.03, 0.03, 0.04), nearbyTransparent);
    lowLightVision = mix(lowLightVision, transparentColor, transparentColorBlend);

    // Apply darkening to normal vision
    vec3 normalVision = mix(color, black, (1.0 - preserve) * (1.0 - nearby * 0.75));

    // Final fade in of low light vision
    fragColor = vec4(clamp(mix(normalVision, lowLightVision, LowLightVisionAmount), 0.0, 1.0), 1.0);
}