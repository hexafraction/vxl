attribute vec3 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
varying vec2 v_texCoords;
void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
    v_texCoords = a_texCoord0;
}
