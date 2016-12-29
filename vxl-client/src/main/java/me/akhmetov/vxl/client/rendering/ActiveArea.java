package me.akhmetov.vxl.client.rendering;

import me.akhmetov.vxl.api.map.MapChunk;

public interface ActiveArea {
    Iterable<MapChunk> getActives();

    void evictOne();

    void addChunk(MapChunk chk);
}
