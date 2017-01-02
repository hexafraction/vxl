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
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL13;

public class GameRenderer implements ApplicationListener {

    /* TODO next few revisions gameplan:
     1) Get a working shader of my own being loaded from resources. DONE
     1a) Wire up the necessary attributes and uniforms including minimal animation. ALMOST DONE (todo some uniforms and attrs for animation)
     2a) Create and test dropping a node. All the data structures are there, just do the add in reverse (drop visible, add hidden faces) DONE
     2) Clean up code massively (by reimplementing algorithms atop new structure). Allow separating by atlas pages, meshes, etc and extend possible attributes.
     3) Update the appearance class to handle transparent textures. Check for this and don't cull quads that face right into
            a non-identical node
     4) When I feel confident with the above, implement other view types as their own quads (i.e. plant-like, wallmounted)
     */


    ActiveArea lam = new NaiveActiveArea();
    MapNode nd1;
    MapNode nd2;
    NodeAppearance ap1;
    NodeAppearance ap2;
    Texture tex1;
    RenderContext rc;
    Texture tex2;
    Texture tex3;
    private ModelBatch mb;
    PixmapPacker pp = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 1, true);
    PackingWrapper wrp = new PackingWrapper(pp);

    GameState gs = new GameState(null, null, true);
    NodeResolutionTable nrt = new NodeResolutionTable(gs);

    OldRenderedChunk chk;
    private NodeTexAtlas atl;
    private ShaderProgram shaderProgram;
    private BlockNodeShader shader;


    public GameRenderer() throws Exception {

    }

    private Environment environment;
    private static Logger logger = LogManager.getLogger();
    public PerspectiveCamera cam;
    public CameraInputController camController;
    float pX, pY, pZ;
    Array<Renderable> renderables = new Array<>(48);
    Pool<Renderable> renderablePool = new ReflectionPool<Renderable>(Renderable.class);
    @Override
    public void create() {
        logger.warn("This is a temporary, problematic rendered. TODOs: Dropping nodes, transparency (both representing in appearance and " +
                "actually rendering, tons of testing, multiple atlases, general cleanup");
        environment = new Environment();
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(2f, 2f, 2f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        rc = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED));
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        tex1 = new NaiveTexture("tex_1.png");
        tex2 = new NaiveTexture("tex_2.png");
        tex3 = new NaiveTexture("tex_3.png");
        atl = new NodeTexAtlas(512, 512);
        ap1 = new BlockNodeAppearance(tex1, tex1, tex3, tex3, tex1, tex1);
        ap2 = new BlockNodeAppearance(tex2, tex2, tex2, tex2, tex2, tex2);
        chk = new OldRenderedChunk(new LoadedMapChunk(gs, 0, 0, 0, nrt), atl);
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
//        for (int i = 0; i < 16; i++) {
//            for (int j = 0; j < 15-i; j++) {
//                for (int k = 0; k < 15-i; k++) {
//                    try {
//                        chk.setNode(j, i, k, ((i) % 2 == 0) ? nd1 : nd2);
//                    } catch (VxlPluginExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }

//        for (int j = 0; j < 16; j++) {
//            for (int k = 0; k < 16; k++) {
//                try {
//                    if((j / 2 + k / 2)%2==0)
//                        chk.setNode(0, j, k, nd1);
//                } catch (VxlPluginExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        for (int j = 0; j < 16; j++) {
//            for (int k = 0; k < 16; k++) {
//                try {
//                    if((15/2+j/2+k/2)%2==0)
//                        chk.setNode(15, j, k, nd1);
//                } catch (VxlPluginExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    try {
                        //if((i+j+k)%2==0)
                            chk.setNode(i, j, k, (i>=4 && i < 12)?nd1:nd1);
                    } catch (VxlPluginExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    try {
                        if((i/2+j/2+k/2)%2==0)
                            chk.setNode(i, j, k, HardcodedNodes.AIR);
                    } catch (VxlPluginExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
//        for (int i = 0; i < 16; i++) {
//            for (int j = 0; j < 16; j++) {
//                for (int k = 0; k < 16; k++) {
//                    try {
//                        if((i+j+k)%2==1)
//                            chk.setNode(i, j, k, nd1);
//                    } catch (VxlPluginExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
        lam.addChunk(chk);
        final String vertexShader = Gdx.files.internal("vertex.glsl").readString();
        final String fragmentShader = Gdx.files.internal("fragment.glsl").readString();
        shader = new BlockNodeShader();
        shader.init();
        sb = new SpriteBatch();
        mb = new ModelBatch((camera, renderables) -> {
            // no op
        });
        rc.begin();

        shader.begin(cam, rc);

        shader.bindTexture(atl.getGdxAtlas().getTextures().first());
        Gdx.gl.glEnable(GL13.GL_MULTISAMPLE);

    }

    @Override
    public void resize(int width, int height) {
        cam.viewportHeight = height;
        cam.viewportWidth = width;
    }

    boolean lastSpacePressed = false;
    SpriteBatch sb;
    int frames = 0;
    int skip = 0;
    boolean usingShader = true;
    boolean lastIncPressed = false;
    @Override
    public void render() {
        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (frames == 30) {
            Gdx.graphics.setTitle((usingShader?"OWN":"GDX")+" | QUADS: "+chk.onlyPage.countQuads()+" | FPS: " + Gdx.graphics.getFramesPerSecond());
            frames = 0;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.F)){
            usingShader = true;
            rc.begin();

            shader.begin(cam, rc);

            shader.bindTexture(atl.getGdxAtlas().getTextures().first());
        }
        if(Gdx.input.isKeyPressed(Input.Keys.G)){
            usingShader = false;
        }
        frames++;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClearDepthf(1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //Gdx.gl.glDisable(Gdx.gl20.GL_BLEND);
        //Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glCullFace(Gdx.input.isKeyPressed(Input.Keys.B)?GL20.GL_NONE:GL20.GL_BACK);
        boolean curSpacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        boolean curIncPressed = Gdx.input.isKeyPressed(Input.Keys.SLASH);
        if ((curSpacePressed & !lastSpacePressed)||Gdx.input.isKeyPressed(Input.Keys.C)) {
            try {
                chk.handleOneQueueItem();
            } catch (VxlPluginExecutionException e) {
                logger.fatal("Failed to handle a chunk delta. If this happens in production we should rebuild the entire chunk in this case.", e);
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            while(chk.getQueueBacklog()>0){
                try {
                    chk.handleOneQueueItem();
                } catch (VxlPluginExecutionException e) {
                    logger.fatal("Failed to handle a chunk delta. If this happens in production we should rebuild the entire chunk in this case.", e);
                }
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            shader.setWireframe(true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.Q)){
            shader.setWireframe(false);
        }
        if(curIncPressed && !lastIncPressed) skip = (skip+1)%48;
        //mb.begin(cam);
        cam.update();
       //shaderProgram.setUniformMatrix("u_projViewTrans", cam.combined);
        //atl.getGdxAtlas().getTextures().first().bind(0);
        //shaderProgram.setUniformi("u_texture", 0);
        int skipped = 0;
        if(usingShader) {

            shader.update(cam);
            renderables.clear();
            chk.onlyPage.getGdxModelInstance().getRenderables(renderables, renderablePool);
            //renderables.reverse();
            for (Renderable r : renderables) {
                r.environment = environment;
                r.worldTransform.idt();
                if(skipped>=skip) shader.render(r);
                skipped++;
            }
        } else {
            mb.begin(cam);
            mb.render(chk.onlyPage.getGdxModelInstance());
            mb.end();
        }

        //mb.render(chk.onlyPage.getGdxModelInstance());
        //mb.end();
        lastSpacePressed = curSpacePressed;
        lastIncPressed = curIncPressed;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

        shader.end();
        rc.end();
        tex1.dispose();
        tex2.dispose();
        wrp.dispose();
        shader.dispose();

    }
}
