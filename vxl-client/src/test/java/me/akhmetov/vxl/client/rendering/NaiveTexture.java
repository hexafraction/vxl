package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import me.akhmetov.vxl.api.rendering.Texture;

public class NaiveTexture extends Texture {

    private Pixmap px;
    private String name;
    public NaiveTexture(String name) {
        super(name);
        Pixmap px = new Pixmap(Gdx.files.internal(name));
        this.name = name;
        this.px = px;

    }


    @Override
    public Pixmap getPixmap() {
        return px;
    }

    @Override
    public void dispose() {
        px.dispose();
    }
}
