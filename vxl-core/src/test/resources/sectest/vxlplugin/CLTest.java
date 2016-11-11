package vxlplugin;

public class CLTest {
    public CLTest() throws ClassNotFoundException {
//Files.readAllBytes(new File("src/test/resources/sectest/flag.txt").toPath());
        ClassLoader cl = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                throw new ClassNotFoundException();
            }
        };
        cl.loadClass("foo");
    }
}
