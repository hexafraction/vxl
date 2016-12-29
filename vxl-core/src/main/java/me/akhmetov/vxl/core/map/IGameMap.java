package me.akhmetov.vxl.core.map;

/**
 * Provides access to the game map. Not for script use.
 */
public interface IGameMap {
    MapChunkImpl getChunk(int x, int y, int z);
    boolean isChunkGenerated(int x, int y, int z);
    boolean isChunkCached(int x, int y, int z);
}
