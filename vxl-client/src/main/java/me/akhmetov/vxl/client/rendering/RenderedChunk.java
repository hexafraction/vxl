package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.api.map.MapChunk;
import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.rendering.BlockNodeAppearance;
import me.akhmetov.vxl.core.ChunkCorruptionException;
import me.akhmetov.vxl.core.map.MapChunkDelta;
import me.akhmetov.vxl.core.map.LoadedMapChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.soap.Node;
import java.io.IOException;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class RenderedChunk implements MapChunk {
    private static Logger logger = LogManager.getLogger();

    private LoadedMapChunk delegate;
    private NodeTexAtlas atl;
    private ModelBuilder mb = new ModelBuilder();

    public RenderedChunk(LoadedMapChunk delegate, NodeTexAtlas atlas) {
        this.delegate = delegate;
        this.atl = atlas;

    }

    public void setNode(int xP, int yP, int zP, int node) {
        delegate.setNode(xP, yP, zP, node);
    }

    public byte[] serialize() throws IOException {
        return delegate.serialize();
    }

    public long getModificationBitfield() {
        return delegate.getModificationBitfield();
    }

    public MapChunkDelta pollQueue() {
        return delegate.pollQueue();
    }

    public void deserialize(byte[] buf) throws ChunkCorruptionException {
        delegate.deserialize(buf);
    }

    public int getQueueBacklog() {
        return delegate.getQueueBacklog();
    }

    public void clearQueue() {
        delegate.clearQueue();
    }

    public void handleOneQueueItem() throws VxlPluginExecutionException {
        MapChunkDelta mcd = pollQueue();
        if (mcd != null)
            handleDelta(mcd, atl);

        onlyPage.rebuild();
    }


    public class ChunkMesh implements Disposable {
        public ChunkMesh() {
            for (int i = 0; i < 48; i++) {
                divisions[i] = new ChunkMeshDivision();
            }
        }

        public ModelInstance getGdxModelInstance() {
            return gdxModelInstance;
        }

        Model gdxModel;
        ModelInstance gdxModelInstance;

        public void rebuild() {
            mb.begin();
            for (int i = 0; i < 48; i++) {
                if (divisions[i].quadCount != 0) {
                    divisions[i].gdxMeshPart = mb.part("cmd" + i, divisions[i].gdxMesh, GL20.GL_TRIANGLES, 0, divisions[i].quadCount * 6,
                            new Material(new TextureAttribute(TextureAttribute.Diffuse, atl.getGdxAtlas().getTextures().first()),
                            new FloatAttribute(FloatAttribute.AlphaTest, 0.5f),
                                    new BlendingAttribute(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA),
                                    new IntAttribute(IntAttribute.CullFace, GL20.GL_BACK)));
                    //System.out.println(divisions[i].quadCount);
                }

            }
            gdxModel = mb.end();
            gdxModelInstance = new ModelInstance(gdxModel, 0, 0, 0);

        }

        @Override
        public void dispose() {
            for (ChunkMeshDivision cmd : divisions) {
                if (cmd != null) cmd.dispose();
            }
            gdxModel.dispose();

        }

        public int countQuads(){
            int total = 0;
            for(ChunkMeshDivision cmd : divisions){
                total+=cmd.quadCount;
            }
            return total;
        }

        public class ChunkMeshDivision implements Disposable {
            int quadCount = 0; // number of quads occupied, out of limit of 512.
            BitSet quadAllocationBitset = new BitSet(512); // allows us to determine which parts of the vertex buffer can be used for new quads
            Mesh gdxMesh = new Mesh(Mesh.VertexDataType.VertexBufferObject, false, 2048, 3072,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

            MeshPart gdxMeshPart;

            // Callers encouraged to reuse a single array for quad
            // Order of fields of each vertex is x y z u v, vertices in COUNTERCLOCKWISE order
            // returns the QUAD index (that is, 1/4 of the vertex index, or 1/20 of the short buffer index)
            public int addQuad(float[] quad) {
                assert (quad.length == 20);
                // we need to put the vertices into the first open slot in the VBO,
                // and put the indices into the next spot in the index buffer.
                if (quadCount == 512) throw new RenderException("Tried to add a quad to a full division.");
                int ncb = quadAllocationBitset.nextClearBit(0);
                assert (ncb < 512);
                short index = (short) ncb;
                FloatBuffer fb = gdxMesh.getVerticesBuffer();
                fb.limit(fb.capacity());
                fb.position(index * 20);
                fb.put(quad, 0, 20);

                // we know they're CCW
                ShortBuffer sb = gdxMesh.getIndicesBuffer();
                sb.limit(sb.capacity());
                sb.position(quadCount * 6); // we want to append to end
                sb.put((short) (index * 4 + 0));
                sb.put((short) (index * 4 + 1));
                sb.put((short) (index * 4 + 3));
                sb.put((short) (index * 4 + 1));
                sb.put((short) (index * 4 + 2));
                sb.put((short) (index * 4 + 3));

                quadCount++;
                quadAllocationBitset.set(index);
                return quadCount - 1;
            }


            // indirect index is what addQuad returned. It points into the indices buffer (but is divided by six)
            // returns true if remap occurred
            public void dropQuad(int indirectIndex) {
                if (indirectIndex >= quadCount) {
                    logger.warn("dropping something nonexistent");
                }
                ShortBuffer sb = gdxMesh.getIndicesBuffer();
                sb.limit(sb.capacity());
                int firstIndex = sb.get(indirectIndex * 6);
                int quadId = firstIndex / 4;
                if (!quadAllocationBitset.get(quadId)) {
                    logger.warn("Dropping something non-existent");
                }
                int relocQuadId = sb.get((quadCount-1)*6)/4;
                if (indirectIndex != quadCount - 1) {

                    // it's not the last quad in the indices buffer. We need to relocate the last quad to the
                    // spot we just freed to keep the indices contiguous. We can leave the
                    // last value as stale (recall that the mesh part limit will keep us from rendering it, once we update
                    // that limit).
                    // Instead of doing a bunch of reads let's base things on the quadId. It should be consistent.

                    // Anyway, otherwise we just decrement the number of quads and perform other bookkeeping.
                    sb.position(indirectIndex * 6); // we want to write to the spot we just dropped.
                    sb.put((short) (relocQuadId * 4 + 0));
                    sb.put((short) (relocQuadId * 4 + 1));
                    sb.put((short) (relocQuadId * 4 + 3));
                    sb.put((short) (relocQuadId * 4 + 1));
                    sb.put((short) (relocQuadId * 4 + 2));
                    sb.put((short) (relocQuadId * 4 + 3));
                }
                quadCount--;
                quadAllocationBitset.clear(quadId);

            }

            @Override
            public void dispose() {
                if (gdxMesh != null) gdxMesh.dispose();

            }
        }


        // A good intermediate value for number of triangles in a mesh is around 1k (source: http://stackoverflow.com/a/5065195)
        // This corresponds to about 512, give or take, quads. The VBO size isn't expected to be crucial (but profiling will be done)
        // The mesh is thus segmented into ChunkMeshDivision instances that each contain 512 quads and their associated indices (recalling
        // that subdivision by texture atlas pages, if any, has already been done). Each mesh part contains a VBO holding up to 512*4=2048
        // vertices, and 512*6=3072 indices.
        // The absolute worst case is 16*16*16*6 quads (that is, all faces rendered) which corresponds to 48 mesh parts.
        // Hence, the array of fixed size.

        ChunkMeshDivision[] divisions = new ChunkMeshDivision[48];

        // these six buffers are used for determining which mesh part, and where in the mesh part, a given face lies.
        // Each index buffer contains quads (as triangles) with a stride of 6. For a given value i in this array, it
        // refers to meah part (i/512) and indices in the index array (6*(i%512)) to (6*(i%512)+5).
        // Note the two layers of indirection (these arrays to OpenGL index arrays, and OpenGL index arrays to OpenGL vertex buffers)

        int[][][] posXFaces = new int[16][16][16];
        int[][][] negXFaces = new int[16][16][16];
        int[][][] posYFaces = new int[16][16][16];
        int[][][] negYFaces = new int[16][16][16];
        int[][][] posZFaces = new int[16][16][16];
        int[][][] negZFaces = new int[16][16][16];

        {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    for (int k = 0; k < 16; k++) {
                        posXFaces[i][j][k] = -1;
                        posYFaces[i][j][k] = -1;
                        posZFaces[i][j][k] = -1;
                        negXFaces[i][j][k] = -1;
                        negYFaces[i][j][k] = -1;
                        negZFaces[i][j][k] = -1;
                    }
                }
            }
        }

        // index is 256*6*x + 16*6*y + 6*z + k where k is 0 for posX, 1 for negX, ..., 5 fot negZ
        private int[] indexReverseTable = new int[24576];

        {
            Arrays.fill(indexReverseTable, -1);
        }


        /* TODO:
         * Create method for drawing a quad to the first open division
         * Provide a provision for moving quads from one division to another if combining them is useful.
                -> Does this actually provide a performance benefit? Dubious for now.
         * Provide a means of relating a node add/drop to one or more quad adds/drops.
                -> I guess backfaces only if a transparent into air (or other non-solid)
        */


    }

    // shared to avoid reallocation. The quad being drawn.
    float[] quad = new float[20];

    public void handleDelta(MapChunkDelta delta, NodeTexAtlas atl) throws VxlPluginExecutionException {
        switch (delta.getType()) {
            case ADD:
                if (!(delta.getAfter().getAppearance() instanceof BlockNodeAppearance)) {
                    logger.error("Can't render an appearance of " + delta.getAfter().getAppearance().getClass().getName() + " just yet. Tell the dev.");
                    return;
                }
                BlockNodeAppearance bna = (BlockNodeAppearance) delta.getAfter().getAppearance();

                boolean hasPosXNeighbor = delta.getX() != 15 && onlyPage.negXFaces[delta.getX() + 1][delta.getY()][delta.getZ()] != -1/*getNode(delta.getX() + 1, delta.getY(), delta.getZ()).getAppearance() instanceof BlockNodeAppearance*/;
                if (hasPosXNeighbor) {
                    drop(onlyPage.negXFaces[delta.getX() + 1][delta.getY()][delta.getZ()]);
                    onlyPage.negXFaces[delta.getX() + 1][delta.getY()][delta.getZ()] = -1;
                } else {
                    // add
                    NodeTexAtlas.Tile tl = atl.getTile(bna.getPosX());
                    quad[0] = delta.getX() + 1;
                    quad[1] = delta.getY();
                    quad[2] = delta.getZ();
                    quad[3] = tl.region.getU2();
                    quad[4] = tl.region.getV2();

                    quad[5] = delta.getX() + 1;
                    quad[6] = delta.getY() + 1;
                    quad[7] = delta.getZ();
                    quad[8] = tl.region.getU2();
                    quad[9] = tl.region.getV();

                    quad[10] = delta.getX() + 1;
                    quad[11] = delta.getY() + 1;
                    quad[12] = delta.getZ() + 1;
                    quad[13] = tl.region.getU();
                    quad[14] = tl.region.getV();

                    quad[15] = delta.getX() + 1;
                    quad[16] = delta.getY();
                    quad[17] = delta.getZ() + 1;
                    quad[18] = tl.region.getU();
                    quad[19] = tl.region.getV2();
                    int quadIdx = add(quad);
                    onlyPage.posXFaces[delta.getX()][delta.getY()][delta.getZ()] = quadIdx;
                    onlyPage.indexReverseTable[quadIdx] = delta.getX() * 256 * 6 + delta.getY() * 16 * 6 + delta.getZ() * 6 + 0;
                }
                boolean hasNegXNeighbor = delta.getX() != 0 && onlyPage.posXFaces[delta.getX() - 1][delta.getY()][delta.getZ()] != -1;/*getNode(delta.getX() - 1, delta.getY(), delta.getZ()).getAppearance() instanceof BlockNodeAppearance*/
                if (hasNegXNeighbor) {
                    drop(onlyPage.posXFaces[delta.getX() - 1][delta.getY()][delta.getZ()]);
                    onlyPage.posXFaces[delta.getX() - 1][delta.getY()][delta.getZ()] = -1;
                } else {
                    // add
                    NodeTexAtlas.Tile tl = atl.getTile(bna.getNegX());
                    quad[0] = delta.getX();
                    quad[1] = delta.getY();
                    quad[2] = delta.getZ();
                    quad[3] = tl.region.getU();
                    quad[4] = tl.region.getV2();

                    quad[5] = delta.getX();
                    quad[6] = delta.getY();
                    quad[7] = delta.getZ() + 1;
                    quad[8] = tl.region.getU2();
                    quad[9] = tl.region.getV2();

                    quad[10] = delta.getX();
                    quad[11] = delta.getY() + 1;
                    quad[12] = delta.getZ() + 1;
                    quad[13] = tl.region.getU2();
                    quad[14] = tl.region.getV();

                    quad[15] = delta.getX();
                    quad[16] = delta.getY() + 1;
                    quad[17] = delta.getZ();
                    quad[18] = tl.region.getU();
                    quad[19] = tl.region.getV();
                    int quadIdx = add(quad);
                    onlyPage.negXFaces[delta.getX()][delta.getY()][delta.getZ()] = quadIdx;
                    onlyPage.indexReverseTable[quadIdx] = delta.getX() * 256 * 6 + delta.getY() * 16 * 6 + delta.getZ() * 6 + 1;

                }

                boolean hasPosYNeighbor = delta.getY() != 15 && onlyPage.negYFaces[delta.getX()][delta.getY() + 1][delta.getZ()] != -1;/*getNode(delta.getX(), delta.getY() + 1, delta.getZ()).getAppearance() instanceof BlockNodeAppearance;*/
                if (hasPosYNeighbor) {
                    drop(onlyPage.negYFaces[delta.getX()][delta.getY() + 1][delta.getZ()]);
                    onlyPage.negYFaces[delta.getX()][delta.getY() + 1][delta.getZ()] = -1;
                } else {
                    NodeTexAtlas.Tile tl = atl.getTile(bna.getPosY());
                    quad[0] = delta.getX();
                    quad[1] = delta.getY() + 1;
                    quad[2] = delta.getZ();
                    quad[3] = tl.region.getU2();
                    quad[4] = tl.region.getV2();

                    quad[5] = delta.getX();
                    quad[6] = delta.getY() + 1;
                    quad[7] = delta.getZ() + 1;
                    quad[8] = tl.region.getU2();
                    quad[9] = tl.region.getV();

                    quad[10] = delta.getX() + 1;
                    quad[11] = delta.getY() + 1;
                    quad[12] = delta.getZ() + 1;
                    quad[13] = tl.region.getU();
                    quad[14] = tl.region.getV();

                    quad[15] = delta.getX() + 1;
                    quad[16] = delta.getY() + 1;
                    quad[17] = delta.getZ();
                    quad[18] = tl.region.getU();
                    quad[19] = tl.region.getV2();
                    int quadIdx = add(quad);
                    onlyPage.posYFaces[delta.getX()][delta.getY()][delta.getZ()] = quadIdx;
                    onlyPage.indexReverseTable[quadIdx] = delta.getX() * 256 * 6 + delta.getY() * 16 * 6 + delta.getZ() * 6 + 2;
                }
                boolean hasNegYNeighbor = delta.getY() != 0 && onlyPage.posYFaces[delta.getX()][delta.getY() - 1][delta.getZ()] != -1;
                if (hasNegYNeighbor) {
                    drop(onlyPage.posYFaces[delta.getX()][delta.getY() - 1][delta.getZ()]);
                    onlyPage.posYFaces[delta.getX()][delta.getY() - 1][delta.getZ()] = -1;
                } else {
                    // add
                    NodeTexAtlas.Tile tl = atl.getTile(bna.getNegY());
                    quad[0] = delta.getX();
                    quad[1] = delta.getY();
                    quad[2] = delta.getZ();
                    quad[3] = tl.region.getU();
                    quad[4] = tl.region.getV2();

                    quad[5] = delta.getX() + 1;
                    quad[6] = delta.getY();
                    quad[7] = delta.getZ();
                    quad[8] = tl.region.getU2();
                    quad[9] = tl.region.getV2();

                    quad[10] = delta.getX() + 1;
                    quad[11] = delta.getY();
                    quad[12] = delta.getZ() + 1;
                    quad[13] = tl.region.getU2();
                    quad[14] = tl.region.getV();

                    quad[15] = delta.getX();
                    quad[16] = delta.getY();
                    quad[17] = delta.getZ() + 1;
                    quad[18] = tl.region.getU();
                    quad[19] = tl.region.getV();
                    int quadIdx = add(quad);
                    onlyPage.negYFaces[delta.getX()][delta.getY()][delta.getZ()] = quadIdx;
                    onlyPage.indexReverseTable[quadIdx] = delta.getX() * 256 * 6 + delta.getY() * 16 * 6 + delta.getZ() * 6 + 3;
                }

                boolean hasPosZNeighbor = delta.getZ() != 15 && onlyPage.negZFaces[delta.getX()][delta.getY()][delta.getZ() + 1] != -1;
                if (hasPosZNeighbor) {

                    drop(onlyPage.negZFaces[delta.getX()][delta.getY()][delta.getZ() + 1]);
                    onlyPage.negZFaces[delta.getX()][delta.getY()][delta.getZ() + 1] = -1;
                } else {
                    // add
                    NodeTexAtlas.Tile tl = atl.getTile(bna.getPosZ());
                    quad[0] = delta.getX();
                    quad[1] = delta.getY();
                    quad[2] = delta.getZ() + 1;
                    quad[3] = tl.region.getU();
                    quad[4] = tl.region.getV2();

                    quad[5] = delta.getX() + 1;
                    quad[6] = delta.getY();
                    quad[7] = delta.getZ() + 1;
                    quad[8] = tl.region.getU2();
                    quad[9] = tl.region.getV2();

                    quad[10] = delta.getX() + 1;
                    quad[11] = delta.getY() + 1;
                    quad[12] = delta.getZ() + 1;
                    quad[13] = tl.region.getU2();
                    quad[14] = tl.region.getV();

                    quad[15] = delta.getX();
                    quad[16] = delta.getY() + 1;
                    quad[17] = delta.getZ() + 1;
                    quad[18] = tl.region.getU();
                    quad[19] = tl.region.getV();
                    int quadIdx = add(quad);
                    onlyPage.posZFaces[delta.getX()][delta.getY()][delta.getZ()] = quadIdx;
                    onlyPage.indexReverseTable[quadIdx] = delta.getX() * 256 * 6 + delta.getY() * 16 * 6 + delta.getZ() * 6 + 4;
                }
                boolean hasNegZNeighbor = delta.getZ() != 0 && onlyPage.posZFaces[delta.getX()][delta.getY()][delta.getZ() - 1] != -1;
                if (hasNegZNeighbor) {
                    drop(onlyPage.posZFaces[delta.getX()][delta.getY()][delta.getZ() - 1]);
                    onlyPage.posZFaces[delta.getX()][delta.getY()][delta.getZ() - 1] = -1;
                } else {
                    // add
                    NodeTexAtlas.Tile tl = atl.getTile(bna.getNegZ());
                    quad[0] = delta.getX();
                    quad[1] = delta.getY();
                    quad[2] = delta.getZ();
                    quad[3] = tl.region.getU2();
                    quad[4] = tl.region.getV2();

                    quad[5] = delta.getX();
                    quad[6] = delta.getY() + 1;
                    quad[7] = delta.getZ();
                    quad[8] = tl.region.getU2();
                    quad[9] = tl.region.getV();

                    quad[10] = delta.getX() + 1;
                    quad[11] = delta.getY() + 1;
                    quad[12] = delta.getZ();
                    quad[13] = tl.region.getU();
                    quad[14] = tl.region.getV();

                    quad[15] = delta.getX() + 1;
                    quad[16] = delta.getY();
                    quad[17] = delta.getZ();
                    quad[18] = tl.region.getU();
                    quad[19] = tl.region.getV2();
                    int quadIdx = add(quad);
                    onlyPage.negZFaces[delta.getX()][delta.getY()][delta.getZ()] = quadIdx;
                    onlyPage.indexReverseTable[quadIdx] = delta.getX() * 256 * 6 + delta.getY() * 16 * 6 + delta.getZ() * 6 + 5;
                }
                break;
            case DROP:
                throw new NotImplementedException();
            case CHANGE:
                throw new NotImplementedException();
        }
    }

    private int add(float[] quad) {
        for (int i = 0; i < 48; i++) {
            if (onlyPage.divisions[i].quadCount < 512) {
                int index = onlyPage.divisions[i].addQuad(quad);
                logger.trace("Adding quad at index " + index);
                return i * 512 + index;
            }
        }
        throw new AssertionError("INTERNAL INCONSISTENCY (not enough space in chunk VBOs). Should never reach this state.");
    }

    private void drop(int index) {
        if (index != -1) {
            logger.trace("Dropping quad at " + index);
            int indexInPart = index % 512;
            int division = index / 512;
            if (indexInPart != onlyPage.divisions[division].quadCount - 1) {
                logger.debug("Relocating "+  (onlyPage.divisions[division].quadCount - 1) + " to " + indexInPart);
                // a relocation is happening
                // The quad being relocated is the last one. The quadCount is grabbed BEFORE actual drop occurs
                int relocPackedIndex = onlyPage.indexReverseTable[division * 512 + onlyPage.divisions[division].quadCount - 1];
                // The reverse table is updated to reflect the relocation.
                onlyPage.indexReverseTable[index] = relocPackedIndex;
                onlyPage.indexReverseTable[division * 512 + onlyPage.divisions[division].quadCount - 1] = -1;

                // We unpack the relocPackedIndex and use it to update the indirect pointer into the mesh data to point to the post-relocation
                // position
                if(relocPackedIndex<0){
                    logger.error("This shouldn't happen.");
                    return;
                }
                int x = relocPackedIndex/(256*6);
                int y = (relocPackedIndex/(16*6))%16;
                int z = (relocPackedIndex/6)%16;

                switch(relocPackedIndex%6){
                    case 0:
                        onlyPage.posXFaces[x][y][z] = index;
                        break;
                    case 1:
                        onlyPage.negXFaces[x][y][z] = index;
                        break;
                    case 2:
                        onlyPage.posYFaces[x][y][z] = index;
                        break;
                    case 3:
                        onlyPage.negYFaces[x][y][z] = index;
                        break;
                    case 4:
                        onlyPage.posZFaces[x][y][z] = index;
                        break;
                    case 5:
                        onlyPage.negZFaces[x][y][z] = index;
                        break;


                }

            }
            onlyPage.divisions[division].dropQuad(indexInPart);
        }
    }


    // one mesh for each atlas page, but right now we're skipping that.
    ChunkMesh onlyPage = new ChunkMesh();


    // DELEGATING METHODS BELOW
    @Override
    public void setNode(int xP, int yP, int zP, MapNode node) throws VxlPluginExecutionException {
        delegate.setNode(xP, yP, zP, node);
    }

    @Override
    public MapNode getNode(int xP, int yP, int zP) throws VxlPluginExecutionException {
        return delegate.getNode(xP, yP, zP);
    }

    @Override
    public int getX() {
        return delegate.getX();
    }

    @Override
    public int getY() {
        return delegate.getY();
    }

    @Override
    public int getZ() {
        return delegate.getZ();
    }

    @Override
    public Serializable put(String key, Serializable value) {
        return delegate.put(key, value);
    }

    @Override
    public Serializable get(String key) {
        return delegate.get(key);
    }

}
