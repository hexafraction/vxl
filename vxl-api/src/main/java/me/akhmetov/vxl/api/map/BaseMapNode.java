package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.rendering.NodeAppearance;

/**
 * A base implementation of a {@link MapNode} useful for basic nodes (i.e. solid nodes such as dirt, bricks, etc). Physics code will be added here as a TODO.
 */
public class BaseMapNode extends MapNode{
    /**
     * Constructs a basic map node with the given name and appearance.
     */
    public BaseMapNode(String name, NodeAppearance appearance) {
        super(name, appearance);
    }
}
