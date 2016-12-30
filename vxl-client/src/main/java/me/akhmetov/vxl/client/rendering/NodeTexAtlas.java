package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import me.akhmetov.vxl.api.rendering.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.IdentityHashMap;

public class NodeTexAtlas {
    private PixmapPacker packer;
    private TextureAtlas gdxAtlas;

    public TextureAtlas getGdxAtlas() {
        return gdxAtlas;
    }

    private IdentityHashMap<Texture, Tile> texToTiles = new IdentityHashMap<>();
    private static Logger logger = LogManager.getLogger();
    public NodeTexAtlas(int width, int height){
        if(!MathUtils.isPowerOfTwo(width)) {
            logger.warn("Atlas width of "+width+" is not power-of-two.");
        }
        if(!MathUtils.isPowerOfTwo(height)) {
            logger.warn("Atlas height of "+height+" is not power-of-two.");
        }
        packer = new PixmapPacker(width, height, Pixmap.Format.RGBA8888, 0, false);

    }

    public synchronized Tile getTile(Texture tex){
        return texToTiles.get(tex);
    }

    public synchronized void packTex(Texture tex){
        packer.pack(tex.getId(), tex.getPixmap());
        if(gdxAtlas==null) genAtlas();
        else updateAtlas();
        Tile texTile = new Tile(packer.getPage(tex.getId()), gdxAtlas.findRegion(tex.getId()));
        texToTiles.put(tex, texTile);
    }

    public synchronized void packBatch(Texture... texes){
        for(Texture tex : texes){
            packer.pack(tex.getId(), tex.getPixmap());
        }
        if(gdxAtlas==null) genAtlas();
        else updateAtlas();
        for(Texture tex : texes){
            Tile texTile = new Tile(packer.getPage(tex.getId()), gdxAtlas.findRegion(tex.getId()));
            texToTiles.put(tex, texTile);
        }

    }

    private void genAtlas() {
        gdxAtlas = packer.generateTextureAtlas(com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapNearestLinear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
                true);
    }
    private void updateAtlas() {
        packer.updateTextureAtlas(gdxAtlas, com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapNearestLinear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
                true);
    }


    public class Tile {
        public Tile(PixmapPacker.Page page, TextureAtlas.AtlasRegion region) {
            this.page = page;
            this.region = region;
        }

        PixmapPacker.Page page;
        TextureAtlas.AtlasRegion region;
    }
}
