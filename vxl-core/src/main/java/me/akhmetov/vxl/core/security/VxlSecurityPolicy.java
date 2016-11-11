package me.akhmetov.vxl.core.security;

import java.security.*;

public class VxlSecurityPolicy extends Policy {
    private final Permissions appPermissions = new Permissions();
    private final Permissions pluginPermissions = new Permissions();

    public VxlSecurityPolicy() {
        appPermissions.add(new AllPermission());
    }


    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        //System.out.println("cs:"+codesource.toString());
        return new Permissions();
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        //System.out.println("cl:"+domain.getClassLoader().toString());
        return isPlugin(domain)?pluginPermissions:appPermissions;
    }

    private boolean isPlugin(ProtectionDomain pd){
        return pd.getClassLoader() instanceof VxlPluginClassLoader;
    }


}
