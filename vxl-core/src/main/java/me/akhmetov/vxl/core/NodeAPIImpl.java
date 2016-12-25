package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.map.ILateBindResolver;
import me.akhmetov.vxl.api.map.INodeMetadataDecoder;
import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.api.map.NodeAPI;

import java.util.concurrent.ConcurrentHashMap;

public class NodeAPIImpl implements NodeAPI {
    private final ConcurrentHashMap<String, INodeMetadataDecoder> mdDecoders = new ConcurrentHashMap<>();
    private final NodeResolutionTable nodeResolutionTable;
    private final ConcurrentHashMap<String, ILateBindResolver> lateBindResolvers = new ConcurrentHashMap<>();
    public NodeAPIImpl(NodeResolutionTable nodeResolutionTable) {
        this.nodeResolutionTable = nodeResolutionTable;
    }

    @Override
    public INodeMetadataDecoder getMetadataDecoder(String id) {
        return mdDecoders.get(id);
    }

    @Override
    public void registerNodeMetadataDecoder(String id, INodeMetadataDecoder decoder) throws VxlPluginExecutionException {
        if (mdDecoders.putIfAbsent(id, decoder) != null)
           throw new VxlPluginExecutionException("A node metadata decoder called '" + id + "' has already been registered.");
    }

    @Override
    public void registerNode(MapNode nd) throws VxlPluginExecutionException {
        nodeResolutionTable.registerMapNode(nd);
    }

    @Override
    public void registerLateBindResolver(String pluginName, ILateBindResolver resolver) throws VxlPluginExecutionException {
        lateBindResolvers.put(pluginName, resolver);
    }

    @Override
    public ILateBindResolver getLateBindResolver(String pluginName) {
        return lateBindResolvers.get(pluginName);
    }
}
