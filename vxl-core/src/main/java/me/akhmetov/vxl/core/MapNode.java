package me.akhmetov.vxl.core;

/**
 * Represents a <em>type</em> of node. For example, in a world containing only dirt, air, and stone, there would be
 * two instances of MapNode constructed by scripts (dirt and stone), no matter how many actual nodes of that type exist
 * in the world map.
 *
 * Once a node is constructed, it must be registered with the game's NodeResolutionTable.
 */
public abstract class MapNode {
    // 0 is invalid
    private int id = 0;

    protected MapNode(String name) {
        this.name = name;
    }


    final void setId(int id){
        this.id = id;
    }

    private final String name;

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    // TODOs get appearance, motion/kinematics callbacks, interact callbacks, custom callbacks

}
