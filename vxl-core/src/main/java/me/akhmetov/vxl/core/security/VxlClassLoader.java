package me.akhmetov.vxl.core.security;

import java.security.CodeSource;
import java.security.Permissions;
import java.security.Principal;
import java.security.ProtectionDomain;

public class VxlClassLoader extends ClassLoader {
    private final IVxlClassProvider provider;

    private final ProtectionDomain pd;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public VxlClassLoader(IVxlClassProvider provider) {
        this.provider = provider;
        Permissions permissions = new Permissions();
        // NO PERMISSIONS ARE ADDED BY DEFAULT
        // This *technically* leaks the instance in the constructor but the ProtectionDomain isn't allowed to leak.
        this.pd = new ProtectionDomain(provider.getCodeSource(), permissions, this, null);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(!name.startsWith("vxlplugin.")){
            throw new ClassNotFoundException("Plugin classes must reside within a subpackage of vxkplugin.");
        }
        byte[] classDef = provider.getClass(name);
        Class<?> clazz = defineClass(name, classDef, 0, classDef.length, pd);
        return clazz;
    }
}
