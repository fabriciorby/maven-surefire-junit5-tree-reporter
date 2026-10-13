package org.apache.maven.plugin.surefire.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Like the class loader surefire passes to clone(), which declares its own addURL.
 */
public class TargetClassLoader extends URLClassLoader {
    public TargetClassLoader(URL... urls) {
        super(urls, null);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
