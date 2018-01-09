package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import me.akhmetov.vxl.api.rendering.RenderBucket;

public class ShaderManager {

    private final BlockNodeShader bns;

    public ShaderManager() {
        bns = new RigidBlockNodeShader();
        bns.init();
    }


    public Shader getShader(RenderBucket rb){
        // TODO
        return bns;
    }
    public void setWireframe(boolean val){
        bns.setWireframe(val);
    }

    public void setAllContexts(RenderContext rc) {
        bns.context = rc;
    }

    public void updateAllShaders(PerspectiveCamera cam, double time) {
        bns.update(cam, time);
    }

    public void disposeAllShaders() {
        bns.dispose();
    }
}
