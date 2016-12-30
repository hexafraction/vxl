package me.akhmetov.vxl.core.map;

import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.map.MapNodeWithMetadata;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.core.GameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class NodeResolutionTable implements Serializable {

    public NodeResolutionTable() {
        buildHardcodedNodes();
    }

    private void buildHardcodedNodes() {
        HardcodedNodes.AIR.setId(1);
        nodesById.put(1, HardcodedNodes.AIR);
        nodeNamesById.put(1, HardcodedNodes.AIR.getName());
        nodeIdsByName.put(HardcodedNodes.AIR.getName(), 1);
        nodesByName.put(HardcodedNodes.AIR.getName(), HardcodedNodes.AIR);
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public NodeResolutionTable(GameState state) {
        this();
        this.state = state;
    }
    private static Logger logger = LogManager.getLogger();
    private transient GameState state;
    private final transient HashMap<String, MapNode> nodesByName = new HashMap<>();
    private final transient TreeMap<Integer, MapNode> nodesById = new TreeMap<>();
    // In lieu of HashBiMap. Only used in synchronized blocks.
    private final HashMap<String, Integer> nodeIdsByName = new HashMap<>();
    private transient TreeMap<Integer, String> nodeNamesById = new TreeMap<>();
    private int maxNodePresent = 65536; // 0-65535 are reserved for hardcoded system nodes.

    public void rebuildTreeMap(){
        for(Map.Entry<String, Integer> e : nodeIdsByName.entrySet()){
            nodeNamesById = new TreeMap<>();
            nodeNamesById.put(e.getValue(), e.getKey());
        }
    }

    public synchronized MapNode resolveNode(int id) throws VxlPluginExecutionException {
        return nodesById.getOrDefault(id, createIdLinkedUnknownNodeOrLateBind(id));
    }

    public synchronized MapNode resolveNode(String name) throws VxlPluginExecutionException {
        return nodesByName.getOrDefault(name, name.startsWith("!")?resolveLateBoundNode(name):createNameLinkedUnknownNode(name));
    }

    public synchronized void registerMapNode(MapNode nd) throws VxlPluginExecutionException {
        if(nd instanceof MapNodeWithMetadata) throw new VxlPluginExecutionException("Cannot register a node with metadata.");
        String name = nd.getName();
        if (!nodeIdsByName.containsKey(name)) {
            if (!state.isAuthoritative()) {
                throw new VxlPluginExecutionException("Cannot register a node called " + name +
                        " because it wasn't first registered on the authoritative server.");
            } else {
                int newVal = ++maxNodePresent;
                nd.setId(newVal);
                nodeIdsByName.put(name, newVal);
                nodeNamesById.put(newVal, name);
                nodesById.put(newVal, nd);
                nodesByName.put(name, nd);
            }
        } else {
            // we know the node ID based on the name. Let's store it to that location.
            int id = nodeIdsByName.get(name);
            nd.setId(id);
            nodesByName.put(name, nd);
            nodesById.put(id, nd);


        }

        UnknownNode unknownById = pendingUnknownNodes.get(nd.getId());
        if (unknownById != null) {
            unknownById.beginDelegating(nd);
            logger.warn("Resolved unknown node with ID "
                    + nd.getId()
                    + " as "
                    + name
                    + ". Chunk behavior may be unusual until the chunk is reloaded.");
        }
        UnknownNode unknownByName = pendingUnknownNodesByName.get(name);
        if (unknownByName != null) {
            unknownByName.beginDelegating(nd);
        }

    }


    private synchronized MapNode createNameLinkedUnknownNode(String name) {
        logger.warn("Attempting to load an unknown node.");
        UnknownNode ourUnknown = new UnknownNode(name);
        pendingUnknownNodesByName.put(name, ourUnknown);
        return ourUnknown;
    }

    private final transient ConcurrentSkipListMap<Integer, UnknownNode> pendingUnknownNodes = new ConcurrentSkipListMap<>();
    private final transient ConcurrentHashMap<String, UnknownNode> pendingUnknownNodesByName = new ConcurrentHashMap<>();

    private synchronized MapNode createIdLinkedUnknownNodeOrLateBind(int id) throws VxlPluginExecutionException {
        String name = nodeNamesById.get(id);
        if(name!=null){
            if(name.startsWith("!")){
                return resolveLateBoundNode(name);
            }
        }
        logger.warn("Attempting to load an unknown node.");
        UnknownNode ourUnknown = new UnknownNode(id);
        // already protected by synchronized block
        pendingUnknownNodes.put(id, ourUnknown);
        return ourUnknown;
    }

    private synchronized MapNode resolveLateBoundNode(String name) throws VxlPluginExecutionException {
        if(!name.startsWith("!")) throw new IllegalArgumentException("The name must start with '!'");
        if(!name.contains(":")) throw new IllegalArgumentException("The name must contain a colon separator");
        MapNode nd = state.getNodeAPI().getLateBindResolver(name.substring(1, name.indexOf(':'))).resolve(name);
        registerMapNode(nd);
        return nd;
    }

    public class UnknownNode extends MapNode {
        // FIXME
        UnknownNode(String name) {
            super(name, null);
        }

        UnknownNode(int id) {
            super("sys:unknown", null);
            setId(id);
        }

        MapNode delegate = null;

        void beginDelegating(MapNode delegate) {
            this.delegate = delegate;
        }

        // TODO when other methods are defined, they will delegate to default impl unless delegate is set, then to delegate

    }

}
