package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

public class PackingWrapper implements Disposable {
    PixmapPacker delegate;
    TextureAtlas atl;

    public PackingWrapper(PixmapPacker delegate) {
        this.delegate = delegate;
        atl = delegate.generateTextureAtlas(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest, true);
    }

    public void pack(String name, Pixmap px) {
        delegate.pack(name, px);
        delegate.updateTextureAtlas(atl, Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest, true);
    }

    public void dispose() {
        delegate.dispose();
        atl.dispose();
    }
}
