#version 150
#ifdef GL_ES
    precision mediump float;
#endif
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projViewTrans;

void main() {
     vec4 tex = texture(u_texture, v_texCoords);
     if(tex.a <= 0.5) {discard; }
     else {
        tex.a = 1;
        gl_FragColor = tex;
     }
}