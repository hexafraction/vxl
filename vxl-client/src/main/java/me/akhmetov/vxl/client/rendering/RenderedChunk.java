package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.api.map.MapChunk;
import me.akhmetov.vxl.api.map.MapNode;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.BitSet;

public class RenderedChunk implements MapChunk {
    private MapChunk delegate;


    private class ChunkMesh implements Disposable {

        Model gdxModel;
        ModelInstance gdxModelInstance;

        @Override
        public void dispose() {
            for (ChunkMeshDivision cmd : divisions) {
                if (cmd != null) cmd.dispose();
            }
            gdxModel.dispose();

        }

        private class ChunkMeshDivision implements Disposable {
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
                fb.position(index * 20);
                fb.put(quad, 0, 20);

                // we know they're CCW
                ShortBuffer sb = gdxMesh.getIndicesBuffer();
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
            public void dropQuad(int indirectIndex) {
                ShortBuffer sb = gdxMesh.getIndicesBuffer();
                int firstIndex = sb.get(indirectIndex * 6);
                int quadId = firstIndex / 4;
                assert (quadAllocationBitset.get(quadId));
                assert (indirectIndex <= quadCount - 1);
                if (indirectIndex != quadCount - 1) {
                    // it's not the last quad in the indices buffer. We need to relocate the last quad to the
                    // spot we just freed to keep the indices contiguous. We can leave the
                    // last value as stale (recall that the mesh part limit will keep us from rendering it, once we update
                    // that limit).
                    // Instead of doing a bunch of reads let's base things on the quadId. It should be consistent.

                    // Anyway, otherwise we just decrement the number of quads and perform other bookkeeping.
                    sb.position(indirectIndex * 6); // we want to append to end
                    sb.put((short) (quadId * 4 + 0));
                    sb.put((short) (quadId * 4 + 1));
                    sb.put((short) (quadId * 4 + 3));
                    sb.put((short) (quadId * 4 + 1));
                    sb.put((short) (quadId * 4 + 2));
                    sb.put((short) (quadId * 4 + 3));
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


        /* TODO:
         * Create method for drawing a quad to the first open division
         * Provide a provision for moving quads from one division to another if combining them is useful.
                -> Does this actually provide a performance benefit?
         * Provide a means of relating a node add/drop to one or more quad adds/drops.
                -> I guess backfaces only if a transparent into air (or other non-solid)
        */

    }

    // one mesh for each atlas page
    ArrayList<ChunkMesh>[] meshesByAtlasPage;

    /* TODO:
     * Create methods for handling chunk updates by their types. Delegate to the right atlas page mesh
     */

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
