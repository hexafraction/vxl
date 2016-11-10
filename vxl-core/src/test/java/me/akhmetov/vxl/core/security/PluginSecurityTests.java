package me.akhmetov.vxl.core.security;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Policy;

public class PluginSecurityTests {

    private VxlClassLoader vcl;

    @Before
    public void setUp() throws Exception {
        Policy.setPolicy(new VxlSecurityPolicy());
        System.setSecurityManager(new SecurityManager());
        vcl = new VxlClassLoader(new IVxlClassProvider("localhost", 10333) {
            @Override
            byte[] getClass(String name) {
                try {

                    return Files.readAllBytes(new File("src/test/resources/sectest/vxlplugin/IOTest.class").toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new NoClassDefFoundError("Cannot read");
                }
            }
        });
    }

    @Test
    public void testFine() throws Exception {

        System.getSecurityManager().checkRead("foo");

    }

    @Test(expected = SecurityException.class)
    public void testBad() throws Exception {

        vcl.loadClass("vxlplugin.IOTest").newInstance();

    }
}
