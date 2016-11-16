package me.akhmetov.vxl.api;

/**
 * Generates the content of a map chunk. This method must perform the same actions for the same map chunk coordinates,
 * even between application startups.
 */
public interface IMapGenerator {
    /**
     * Generates content in the chunk
     * @param mc The chunk whose content should be edited
     * @param x The X coordinate of the chunk.
     * @param y The Y coordinate of the chunk.
     * @param z The Z coordinate of the chunk.
     * @param mapSeed The numeric "seed" for the map. The generator should vary any random results based on the seed.
     */
    void generate(MapChunk mc, int x, int y, int z, long mapSeed);
}
