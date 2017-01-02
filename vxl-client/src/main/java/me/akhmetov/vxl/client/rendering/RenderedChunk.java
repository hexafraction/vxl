package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.graphics.Camera;
import me.akhmetov.vxl.api.rendering.RenderBucket;
import me.akhmetov.vxl.core.map.LoadedMapChunk;

import java.util.EnumMap;

public class RenderedChunk implements VxlRenderable {
    public RenderedChunk(LoadedMapChunk delegate) {
        this.delegate = delegate;
    }

    private final LoadedMapChunk delegate;

    /*
     * OVERALL STRUCTURE:
     * Each chunk is split into render buckets. Each bucket contains one or more meshes (up to 48 per bucket), with
     * 3072 quads per mesh. We use 48 meshes per bucket because thoretically one bucket could contain the absolute worst-case
     * situation of every possible quad, for example in the transparent no-cull bucket where different transparent nodes back-to-back
     * to each other would require both front and back faces.
     *
     * All node-level operations occur at the level of the chunk itself; each quad operation then gets issued to the correct
     * bucket and mesh. For adds, the bucket where the operation should occur is determined by the node appearance itself.
     * The bucket, and bucket address to which a write is issued is stored in 3D (index by [x][y][z]) arrays, as a packed integer.
     *
     * The bucket address is an address within each bucket that, possibly through some level of indirection, points
     * to to a group of 6 entries in the index buffer of that mesh. The reason for the indirection is that dropping quads
     * may require relocation of entries to keep the buffer contiguous, and quads might be relocated between meshes.
     */

    private class ChunkBucket {
        // Each bucket contains up to 48 meshes

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
