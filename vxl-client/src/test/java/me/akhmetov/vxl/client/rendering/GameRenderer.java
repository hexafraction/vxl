package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.api.map.BaseMapNode;
import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.rendering.BlockNodeAppearance;
import me.akhmetov.vxl.api.rendering.NodeAppearance;
import me.akhmetov.vxl.api.rendering.Texture;
import me.akhmetov.vxl.core.GameState;
import me.akhmetov.vxl.core.map.HardcodedNodes;
import me.akhmetov.vxl.core.map.LoadedMapChunk;
import me.akhmetov.vxl.core.map.NodeResolutionTable;

public class GameRenderer implements ApplicationListener {

    ActiveArea lam = new NaiveActiveArea();
    MapNode nd1;
    MapNode nd2;
    NodeAppearance ap1;
    NodeAppearance ap2;
    Texture tex1;
    Texture tex2;
    Texture tex3;
    private ModelBatch mb;
    PixmapPacker pp = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 1, true);
    PackingWrapper wrp = new PackingWrapper(pp);

    GameState gs = new GameState(null, null, true);
    NodeResolutionTable nrt = new NodeResolutionTable(gs);

    RenderedChunk chk;
    private NodeTexAtlas atl;

    public GameRenderer() throws Exception {

    }

    private Environment environment;

    public PerspectiveCamera cam;
    public CameraInputController camController;
    float pX, pY, pZ;
    @Override
    public void create() {

        environment = new Environment();
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(1f, 1f, 1f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        tex1 = new NaiveTexture("tex_1.png");
        tex2 = new NaiveTexture("tex_2.png");
        tex3 = new NaiveTexture("tex_3.png");
        atl = new NodeTexAtlas(512, 512);
        ap1 = new BlockNodeAppearance(tex1, tex1, tex3, tex3, tex1, tex1);
        ap2 = new BlockNodeAppearance(tex2, tex2, tex2, tex2, tex2, tex2);
        chk = new RenderedChunk(new LoadedMapChunk(gs, 0, 0, 0, nrt), atl);
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    try {
                        chk.setNode(i, j, k, HardcodedNodes.AIR);
                    } catch (VxlPluginExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        chk.clearQueue();
        chk.onlyPage.rebuild();
        nd1 = new BaseMapNode("test:n1", ap1);
        nd2 = new BaseMapNode("test:n2", ap2);
        atl.packBatch(tex1, tex2, tex3);
        try {
            nrt.registerMapNode(nd1);
        } catch (VxlPluginExecutionException e) {
            e.printStackTrace();
        }
        try {
            nrt.registerMapNode(nd2);
        } catch (VxlPluginExecutionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 15-i; j++) {
                for (int k = 0; k < 15-i; k++) {
                    try {
                        chk.setNode(j, i, k, ((i) % 2 == 0) ? nd1 : nd2);
                    } catch (VxlPluginExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        lam.addChunk(chk);
        mb = new ModelBatch();
        sb = new SpriteBatch();

    }

    @Override
    public void resize(int width, int height) {

    }

    boolean lastSpacePressed = false;
    SpriteBatch sb;
    int frames = 0;

    @Override
    public void render() {
        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (frames == 60) {
            Gdx.graphics.setTitle("TEST | FPS: " + Gdx.graphics.getFramesPerSecond());
            frames = 0;
        }
        frames++;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glCullFace(Gdx.input.isKeyPressed(Input.Keys.B)?GL20.GL_NONE:GL20.GL_BACK);
        boolean curSpacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if ((curSpacePressed & !lastSpacePressed)||Gdx.input.isKeyPressed(Input.Keys.C)) {
            try {
                chk.handleOneQueueItem();
            } catch (VxlPluginExecutionException e) {
                e.printStackTrace();
            }
        }

        mb.begin(cam);

        mb.render(chk.onlyPage.getGdxModelInstance(),environment);
        mb.end();
        lastSpacePressed = curSpacePressed;
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
