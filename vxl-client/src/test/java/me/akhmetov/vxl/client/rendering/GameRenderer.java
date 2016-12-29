package me.akhmetov.vxl.client.rendering;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import me.akhmetov.vxl.api.map.BaseMapNode;
import me.akhmetov.vxl.api.map.MapChunk;
import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.rendering.BlockNode;
import me.akhmetov.vxl.api.rendering.NodeAppearance;
import me.akhmetov.vxl.api.rendering.Texture;
import me.akhmetov.vxl.core.GameState;
import me.akhmetov.vxl.core.map.MapChunkImpl;
import me.akhmetov.vxl.core.map.NodeResolutionTable;

public class GameRenderer implements ApplicationListener {

    ActiveArea lam = new NaiveActiveArea();
    MapNode nd1;
    MapNode nd2;
    NodeAppearance ap1;
    NodeAppearance ap2;
    Texture tex1;
    Texture tex2;
    private ModelBatch mb;
    PixmapPacker pp = new PixmapPacker(2048,2048, Pixmap.Format.RGBA8888, 1, true);
    PackingWrapper wrp = new PackingWrapper(pp);

    GameState gs = new GameState(null, null, true);
    NodeResolutionTable nrt = new NodeResolutionTable(gs);

    MapChunk chk = new MapChunkImpl(gs, 0, 0, 0, nrt);
    public GameRenderer() throws Exception {
        tex1 = new NaiveTexture("tex_1.png", wrp);
        tex2 = new NaiveTexture("tex_2.png", wrp);
        ap1 = new BlockNode(tex1, tex1, tex1, tex1, tex1, tex1);
        ap2 = new BlockNode(tex2, tex2, tex2, tex2, tex2, tex2);
        nd1 = new BaseMapNode("test:n1", ap1);
        nd2 = new BaseMapNode("test:n2", ap2);
        nrt.registerMapNode(nd1);
        nrt.registerMapNode(nd2);
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < i; j++){
                for(int k = 0; k < i; k++){
                    chk.setNode(i, j, k, (i%2==0)?nd1:nd2);
                }
            }
        }
        lam.addChunk(chk);
    }
    @Override
    public void create() {

        mb = new ModelBatch();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        tex1.dispose();
        tex2.dispose();
        wrp.dispose();
    }
}
