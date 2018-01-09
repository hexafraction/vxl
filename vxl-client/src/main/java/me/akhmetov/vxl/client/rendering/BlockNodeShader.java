package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class BlockNodeShader implements Shader {

    private static Logger logger = LogManager.getLogger();
    protected ShaderProgram prog;
    Camera cam;
    RenderContext context;
    int u_projViewTrans;
    int u_worldTrans;
    int u_texture;
    int texUnit;
    boolean wires = false;
    public void init(){
        u_projViewTrans = prog.getUniformLocation("u_projViewTrans");
        u_worldTrans = prog.getUniformLocation("u_worldTrans");
        u_texture = prog.getUniformLocation("u_texture");
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

    public abstract void render(MeshPart part, Matrix4 worldTransform);

    abstract void update(Camera cam, double time);

    protected void buildProg(String vertPath, String fragPath) {
        String vert = Gdx.files.internal(vertPath).readString();
        String frag = Gdx.files.internal(fragPath).readString();
        prog = new ShaderProgram(vert, frag);
        if (!prog.isCompiled()) {
            logger.fatal("Failed to compile shader with the following errors:\n" + prog.getLog());
            throw new GdxRuntimeException("Failed to compile shader with log: "+prog.getLog());
        }
    }

}
