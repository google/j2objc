package java.net;

import java.io.IOException;

/**
 * J2ObjC-specific delegate of {@link java.net.URL}, to support the
 * separation of jre_net dependencies from jre_core. URL needs to be
 * core because core classes like ClassLoader reference URL publicly.
 */
interface URLDelegate {

  void initURL(URL url, URL context, String spec, Object handler) throws MalformedURLException;

  int getDefaultPort(URL url) throws MalformedURLException;

  Object getURLStreamHandler(String protocol);

  void setURLStreamHandlerFactory(Object factory);

  boolean equals(URL u1, URL u2) throws MalformedURLException;

  int hashCode(URL u) throws MalformedURLException;

  boolean sameFile(URL u1, URL u2) throws MalformedURLException;

  String toExternalForm(URL u) throws MalformedURLException;

  URLConnection openConnection(URL u) throws IOException;

  URLConnection openConnection(URL url, Object proxy) throws IOException;
}
