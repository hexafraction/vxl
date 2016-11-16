package me.akhmetov.vxl.core;

import com.sun.xml.internal.ws.developer.Serialization;
import me.akhmetov.vxl.api.MapNode;
import me.akhmetov.vxl.api.MapNodeWithMetadata;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.core.security.SerializationSupport;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a 16x16x16 element of the game world, with its associated metadata.
 */
class MapChunkImpl implements me.akhmetov.vxl.api.MapChunk {
    /**
     * Used for caching and tracking whether saving is needed.
     */
    private long modificationCount = 0;
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
    private long modificationBitfield = 0;

    private int mapGeneratorSetUsed = 0;

    /**
     * Reference to game state
     */
    private GameState game;

    /**
     * The actual entries in the chunk, stored unpacked.
     */
    final int[][][] chunkData = new int[16][16][16];

    /**
     * The CHUNK position in 3D space. The range makes the world hypothetically
     * up to 68mln km in each dimension.
     */
    private final int xid, yid, zid;

    /**
     * The list of extended nodes. It becomes "sparse" during operation but becomes less "sparse" when a chunk is loaded
     * from disk.
     */
    final TreeMap<Integer, NodeMetadata> extendedNodes = new TreeMap<>();

    /**
     * Resolves node IDs to nodes
     */
    private NodeResolutionTable resolver;

    MapChunkImpl(GameState game, int xid, int yid, int zid, NodeResolutionTable resolver) {
        this.game = game;
        this.xid = xid;
        this.yid = yid;
        this.zid = zid;
        this.resolver = resolver;
    }

    /**
     * Sets the value of the node to the integer value.
     */

    public synchronized void setNode(int xP, int yP, int zP, int node) {
        chunkData[zP][yP][xP] = node;
        modificationBitfield |= getModBit(xP, yP, zP);
        modificationCount++;
    }

    @Override
    public void setNode(int xP, int yP, int zP, MapNode node) throws VxlPluginExecutionException {
        if (node instanceof MapNodeWithMetadata) {

            Object mdo = (((MapNodeWithMetadata) node).storeToMetadata());
            String decoder = (((MapNodeWithMetadata) node).getDecoderId());
            NodeMetadata md = new NodeMetadata(game, (MapNodeWithMetadata) node, decoder, mdo);
            synchronized (extendedNodes) {

                int key = toTreeMapKey(xP, yP, zP);

                extendedNodes.put(key, md);
                setNode(xP, yP, zP, -1);
            }
        } else {
            if (node.getId() == 0) {
                throw new VxlPluginExecutionException("Cannot store a node to a chunk if it hasn't been registered and received an ID");
            }
        }
    }

    private int toTreeMapKey(int xP, int yP, int zP) {
        return zP * 256 + yP * 16 + xP;
    }

    /**
     * Gets the numeric value of the node
     */
    private int getNodeVal(int xP, int yP, int zP) {
        return chunkData[zP][yP][xP];
    }

    @Override
    public synchronized MapNode getNode(int xP, int yP, int zP) throws VxlPluginExecutionException {
        int val = getNodeVal(xP, yP, zP);
        if (val != -1) {
            // standard node
            return resolver.resolveNode(val);
        } else {
            synchronized (extendedNodes) {
                NodeMetadata md = extendedNodes.get(toTreeMapKey(xP, yP, zP));
                return md.getNode();
            }
        }
    }

    @Override
    public int getX() {
        return xid;
    }

    @Override
    public int getY() {
        return yid;
    }

    @Override
    public int getZ() {
        return zid;
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

    @Override
    public Serializable put(String key, Serializable value) {
        return chunkMetadata.put(key, value);
    }

    @Override
    public Serializable get(String key) {
        return chunkMetadata.get(key);
    }

    /**
     * Serializes to a byte array
     */
    public synchronized byte[] serialize() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (DataOutputStream dos = new DataOutputStream(baos)) {
                dos.writeLong(modificationBitfield);
                dos.writeLong(modificationCount);
        /* * 0 <= x < 8; 0 <= y < 8
         * * 8 <= x < 16; 0 <= y < 8
         * * 0 <= x < 8; 8 <= y < 16
         * * 8 <= x < 16; 8 <= y < 16 */
                for (int z = 0; z < 16; z++) {
                    if ((modificationBitfield & (1L << (z * 4 + 0))) != 0) {
                        for (int y = 0; y < 8; y++) {
                            for (int x = 0; x < 8; x++) {
                                dos.writeInt(getNodeVal(x, y, z));
                            }
                        }
                    }
                    if ((modificationBitfield & (1L << (z * 4 + 1))) != 0) {
                        for (int y = 0; y < 8; y++) {
                            for (int x = 8; x < 16; x++) {
                                dos.writeInt(getNodeVal(x, y, z));
                            }
                        }
                    }
                    if ((modificationBitfield & (1L << (z * 4 + 2))) != 0) {
                        for (int y = 8; y < 16; y++) {
                            for (int x = 0; x < 8; x++) {
                                dos.writeInt(getNodeVal(x, y, z));
                            }
                        }
                    }
                    if ((modificationBitfield & (1L << (z * 4 + 3))) != 0) {
                        for (int y = 8; y < 16; y++) {
                            for (int x = 8; x < 16; x++) {
                                dos.writeInt(getNodeVal(x, y, z));
                            }
                        }
                    }
                }
                dos.writeShort(extendedNodes.size());
                for (Map.Entry<Integer, NodeMetadata> extNode : extendedNodes.entrySet()) {
                    dos.writeShort((short) (int) extNode.getKey());
                    SerializationSupport.scriptSerialize(dos, extNode.getValue());
                }
                SerializationSupport.scriptSerialize(dos, chunkMetadata);
                dos.flush();
                return baos.toByteArray();
            }
        }
    }

    public synchronized void deserialize(byte[] buf) throws ChunkCorruptionException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buf)) {
            try (DataInputStream dis = new DataInputStream(bais)) {
                modificationBitfield = dis.readLong();
                modificationCount = dis.readLong();
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 16; y++) {
                        for (int x = 0; x < 16; x++) {
                            chunkData[x][y][z] = 0;
                        }
                    }
                }
                /* * 0 <= x < 8; 0 <= y < 8
                 * * 8 <= x < 16; 0 <= y < 8
                 * * 0 <= x < 8; 8 <= y < 16
                 * * 8 <= x < 16; 8 <= y < 16 */
                for (int z = 0; z < 16; z++) {
                    if ((modificationBitfield & (1L << (z * 4 + 0))) != 0) {
                        for (int y = 0; y < 8; y++) {
                            for (int x = 0; x < 8; x++) {
                                chunkData[z][y][x] = dis.readInt();
                            }
                        }
                    }
                    if ((modificationBitfield & (1L << (z * 4 + 1))) != 0) {
                        for (int y = 0; y < 8; y++) {
                            for (int x = 8; x < 16; x++) {
                                chunkData[z][y][x] = dis.readInt();
                            }
                        }
                    }
                    if ((modificationBitfield & (1L << (z * 4 + 2))) != 0) {
                        for (int y = 8; y < 16; y++) {
                            for (int x = 0; x < 8; x++) {
                                chunkData[z][y][x] = dis.readInt();
                            }
                        }
                    }
                    if ((modificationBitfield & (1L << (z * 4 + 3))) != 0) {
                        for (int y = 8; y < 16; y++) {
                            for (int x = 8; x < 16; x++) {
                                chunkData[z][y][x] = dis.readInt();
                            }
                        }
                    }
                }
                extendedNodes.clear();
                int mapSz = dis.readShort();
                for (int i = 0; i < mapSz; i++) {
                    int key = dis.readShort();
                    Object deserialized;
                    try {
                        deserialized = SerializationSupport.scriptDeserialize(dis);

                    } catch (Exception e) {
                        throw new ChunkCorruptionException(e);
                    }
                    if (deserialized instanceof NodeMetadata || deserialized == null) {
                        extendedNodes.put(key, (NodeMetadata) deserialized);
                    } else
                        throw new ChunkCorruptionException("Failed to deserialize the chunk; expected a NodeMetadata but instead got: " +
                                deserialized.getClass().getName() +
                                ":" + deserialized.toString());
                }
                try {

                    Object chMeta = SerializationSupport.scriptDeserialize(dis);
                    if (chMeta instanceof Map) {
                        chunkMetadata = (Map<String, Serializable>) chMeta;
                    } else {
                        throw new ChunkCorruptionException("Failed to deserialize metadata map. Expected a map but got " + chMeta.getClass().getName());
                    }
                } catch (Exception e) {
                    throw new ChunkCorruptionException(e);
                }
            }
        } catch (IOException e) {

            throw new ChunkCorruptionException(e);
        }
    }

    int getMapGeneratorSetUsed() {
        return mapGeneratorSetUsed;
    }

    void setMapGeneratorSetUsed(int mapGeneratorSetUsed) {
        this.mapGeneratorSetUsed = mapGeneratorSetUsed;
    }

    public long getModificationBitfield() {
        return modificationBitfield;
    }

    private Map<String, Serializable> chunkMetadata = new HashMap<>();
}
