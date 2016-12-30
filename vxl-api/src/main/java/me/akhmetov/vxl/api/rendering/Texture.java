package me.akhmetov.vxl.api.rendering;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

/**
 * Represents a texture objecct. Obtain one with the file API (todo create implementation classes)
 */
public abstract class Texture implements Disposable {
    public abstract Pixmap getPixmap();

    private final String id;

    public String getId() {
        return id;
    }

    protected Texture(String id) {
        this.id = id;
    }

    ;
}
