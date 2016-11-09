package me.akhmetov.vxl.core.security;

import org.nustaq.serialization.FSTConfiguration;

public class SerializationSupport {
    private static final FSTConfiguration metadataSerializer;
    static {
        metadataSerializer = FSTConfiguration.createDefaultConfiguration();
        metadataSerializer.setVerifier(ClassVerifier::verifySerialization);
    }

}
