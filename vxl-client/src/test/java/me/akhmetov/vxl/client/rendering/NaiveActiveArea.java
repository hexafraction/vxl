package me.akhmetov.vxl.client.rendering;

import me.akhmetov.vxl.api.map.MapChunk;

import java.util.Collection;
import java.util.HashSet;

public class NaiveActiveArea implements ActiveArea {
    HashSet<MapChunk> chunks;
    @Override
    public Iterable<MapChunk> getActives(){
        return chunks;
    }
    @Override
    public void evictOne(){
        throw new UnsupportedOperationException("Naive LAM can't evict stuff.");
    }
    @Override
    public void addChunk(MapChunk chk){
        chunks.add(chk);
    }

}
