package me.akhmetov.vxl.core;

import me.akhmetov.vxl.core.security.VxlPermission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class NodeResolutionTable implements Serializable {
    public NodeResolutionTable(GameState state) {
        System.getSecurityManager().checkPermission(new VxlPermission("corestate"));
        this.state = state;

    }

    private final transient GameState state;
    private final transient ConcurrentHashMap<String, MapNode> nodesByName = new ConcurrentHashMap<>();
    private final transient ConcurrentSkipListMap<Integer, MapNode> nodesById = new ConcurrentSkipListMap<>();
    private final HashMap<String, Integer> nodeIdsByName = new HashMap<>();
    int maxNode = 65536; // 0-65535 are reserved for hardcoded system nodes.

    public MapNode resolveNode(int id) {
        return nodesById.getOrDefault(id, createIdLinkedUnknownNode(id));
    }

    public MapNode resolveNode(String name) {
        return nodesByName.getOrDefault(name, createNameLinkedUnknownNode(name));
    }

    public synchronized void registerMapNode(MapNode nd) throws VxlPluginExecutionException {
        if (!nodeIdsByName.containsKey(nd.getName())) {
            if (!state.isAuthoritative()) {
                throw new VxlPluginExecutionException("Cannot register a node called " + nd.getName() +
                        " because it wasn't first registered on the authoritative server.");
            } else {
                int newVal = ++maxNode;
                nd.setId(newVal);
                nodeIdsByName.put(nd.getName(), nd.getId());
                nodesById.put(newVal, nd);
                nodesByName.put(nd.getName(), nd);
            }
        } else {
            // we know the node ID based on the name. Let's store it to that location.
            int id = nodeIdsByName.get(nd.getName());
            nodesByName.put(nd.getName(), nd);
            nodesById.put(id, nd);


        }

        UnknownNode unknownById = pendingUnknownNodes.get(nd.getId());
        if (unknownById != null) {
            unknownById.beginDelegating(nd);
            Logging.log(Logging.Severity.WARNING, "Resolved unknown node with ID "
                    + nd.getId()
                    + " as "
                    + nd.getName()
                    + ". Chunk behavior may be unusual until the chunk is reloaded.");
        }
        UnknownNode unknownByName = pendingUnknownNodesByName.get(nd.getName());
        if (unknownByName != null) {
            unknownByName.beginDelegating(nd);
        }

    }


    private synchronized MapNode createNameLinkedUnknownNode(String name) {
        Logging.log(Logging.Severity.WARNING, "Attempting to load an unknown node.");
        UnknownNode ourUnknown = new UnknownNode(name);
        pendingUnknownNodesByName.put(name, ourUnknown);
        return ourUnknown;
    }

    private final transient ConcurrentSkipListMap<Integer, UnknownNode> pendingUnknownNodes = new ConcurrentSkipListMap<>();
    private final transient ConcurrentHashMap<String, UnknownNode> pendingUnknownNodesByName = new ConcurrentHashMap<>();

    private synchronized MapNode createIdLinkedUnknownNode(int id) {
        Logging.log(Logging.Severity.WARNING, "Attempting to load an unknown node.");
        UnknownNode ourUnknown = new UnknownNode(id);
        // already protected by synchronized block
        pendingUnknownNodes.put(id, ourUnknown);
        return ourUnknown;
    }

    public class UnknownNode extends MapNode {

        UnknownNode(String name) {
            super(name);
        }

        UnknownNode(int id) {
            super("sys:unknown");
            setId(id);
        }

        MapNode delegate = null;

        void beginDelegating(MapNode delegate) {
            this.delegate = delegate;
        }

        // TODO when other methods are defined, they will delegate to default impl unless delegate is set, then to delegate

    }

}
