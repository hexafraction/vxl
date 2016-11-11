package me.akhmetov.vxl.core.security;

import java.security.*;

public class VxlPluginClassLoader extends ClassLoader {
    static {
        Policy.setPolicy(new VxlSecurityPolicy());
        System.setSecurityManager(new SecurityManager());
    }
    private final IVxlClassProvider provider;

    private final ProtectionDomain pd;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public VxlPluginClassLoader(IVxlClassProvider provider) {
        this.provider = provider;
        Permissions permissions = new Permissions();
        // NO PERMISSIONS ARE ADDED BY DEFAULT
        // This *technically* leaks the instance in the constructor but the ProtectionDomain isn't allowed to leak.
        //noinspection ThisEscapedInObjectConstruction
        this.pd = new ProtectionDomain(provider.getCodeSource(), permissions, this, null);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(!name.startsWith("vxlplugin.")){
            throw new ClassNotFoundException("Plugin classes must reside within a subpackage of vxlplugin.");
        }
        byte[] classDef = provider.getClass(name);
        return defineClass(name, classDef, 0, classDef.length, pd);
    }
}
