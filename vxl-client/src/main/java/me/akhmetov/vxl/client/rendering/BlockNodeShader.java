package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class BlockNodeShader implements Shader {
    ShaderProgram prog;
    Camera cam;
    RenderContext context;
    int u_projViewTrans;
    int u_worldTrans;
    int u_texture;
    int texUnit;
    private static Logger logger = LogManager.getLogger();

    @Override
    public void init() {
        String vert = Gdx.files.internal("vertex.glsl").readString();
        String frag = Gdx.files.internal("fragment.glsl").readString();
        prog = new ShaderProgram(vert, frag);
        if (!prog.isCompiled()) {
            logger.fatal("Failed to compile shader with the following errors:\n" + prog.getLog());
            throw new GdxRuntimeException("Failed to compile shader with log: "+prog.getLog());
        }
        u_projViewTrans = prog.getUniformLocation("u_projViewTrans");
        u_worldTrans = prog.getUniformLocation("u_worldTrans");
        u_texture = prog.getUniformLocation("u_texture");
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
    public void begin(Camera camera, RenderContext context) {
        this.cam = camera;
        this.context = context;
        prog.begin();
        prog.setUniformMatrix(u_projViewTrans, camera.combined);

        prog.setUniformi(u_texture, texUnit);
        context.setDepthTest(GL20.GL_LESS);
        context.setCullFace(GL20.GL_BACK);
        context.setDepthMask(true);
        context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    }

    @Override
    public void render(Renderable renderable) {
        prog.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        if(wires) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE );
        else GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        renderable.meshPart.render(prog);
        //prog.setUniformi(u_texture, new Texture("").bind(););
    }
    boolean wires = false;
    void setWireframe(boolean val){
        if(val){
            wires = true;
            context.setDepthTest(GL20.GL_ALWAYS);
            context.setCullFace(GL20.GL_NONE);
        } else {
            wires = false;
            context.setDepthTest(GL20.GL_LESS);
            context.setCullFace(GL20.GL_BACK);
        }
    }

    @Override
    public void end() {
        prog.end();
    }

    @Override
    public void dispose() {
        prog.dispose();
    }

    public void update(Camera cam) {
        prog.setUniformMatrix(u_projViewTrans, cam.combined);
    }
}
