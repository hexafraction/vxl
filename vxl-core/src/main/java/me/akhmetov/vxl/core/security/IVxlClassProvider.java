package me.akhmetov.vxl.core.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;

public abstract class IVxlClassProvider {
    abstract byte[] getClass(String name);

    final String host;
    final int port;

    public IVxlClassProvider(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public final CodeSource getCodeSource(){
        return null; // TODO reevaluate
    }
}
