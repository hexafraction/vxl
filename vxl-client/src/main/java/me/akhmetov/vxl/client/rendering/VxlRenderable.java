package me.akhmetov.vxl.client.rendering;


import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Shader;

public interface VxlRenderable {
    public void render();
    public void update(Camera cam);

}
