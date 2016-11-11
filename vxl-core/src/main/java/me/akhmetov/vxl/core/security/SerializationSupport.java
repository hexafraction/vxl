package me.akhmetov.vxl.core.security;

import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class SerializationSupport {
    private static final FSTConfiguration scriptSerializer;
    static {
        scriptSerializer = FSTConfiguration.createDefaultConfiguration();
        scriptSerializer.setVerifier(ClassVerifier::verifySerialization);
    }

    private SerializationSupport() {
    }

    public static byte[] scriptSerialize(Object o){
        return scriptSerializer.asByteArray(o);
    }

    public static Object scriptDeserialize(byte[] buf){
        return scriptSerializer.asObject(buf);
    }
    public static void scriptSerialize(OutputStream os, Object o) throws IOException {
        scriptSerializer.encodeToStream(os, o);
    }

    // suppressing because the serialization library throws Exception, not much else we can do
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public static Object scriptDeserialize(InputStream is) throws Exception {
        return scriptSerializer.decodeFromStream(is);
    }


    private static final FSTConfiguration appSerializer;
    static {
        appSerializer = FSTConfiguration.createDefaultConfiguration();
    }
    public static byte[] appSerialize(Object o){
        return appSerializer.asByteArray(o);
    }

    public static Object appDeserialize(byte[] buf){
        return appSerializer.asObject(buf);
    }
}
