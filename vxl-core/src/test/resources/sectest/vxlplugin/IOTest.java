package vxlplugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Policy;

public class IOTest {
public IOTest() throws Exception{
Files.readAllBytes(new File("src/test/resources/sectest/flag.txt").toPath());
}
}
