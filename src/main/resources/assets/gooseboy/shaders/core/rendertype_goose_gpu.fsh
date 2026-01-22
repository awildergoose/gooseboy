#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 debugClipPos;
in vec3 debugWorldPos;

out vec4 fragColor;

void main() {
    vec3 worldColor = (debugWorldPos + 16.0) / 32.0;
    fragColor = vec4(worldColor, 1.0);
}