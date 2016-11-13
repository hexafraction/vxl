package me.akhmetov.vxl.core.security;

public final class SecurityUtils {
    public static void checkConstructException() {
        System.getSecurityManager().checkPermission(new VxlPermission("coreexception"));
    }
    private SecurityUtils(){}
}
