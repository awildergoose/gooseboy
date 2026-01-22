#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;

out vec2 texCoord0;

//void main() {
//    vec3 pos = Position + ModelOffset;
//    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

//    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
//}
//void main() {
//    vec2 quadPos = UV0 * 2.0 - 1.0;
//    quadPos *= 0.2;
//    vec2 screenPos = vec2(0.0, 0.0);

//    gl_Position = vec4(quadPos + screenPos, -0.9, 1.0);
//    texCoord0 = UV0;
//}
void main() {
    vec3 pos = Position + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
}
