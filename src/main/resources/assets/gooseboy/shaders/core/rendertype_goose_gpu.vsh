#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;

out vec2 texCoord0;
out vec4 debugClipPos;
out vec3 debugWorldPos;

void main() {
    vec3 pos = Position + ModelOffset;
    debugWorldPos = pos;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    debugClipPos = gl_Position;
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
}