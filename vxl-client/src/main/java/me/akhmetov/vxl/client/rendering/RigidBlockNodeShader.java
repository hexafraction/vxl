package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class RigidBlockNodeShader extends BlockNodeShader {
    private static Logger logger = LogManager.getLogger();

    @Override
    public void init() {
        buildProg("rigid_vtx.glsl", "rigid_frag.glsl");
        super.init();
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
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        prog.setUniformi(u_texture, texUnit);
        renderable.meshPart.render(prog, true);
    }

    @Override
    public void render(MeshPart part, Matrix4 worldTransform) {
        prog.setUniformMatrix(u_worldTrans, worldTransform);
        if(wires) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE );
        else GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);

        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        prog.setUniformi(u_texture, texUnit);
        part.mesh.setAutoBind(true);

        //part.mesh.bind(prog);
        part.render(prog);
        //part.mesh.unbind(prog);
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
