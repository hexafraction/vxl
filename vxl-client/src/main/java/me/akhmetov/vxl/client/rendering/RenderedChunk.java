package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.utils.Disposable;
import me.akhmetov.vxl.api.rendering.BlockNodeAppearance;
import me.akhmetov.vxl.api.rendering.RenderBucket;
import me.akhmetov.vxl.core.map.LoadedMapChunk;
import sun.security.provider.certpath.Vertex;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;

public class RenderedChunk implements VxlRenderable {
    public RenderedChunk(LoadedMapChunk delegate) {
        this.delegate = delegate;
    }

    private final LoadedMapChunk delegate;

    /*
     * OVERALL STRUCTURE:
     * Each chunk is split into render buckets. Each bucket contains one or more meshes (up to 48 per bucket), with
     * 512 quads per mesh. We use 48 meshes per bucket because theoretically one bucket could contain the absolute worst-case
     * situation of every possible quad, for example in the transparent no-cull bucket where different transparent nodes back-to-back
     * to each other would require both front and back faces.
     *
     * All node-level operations occur at the level of the chunk itself; each quad operation then gets issued to the correct
     * bucket and mesh. For adds, the bucket where the operation should occur is determined by the node appearance itself.
     * The bucket, and bucket address to which a write is issued is stored in 3D (index by [x][y][z]) arrays, as a packed integer.
     *
     * The quad ID is an address within each bucket that, possibly through some level of indirection, points
     * to to a group of 6 entries in the index buffer of that mesh. The reason for the indirection is that dropping quads
     * may require relocation of entries to keep the buffer contiguous, and quads might be relocated between meshes.
     */

    private class ChunkBucket {
        private final boolean useWeights;
        private final RenderBucket bucket;
        // Each bucket contains up to 48 meshes

        // Map quad IDs to (512*mesh) + quad
        int[] idToLocation = new int[512 * 48];
        // Reverse mapping, needed to support relocation of quads in the index buffer
        int[] locationToId = new int[512 * 48];
        // IDs might not be contiguous, but it doesn't mean that we can't try to allocate them contiguously for speed.
        // Only in the worst case will we have to do linear scans through the idToLocation array to find an available ID.
        int nextIdToAllocate = 0;

        {
            Arrays.fill(idToLocation, -1);
            Arrays.fill(locationToId, -1);
        }

        // initially all null, will be allocated as needed
        QuadMesh[] meshes = new QuadMesh[48];

        final Shader shader;

        public ChunkBucket(Shader shader, RenderBucket bucket) {
            this.shader = shader;
            this.useWeights = (shader instanceof WavyBlockNodeShader);
            this.quad = new float[this.useWeights ? 28 : 24];
            this.bucket = bucket;
        }

        private int[][][] posXFaces = new int[16][16][16];
        private int[][][] posYFaces = new int[16][16][16];
        private int[][][] posZFaces = new int[16][16][16];
        private int[][][] negXFaces = new int[16][16][16];
        private int[][][] negYFaces = new int[16][16][16];
        private int[][][] negZFaces = new int[16][16][16];

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

        private void drop(int id) {
            int loc = idToLocation[id];
            int meshId = loc / 512;
            boolean relocationDone = meshes[meshId].dropAndRelocateLast(loc%512);
            if(relocationDone){
                int relocatedLoc = meshId * 512 + meshes[meshId].quadCount;
                int relocatedId = locationToId[relocatedLoc]; // quadCount has been decremented hence no off by one
                idToLocation[relocatedId] = loc;
                locationToId[loc] = relocatedId;
                locationToId[relocatedLoc] = -1;
            } else {
                locationToId[loc] = -1;
            }
            idToLocation[id] = -1;
        }


        private int add(float[] quad) {
            for (int i = 0; i < 48; i++) {
                if (meshes[i] == null) {
                    meshes[i] = new QuadMesh(bucket.name() + "-" + i, useWeights);
                }
                if(meshes[i].quadCount<512){
                    int location = i * 512 + meshes[i].addQuad(quad);
                    int id = nextIdToAllocate;
                    if (idToLocation[id] != -1) {
                        id = 0;
                        while(idToLocation[id] != -1){
                            id++;
                        }
                    }
                    idToLocation[id] = location;
                    locationToId[location] = id;
                    return id;
                }

            }
            throw new RuntimeException("Ran out of room in meshes to store this quad. This should not happen.");
        }

        private final float[] quad;

        private void addNegZ(NodeTexAtlas atl, int x, int y, int z, BlockNodeAppearance bna, float light, float animWeight) {
            drop(negZFaces[x][y][z]);
            NodeTexAtlas.Tile tl = atl.getTile(bna.getNegZ());
            int i = 0;
            quad[i++] = x;
            quad[i++] = y;
            quad[i++] = z;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y + 1;
            quad[i++] = z;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y + 1;
            quad[i++] = z;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y;
            quad[i++] = z;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            int quadIdx = add(quad);
            negZFaces[x][y][z] = quadIdx;

        }

        private void addPosZ(NodeTexAtlas atl, int x, int y, int z, BlockNodeAppearance bna, float light, float animWeight) {
            drop(posZFaces[x][y][z]);
            NodeTexAtlas.Tile tl = atl.getTile(bna.getPosZ());
            int i = 0;
            quad[i++] = x;
            quad[i++] = y;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y + 1;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y + 1;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            int quadIdx = add(quad);
            posZFaces[x][y][z] = quadIdx;
        }

        private void addNegY(NodeTexAtlas atl, int x, int y, int z, BlockNodeAppearance bna, float light, float animWeight) {
            drop(negYFaces[x][y][z]);
            NodeTexAtlas.Tile tl = atl.getTile(bna.getNegY());
            int i = 0;
            quad[i++] = x;
            quad[i++] = y;
            quad[i++] = z;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y;
            quad[i++] = z;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            int quadIdx = add(quad);
            negYFaces[x][y][z] = quadIdx;
        }

        private void addPosY(NodeTexAtlas atl, int x, int y, int z, BlockNodeAppearance bna, float light, float animWeight) {
            drop(posYFaces[x][y][z]);
            NodeTexAtlas.Tile tl = atl.getTile(bna.getPosY());
            int i = 0;
            quad[i++] = x;
            quad[i++] = y + 1;
            quad[i++] = z;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y + 1;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y + 1;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y + 1;
            quad[i++] = z;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            int quadIdx = add(quad);
            posYFaces[x][y][z] = quadIdx;
        }

        private void addNegX(NodeTexAtlas atl, int x, int y, int z, BlockNodeAppearance bna, float light, float animWeight) {
            drop(negXFaces[x][y][z]);
            NodeTexAtlas.Tile tl = atl.getTile(bna.getNegX());
            int i = 0;
            quad[i++] = x;
            quad[i++] = y;
            quad[i++] = z;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y + 1;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x;
            quad[i++] = y + 1;
            quad[i++] = z;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            int quadIdx = add(quad);
            negXFaces[x][y][z] = quadIdx;

        }

        private void addPosX(NodeTexAtlas atl, int x, int y, int z, BlockNodeAppearance bna, float light, float animWeight) {
            drop(posXFaces[x][y][z]);
            NodeTexAtlas.Tile tl = atl.getTile(bna.getPosX());
            int i = 0;
            quad[i++] = x + 1;
            quad[i++] = y;
            quad[i++] = z;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y + 1;
            quad[i++] = z;
            quad[i++] = tl.region.getU2();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y + 1;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            quad[i++] = x + 1;
            quad[i++] = y;
            quad[i++] = z + 1;
            quad[i++] = tl.region.getU();
            quad[i++] = tl.region.getV2();
            quad[i++] = light;
            if (useWeights) quad[i++] = animWeight;

            int quadIdx = add(quad);
            posXFaces[x][y][z] = quadIdx;
        }
    }

    /**
     * A mesh consisting of quads. Uses GL_TRIANGLE primitives rather than quad primitives, which were removed
     * in OpenGL 3.1.
     */
    private class QuadMesh implements Disposable {
        private final FloatBuffer vertices;
        private final ShortBuffer indices;
        private final boolean hasAnimWeight;
        private final Mesh gdxMesh;
        private final BitSet quadBitmap = new BitSet(512);
        private int quadCount = 0;
        private int maxIndex = 0;

        public int getQuadCount() {
            return quadCount;
        }

        private MeshPart gdxMeshPart;

        public void update() {
            if (gdxMeshPart == null) {
                gdxMeshPart = new MeshPart(id, gdxMesh, 0, quadCount * 6, GL20.GL_TRIANGLES);
            } else {
                gdxMeshPart.set(id, gdxMesh, 0, quadCount * 6, GL20.GL_TRIANGLES);
            }
        }

        private final String id;

        public QuadMesh(String id, boolean hasAnimWeight) {
            this.id = id;
            this.hasAnimWeight = hasAnimWeight;
            if (this.hasAnimWeight) gdxMesh = new Mesh(false, 2048, 3072,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texcoord"),
                    new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_vtx_lighting"),
                    new VertexAttribute(VertexAttributes.Usage.BoneWeight, 1, "a_anim_weight"));
            else gdxMesh = new Mesh(false, 2048, 3072,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texcoord"),
                    new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_vtx_lighting"));
            vertices = gdxMesh.getVerticesBuffer();
            indices = gdxMesh.getIndicesBuffer();
        }

        @Override
        public void dispose() {
            gdxMesh.dispose();
        }

        // returns the position in the index buffer, where this quad got stored (using a stride of 6).
        private int addQuad(float[] quadBuf) {
            assert (quadBuf.length == (hasAnimWeight ? 28 : 24)) : "Float buffer has wrong length: " + quadBuf.length
                    + " for mesh " + (hasAnimWeight ? "with" : "without") + " animation weights";
            if (quadCount >= 512) {
                throw new RuntimeException("Not enough room in this mesh for this quad.");
            }
            int index = quadBitmap.nextClearBit(0);
            assert (index < 512);

            maxIndex = Math.max(index, maxIndex);
            vertices.limit(Math.max(vertices.position(), (maxIndex + 1) * (hasAnimWeight ? 28 : 24) - 1));
            vertices.position(index);
            vertices.put(quadBuf);
            indices.limit(indices.limit() + 6);
            indices.position(quadCount * 6);
            indices.put((short) (index * 4 + 0));
            indices.put((short) (index * 4 + 1));
            indices.put((short) (index * 4 + 3));
            indices.put((short) (index * 4 + 1));
            indices.put((short) (index * 4 + 2));
            indices.put((short) (index * 4 + 3));
            quadCount++;
            quadBitmap.set(index);
            return quadCount - 1;
        }


        // return value is whether a remap took place, in which case the ID arrays need to be updated so the last
        // quad is now where the dropped quad was.
        private boolean dropAndRelocateLast(int index) {
            int droppedFirstIndex = indices.get(index * 6);
            assert (quadBitmap.get(droppedFirstIndex / 4)) : "Expected the index being dropped to still be in the bitmap.";
            int relocatedQuadId = indices.get((quadCount - 1) * 6) / 4;
            boolean relocated = false;
            if (index != quadCount - 1) {
                relocated = true;
                // doing a relocation
                indices.position(index * 6);
                indices.put((short) (relocatedQuadId * 4 + 0));
                indices.put((short) (relocatedQuadId * 4 + 1));
                indices.put((short) (relocatedQuadId * 4 + 3));
                indices.put((short) (relocatedQuadId * 4 + 1));
                indices.put((short) (relocatedQuadId * 4 + 2));
                indices.put((short) (relocatedQuadId * 4 + 3));
            }
            quadCount--;
            quadBitmap.clear(droppedFirstIndex / 4);
            indices.limit(index * 6 - 1);
            return relocated;
        }

    }

    private final EnumMap<RenderBucket, ChunkBucket> buckets = new EnumMap<>(RenderBucket.class);

    @Override
    public void render() {
        // TODO
    }

    @Override
    public void update(Camera cam) {

    }
}
