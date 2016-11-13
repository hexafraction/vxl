package me.akhmetov.vxl.core;

/**
 * Provides access to the game map.
 */
interface IGameMap {
    MapChunk getChunk(int x, int y, int z);
    boolean isChunkGenerated(int x, int y, int z);
    boolean isChunkCached(int x, int y, int z);
}
