package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.rendering.NodeAppearance;

/**
 * Represents a <em>type</em> of node. For example, in a world containing only dirt, air, and stone, there would be
 * two instances of MapNode constructed by scripts (dirt and stone), no matter how many actual nodes of that type exist
 * in the world map.
 * <p>
 * Once a node is constructed, it must be registered with the game's NodeResolutionTable unless it subclasses MapNodeWithMetadata.
 * <p>
 * The ID of a node is a numeric int value used for serialization. Plugin code should not change it.
 * The name of a node is a textual identifier for the node, not intended to be seen by the player except in debugging tools.
 * The format of the name should be of the form x:y where x is the name of the plugin, and y is the name of the node.
 * X should contain only underscores and alphanumerics (matching the plugin package) while Y may contain alphanumerics,
 * underscores, dots, and square bracket characters.
 * For example, the plugin in Java package vxlplugin.default defines a node representing "Dirt with grass" with name "default:dirt_with_grass".
 * <p>
 * An exception is made for so-called late bound nodes, where the name is prefixed with the character '!'.
 * These nodes are created by a script callback when needed, but they may be created and registered directly by a plugin.
 */
public abstract class MapNode {


    // 0 is invalid, but the value will be set appropriately during loading.
    private int id = 0;
    private NodeAppearance appearance;

    /**
     * Constructs a basic map node with the given name.
     *
     * @param name
     */
    protected MapNode(String name, NodeAppearance appearance) {
        this.name = name;
        this.appearance = appearance;
    }


    /**
     * Sets the node ID used for serialization. Plugin code should not call this method.
     *
     * @param id The ID to set.
     */
    public final void setId(int id) {
        this.id = id;
    }

    private final String name;


    public final String getName() {
        return name;
    }

    public final int getId() {
        return id;
    }

    public NodeAppearance getAppearance(){

        return appearance;
    }
}
