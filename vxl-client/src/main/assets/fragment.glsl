#version 150
#ifdef GL_ES
    precision mediump float;
#endif
in vec2 v_texCoords;
out vec4 color;
uniform sampler2D u_texture;
uniform mat4 u_projViewTrans;

void main() {
     vec4 tex = texture(u_texture, v_texCoords);
     if(tex.a <= 0.5) {discard; }
     else {
        tex.a = 1;
        color = tex;
     }
}