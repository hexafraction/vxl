package me.akhmetov.vxl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a 16x16x16 element of the game world, with its associated metadata.
 */
public class MapChunk {
    /**
     * Used for caching and tracking whether saving is needed.
     */
    private AtomicLong modificationCount;
    /**
     * Packed representation of the areas that have been modified in this chunk.
     * This is represented using 16 nibbles, each representing modifications in each z-"slice",
     * from bottom to top (going from least to most significant nibble)
     * Within each nibble, the bits represent, from MSB to LSB:
     * * 0 <= x < 8; 0 <= y < 8
     * * 8 <= x < 16; 0 <= y < 8
     * * 0 <= x < 8; 8 <= y < 16
     * * 8 <= x < 16; 8 <= y < 16
     */
    private AtomicLong modificationBitfield;

    /**
     * Reference to game state
     */
    private GameState game;

    /**
     * The actual entries in the chunk, stored unpacked.
     */
    private int[][][] chunkData = new int[16][16][16];

    /**
     * The CHUNK position in 3D space. The range makes the world hypothetically
     * up to 68mln km in each dimension.
     */
    private int xid, yid, zid;

    /**
     * The list of extended nodes. It becomes "sparse" during operation but becomes less "sparse" when a chunk is loaded
     * from disk.
     */
    ArrayList<NodeMetadata> extendedNodes = new ArrayList<NodeMetadata>();

    /**
     * Resolves node IDs to nodes
     */
    private NodeResolver resolver;

    /**
     * Sets the value of the node to the integer value.
     */

    public void setNode(int xP, int yP, int zP, int node) {
        chunkData[zP][yP][xP] = node;
        modificationBitfield.accumulateAndGet(getModBit(xP, yP, zP), (a, b) -> a | b);
    }

    /**
     * Gets the numeric value of the node
     *
     * @param xP
     * @param yP
     * @param zP
     * @return
     */
    public int getNodeVal(int xP, int yP, int zP) {
        return chunkData[xP][yP][zP];
    }

    public MapNode getNode(int xP, int yP, int zP) {
        int val = getNodeVal(xP, yP, zP);
        if (val < (1 << 30)) {
            // standard node
            return resolver.resolveNode(val);
        }
        else {
            NodeMetadata md = extendedNodes.get(val - (1 <<30));
            return md.getNode();
        }
    }

    // gets the bitfield for a single node modification
    private long getModBit(int xP, int yP, int zP) {
        if (xP < 8) {
            if (yP < 8) {
                return 1L << (zP * 4);
            } else {
                return 1L << (zP * 4 + 2);
            }
        } else {
            if (yP < 8) {
                return 1L << (zP * 4 + 1);
            } else {
                return 1L << (zP * 4 + 3);
            }
        }
    }

    HashMap<String, Object> chunkMetadata = new HashMap<>();
}
