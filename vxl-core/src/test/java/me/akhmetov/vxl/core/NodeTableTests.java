package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.map.BaseMapNode;
import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.core.map.NodeResolutionTable;
import me.akhmetov.vxl.core.security.SerializationSupport;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NodeTableTests {


    @Test
    public void testBasic() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1", null);
        NodeResolutionTable nrt = new NodeResolutionTable(new GameState(null, null, true));
        nrt.registerMapNode(nd1);
        assertTrue(nd1.getId()>65535);
        //noinspection ObjectEquality
        assertTrue(nrt.resolveNode(nd1.getId())==nd1);
        //noinspection ObjectEquality
        assertTrue(nrt.resolveNode("unittests:test1")==nd1);

    }

    @Test(expected= VxlPluginExecutionException.class)
    public void testNonAuthoritative() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1", null);
        NodeResolutionTable nrt = new NodeResolutionTable(new GameState(null, null, false));
        nrt.registerMapNode(nd1);
    }

    @Test
    public void testUnknowns() throws Exception {
        NodeResolutionTable nrt = new NodeResolutionTable(new GameState(null, null, true));
        assertTrue(nrt.resolveNode(1444) instanceof NodeResolutionTable.UnknownNode);
        assertTrue(nrt.resolveNode("unittests:test1") instanceof NodeResolutionTable.UnknownNode);

    }

    @Test
    public void testSerializationPerf() throws Exception {
        for(int i = 0; i < 100; i++){
            iterateSerPerf();
        }
        long nanoStart = System.nanoTime();
        for(int i = 0; i < 1000; i++){
            iterateSerPerf();
        }
        long nanoEnd = System.nanoTime();
        System.out.println("nanos per cycle: " + (nanoEnd-nanoStart) / 1000.0);
        System.out.println("serializations per second: " + 1000000000/((nanoEnd-nanoStart) / 1000.0));
        iterateSerSize();

    }

    private void iterateSerPerf() throws VxlPluginExecutionException {

        NodeResolutionTable nrt = new NodeResolutionTable();
        nrt.setState(new GameState(null, null, true));
        for(int i = 0; i < 10000; i++){
            MapNode nd = new BaseMapNode("unittests:"+Integer.toString(i), null);
            nrt.registerMapNode(nd);
        }

        byte[] buf = SerializationSupport.appSerialize(nrt);

        NodeResolutionTable nrt2 = (NodeResolutionTable) SerializationSupport.appDeserialize(buf);
        nrt2.rebuildTreeMap();
        nrt.setState(new GameState(null, null, true));
    }

    private void iterateSerSize() throws VxlPluginExecutionException {

        NodeResolutionTable nrt = new NodeResolutionTable();
        nrt.setState(new GameState(null, null, true));
        for(int i = 0; i < 10000; i++){
            MapNode nd = new BaseMapNode("unittests:"+Integer.toString(i), null);
            nrt.registerMapNode(nd);
        }

        byte[] buf = SerializationSupport.appSerialize(nrt);
        System.out.println("Size of serialization for 10000 nodes: "+buf.length);
    }

    @Test
    public void testSerialization() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1", null);
        MapNode nd2 = new BaseMapNode("unittests:test2", null);
        NodeResolutionTable nrt = new NodeResolutionTable();
        nrt.setState(new GameState(null, null, true));
        nrt.registerMapNode(nd1);
        nrt.registerMapNode(nd2);
        byte[] buf = SerializationSupport.appSerialize(nrt);
        NodeResolutionTable nrt2 = (NodeResolutionTable) SerializationSupport.appDeserialize(buf);
        nrt2.rebuildTreeMap();
        nrt.setState(new GameState(null, null, true));
        MapNode nd12 = new BaseMapNode("unittests:test1", null);
        MapNode nd3 = new BaseMapNode("unittests:test3", null);
        nrt.registerMapNode(nd12);
        nrt.registerMapNode(nd3);
        assertTrue(nd1.getId()==nd12.getId());
        assertTrue(nd2.getId()!=nd1.getId());
        assertTrue(nd3.getId()!=nd1.getId());
        assertTrue(nd2.getId()!=nd3.getId());
    }
    @Test
    public void testSerializationNonAuth() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1", null);
        MapNode nd2 = new BaseMapNode("unittests:test2", null);
        NodeResolutionTable nrt = new NodeResolutionTable();
        nrt.setState(new GameState(null, null, true));
        nrt.registerMapNode(nd1);
        nrt.registerMapNode(nd2);
        byte[] buf = SerializationSupport.appSerialize(nrt);
        NodeResolutionTable nrt2 = (NodeResolutionTable) SerializationSupport.appDeserialize(buf);

        nrt.setState(new GameState(null, null, false));
        MapNode nd12 = new BaseMapNode("unittests:test1", null);
        nrt.registerMapNode(nd12);
        assertTrue(nd1.getId()==nd12.getId());
        assertTrue(nd2.getId()!=nd1.getId());
    }
}
