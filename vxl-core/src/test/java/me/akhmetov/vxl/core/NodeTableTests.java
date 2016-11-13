package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.BaseMapNode;
import me.akhmetov.vxl.api.MapNode;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.core.security.SerializationSupport;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NodeTableTests {


    @Test
    public void testBasic() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1");
        NodeResolutionTable nrt = new NodeResolutionTable(new GameState(null, null, true));
        nrt.registerMapNode(nd1);
        assertTrue(nd1.getId()>65535);
        assertTrue(nrt.resolveNode(nd1.getId())==nd1);
        assertTrue(nrt.resolveNode("unittests:test1")==nd1);

    }

    @Test(expected= VxlPluginExecutionException.class)
    public void testNonAuthoritative() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1");
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
    public void testSerialization() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1");
        MapNode nd2 = new BaseMapNode("unittests:test2");
        NodeResolutionTable nrt = new NodeResolutionTable();
        nrt.setState(new GameState(null, null, true));
        nrt.registerMapNode(nd1);
        nrt.registerMapNode(nd2);
        byte[] buf = SerializationSupport.appSerialize(nrt);
        NodeResolutionTable nrt2 = (NodeResolutionTable) SerializationSupport.appDeserialize(buf);

        nrt.setState(new GameState(null, null, true));
        MapNode nd12 = new BaseMapNode("unittests:test1");
        MapNode nd3 = new BaseMapNode("unittests:test3");
        nrt.registerMapNode(nd12);
        nrt.registerMapNode(nd3);
        assertTrue(nd1.getId()==nd12.getId());
        assertTrue(nd2.getId()!=nd1.getId());
        assertTrue(nd3.getId()!=nd1.getId());
        assertTrue(nd2.getId()!=nd3.getId());
    }
    @Test
    public void testSerializationNonAuth() throws Exception {
        MapNode nd1 = new BaseMapNode("unittests:test1");
        MapNode nd2 = new BaseMapNode("unittests:test2");
        NodeResolutionTable nrt = new NodeResolutionTable();
        nrt.setState(new GameState(null, null, true));
        nrt.registerMapNode(nd1);
        nrt.registerMapNode(nd2);
        byte[] buf = SerializationSupport.appSerialize(nrt);
        NodeResolutionTable nrt2 = (NodeResolutionTable) SerializationSupport.appDeserialize(buf);

        nrt.setState(new GameState(null, null, false));
        MapNode nd12 = new BaseMapNode("unittests:test1");
        nrt.registerMapNode(nd12);
        assertTrue(nd1.getId()==nd12.getId());
        assertTrue(nd2.getId()!=nd1.getId());
    }
}
