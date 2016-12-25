package me.akhmetov.vxl.api.map;

/**
 * A base implementation of a {@link MapNode} useful for basic nodes (i.e. solid nodes such as dirt, bricks, etc);
 */
public class BaseMapNode extends MapNode{
    /**
     * Constructs a basic map node with the given name.
     *
     * @param name
     */
    public BaseMapNode(String name) {
        super(name);
    }
}
