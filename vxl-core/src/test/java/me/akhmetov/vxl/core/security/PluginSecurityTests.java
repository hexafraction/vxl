package me.akhmetov.vxl.core.security;

import me.akhmetov.vxl.core.GameState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@SuppressWarnings("ALL")
public class PluginSecurityTests {

    private VxlPluginClassLoader vcl;


    @Before
    public void setUp() throws Exception {

        vcl = new VxlPluginClassLoader(new IVxlClassProvider("localhost", 10333) {
            @Override
            byte[] getClass(String name) {
                try {

                    return Files.readAllBytes(new File("src/test/resources/sectest/vxlplugin/"+name.substring(10)+".class").toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new NoClassDefFoundError("Cannot read");
                }
            }
        });
    }

    @Test
    public void testFineIO() throws Exception {

        System.getSecurityManager().checkRead("foo");

    }

    @Test
    public void testFineSetSM() throws Exception {

        System.setSecurityManager(System.getSecurityManager());

    }

    @Test(expected = SecurityException.class)
    public void testBadIO() throws Exception {

        vcl.loadClass("vxlplugin.IOTest").newInstance();

    }

    @Test(expected = SecurityException.class)
    public void testBadSM() throws Exception {

        vcl.loadClass("vxlplugin.SMTest").newInstance();

    }


    @Test
    public void testBadSerialization() throws Exception {

        try {
            SerializationSupport.scriptSerialize(new GameState(null, null, false));
        } catch(RuntimeException e){
            Assert.assertTrue("Got the wrong RuntimeException: "+e.getMessage(), e.getMessage().startsWith("tried to deserialize forbidden class"));
            return;
        } Assert.fail("Expected a RuntimeException");

    }

    @Test(expected = SecurityException.class)
    public void testBadClassloader() throws Exception {

        vcl.loadClass("vxlplugin.CLTest").newInstance();

    }
}
