#version 150
attribute vec3 a_pos;
attribute vec2 a_texCoord0;
attribute float a_vtx_lighting;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
out vec2 v_texCoords;
void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_pos, 1.0);
    v_texCoords = a_texCoord0;
}
