package me.akhmetov.vxl.core.map;

import me.akhmetov.vxl.api.map.MapNode;

public class MapChunkDelta {
    public MapChunkDelta(Type type, int x, int y, int z, MapNode before, MapNode after) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.before = before;
        this.after = after;
    }

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public MapNode getBefore() {
        return before;
    }

    public MapNode getAfter() {
        return after;
    }

    public enum Type {
        ADD, DROP, CHANGE
    }
    final int x, y, z;
    final MapNode before, after;
    final Type type;
}
