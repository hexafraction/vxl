package me.akhmetov.vxl.core.security;

public final class SecurityUtils {
    public static void checkConstructException() {
        System.getSecurityManager().checkPermission(new VxlPermission("coreexception"));
    }
    public static void checkLibgdxLowLevel() {
        System.getSecurityManager().checkPermission(new VxlPermission("gdx"));
    }
    private SecurityUtils(){}
}
