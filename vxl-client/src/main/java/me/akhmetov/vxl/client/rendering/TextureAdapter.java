package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import me.akhmetov.vxl.api.rendering.Texture;

public abstract class TextureAdapter extends Texture {

    abstract TextureRegion getTexRegion();
}
