package vxlplugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Policy;

public class SMTest {
public SMTest(){
//Files.readAllBytes(new File("src/test/resources/sectest/flag.txt").toPath());
System.setSecurityManager(System.getSecurityManager());
}
}
