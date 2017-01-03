package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class WavyBlockNodeShader extends BlockNodeShader {
    int u_time;

    @Override
    public void init() {
        buildProg("wavy_vtx.glsl", "wavy_frag.glsl");
        super.init();

        u_time = prog.getUniformLocation("u_time");
    }

    public void bindTexture(Texture tex){
        texUnit = context.textureBinder.bind(tex);
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return false;
    }

    @Override
    public void render(Renderable renderable) {
        prog.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        if(wires) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE );
        else GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        renderable.meshPart.render(prog);
        //prog.setUniformi(u_texture, new Texture("").bind(););
    }

    @Override
    public void end() {
        prog.end();
    }

    @Override
    public void dispose() {
        prog.dispose();
    }

    @Override
    public void update(Camera cam, double time) {
        prog.setUniformMatrix(u_projViewTrans, cam.combined);
    }
}
