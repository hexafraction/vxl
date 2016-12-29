package me.akhmetov.vxl.core.map;

import me.akhmetov.vxl.api.map.MapNode;

public class MapChunkDelta {
    public MapChunkDelta(int x, int y, int z, int chX, int chY, int chZ, MapNode before, MapNode after) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.chX = chX;
        this.chY = chY;
        this.chZ = chZ;
        this.before = before;
        this.after = after;
    }

    public enum Type {
        ADD, DROP, CHANGE
    }
    final int x, y, z;
    final int chX, chY, chZ;
    final MapNode before, after;

}
