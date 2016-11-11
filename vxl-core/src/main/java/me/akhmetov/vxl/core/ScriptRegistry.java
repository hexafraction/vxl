package me.akhmetov.vxl.core;

import java.util.concurrent.ConcurrentHashMap;

public class ScriptRegistry {
    private final ConcurrentHashMap<String, INodeMetadataDecoder> mdDecoders = new ConcurrentHashMap<>();

    public INodeMetadataDecoder getMetadataDecoder(String id) {
        return mdDecoders.get(id);
    }

    public void registerNodeMetadataDecoder(String id, INodeMetadataDecoder decoder) throws VxlPluginExecutionException {
        if (mdDecoders.putIfAbsent(id, decoder) != null)
           throw new VxlPluginExecutionException("A node metadata decoder called '" + id + "' has already been registered.");
    }
}
