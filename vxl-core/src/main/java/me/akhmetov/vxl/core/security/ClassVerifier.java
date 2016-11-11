package me.akhmetov.vxl.core.security;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public final class ClassVerifier {
    private ClassVerifier() {
    }

    public static <T> boolean verifySerialization(Class<T> clazz){
        if(clazz.getPackage().getName().startsWith("me.akhmetov.vxl.core.security")){
            // NOOOOOO
            return false;
        }
        if(clazz.getPackage().getName().startsWith("java.")) {
            // we use the security manager at the
            // classloader level to handle these things.
            return true;
        }
        if(clazz.getPackage().getName().startsWith("vxlplugin.")){
            // is a class instantiated by a VXL plugin
            return true;
        }
        ScriptMaySerialize safeAnnotation = clazz.getAnnotation(ScriptMaySerialize.class);
        return safeAnnotation != null;

    }
}
