package me.akhmetov.vxl.api.rendering;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

/**
 * Represents a texture objecct. Obtain one with the file API (todo)
 */
public abstract class Texture implements Disposable {
    public abstract int getWidth();
    public abstract int getHeight();
}
