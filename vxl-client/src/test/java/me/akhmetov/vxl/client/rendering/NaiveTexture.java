package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import me.akhmetov.vxl.api.rendering.Texture;

public class NaiveTexture extends TextureAdapter {
    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
    private PackingWrapper packer;
    private Pixmap px;
    private String name;
    public NaiveTexture(String name, PackingWrapper packer) {
        Pixmap px = new Pixmap(Gdx.files.internal(name));
        packer.pack(name, px);
        this.packer = packer;
        this.name = name;
        this.px = px;

    }

    @Override
    public void dispose() {
        px.dispose();
    }
    volatile TextureRegion cached = null;
    @Override
    TextureRegion getTexRegion() {
        if(cached==null){
            synchronized(this){
                if(cached==null){
                    cached = packer.atl.findRegion(name);
                }
            }
        }
        return cached;
    }
}
