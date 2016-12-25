package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.VxlPluginExecutionException;

/**
 * Provides support for interacting with mapnode registration, late binds, and metadata decoding.
 */
public interface NodeAPI {
    /**
     * Returns the metadata decoder with the specified ID.
     */
    INodeMetadataDecoder getMetadataDecoder(String id);

    /**
     * Registers a decoder that converts a deserialized node metadata object to the desired node
     * @param id The ID of the deocder, which should correspond with the return value of
     * {@link MapNodeWithMetadata#getDecoderId()} for the node.
     * @param decoder The implementation of the decoder itself.
     * @throws VxlPluginExecutionException
     */
    void registerNodeMetadataDecoder(String id, INodeMetadataDecoder decoder) throws VxlPluginExecutionException;

    /**
     * Registers a node, allowing it to be placed in the map. Nodes that are instances of {@link MapNodeWithMetadata}
     * cannot be registered this way since they are constructed as needed by using a metadata decoder.
     * @param nd The node.
     * @throws VxlPluginExecutionException
     */
    void registerNode(MapNode nd) throws VxlPluginExecutionException;

    /**
     * Registers a callback that resolves late-bound nodes.
     * @param pluginName The name of the plugin (e.g. "foo" to resolve late binds of the form "!foo:xyz"
     * @param resolver The callback to register.
     * @throws VxlPluginExecutionException
     */
    void registerLateBindResolver(String pluginName, ILateBindResolver resolver) throws VxlPluginExecutionException;

    /**
     * Returns the late bind resolver for the specified plugin name.
     */
    ILateBindResolver getLateBindResolver(String pluginName);
}
