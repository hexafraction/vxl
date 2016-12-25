package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.map.MapNode;

/**
 * Used to resolve a node with a name starting with '!' that hasn't been registered already.
 */
public interface ILateBindResolver {
    /**
     * Resolves the node.
     * @param name The node name, containing the '!'
     * @return The node that should appear to players.
     */
    MapNode resolve(String name);
}
