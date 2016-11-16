package me.akhmetov.vxl.api;

/**
 * Provides functionality to interact with the map data and map generation functionality.
 * <h2>Coordinates:</h2>
 * The map uses 3D cartesian coordinates. Positive X is north, positive Y is east, positive Z is up.
 * For node coordinates, 1 unit is 1 meter.
 * For chunk coordinates, 1 unit is 16 meters.
 * The maximum map size is 2^29 (roughly 500mln) nodes in each direction from the origin, corresponding to
 * a total map size of (64M chunks)^3.
 * A chunk with chunk coordinates (0, 1, 2) spans a cube with corners having node coordinates (0, 16, 32) to (15, 31, 47).
 * <p>
 * Map generators:
 * Map generators are callbacks that are passed an instance of a {@link MapChunk} object. They must be registered by
 * name AND version. For compatibility, if a map generator of some name and version is ever registered, it MUST
 * be registered again in every future version of the plugin (because ir will be used to reconstruct a compressed
 * chunk that was originally generated with it)
 */
public interface MapAPI {
    /**
     * Returns whether a chunk was ever generated.
     *
     * @param x Chunk ID x
     * @param y Chunk ID y
     * @param z Chunk ID z
     * @return True if it was ever generated, false otherwise.
     */
    boolean isChunkGenerated(int x, int y, int z);

    /**
     * Returns whether a chunk is currently in the in-memory cache.
     *
     * @param x Chunk ID x
     * @param y Chunk ID y
     * @param z Chunk ID z
     * @return True if it is in the cache and need not be loaded from disk and deserialized.
     */
    boolean isChunkCached(int x, int y, int z);

    /**
     * Returns whether a chunk is currently in memory and actively eligible to have callbacks called on it.
     * Chunks are active when players are within or nearby, or while a plugin has it loaded.
     *
     * @param x Chunk ID x
     * @param y Chunk ID y
     * @param z Chunk ID z
     * @return True if it is loaded.
     */
    boolean isChunkLoaded(int x, int y, int z);

    /**
     * Loads the chunk into memory if not cached, and makes it active.
     *
     * @param x Chunk ID x
     * @param y Chunk ID y
     * @param z Chunk ID z
     */
    void loadChunk(int x, int y, int z);

    /**
     * Gets the node at a specified location.
     *
     * @param x               The X node coordinate
     * @param y               The Y node coordinate
     * @param z               The Z node coordinate
     * @param loadIfNecessary Whether the chunk should be loaded if it is not cached or loaded at this time.
     * @return the mapNode at a given location, or null if loadIfNecessary is false and the chunk is not loaded or cached.
     */
    MapNode getNode(int x, int y, int z, boolean loadIfNecessary);

    /**
     * Sets the node at a specified location.
     *
     * @param nd The node to put into the map.
     * @param x               The X node coordinate
     * @param y               The Y node coordinate
     * @param z               The Z node coordinate
     * @param loadIfNecessary Whether the chunk should be loaded if it is not cached or loaded at this time.
     * @return true if the node was set, or false if loadIfNecessary is false and the chunk is not loaded or cached.
     */
    boolean setNode(MapNode nd, int x, int y, int z, boolean loadIfNecessary);

    /**
     * Gets an entire chunk, for example to interact with its metadata.
     * @param x               The X chunk coordinate
     * @param y               The Y chunk coordinate
     * @param z               The Z chunk coordinate
     * @param loadIfNecessary Whether the chunk should be loaded if it is not cached or loaded at this time.
     */
    MapChunk getChunk(int x, int y, int z, boolean loadIfNecessary);

    /**
     * Registers a map generator with the indicated ID and version. Calling this method does not
     * cause it to be invoked for map generation of new chunks. The set of map generators used to generate
     * new chunks is created by using
     *
     * @param id      A string, containing only alphanumerics, indicating the name of the map generator.
     * @param version A numeric version
     */
    void registerMapGenerator(IMapGenerator mg, String id, int version);

    /**
     * Adds a generator to the set of those called during generation of new chunks.
     *
     * @param id      The string ID of the generator.
     * @param version The version of the generator to use.
     * @param stage   The stage at which the generator is to be invoked.
     */
    void activateMapGenerator(String id, int version, MapGenerationStage stage);

    /**
     * Enumerates the steps of map generation.
     */
    public enum MapGenerationStage {
        /**
         * Generation of the rough terrain (i.e. elevation of ground)
         */
        BASE,
        /**
         * Generation of caves.
         */
        CAVES,
        /**
         * Generation of liquids
         */
        LIQUIDS,
        /**
         * Generation of resources such as ores
         */
        ORES,
        /**
         * Generation of surface-level items such as trees
         */
        SURFACE

    }

}
