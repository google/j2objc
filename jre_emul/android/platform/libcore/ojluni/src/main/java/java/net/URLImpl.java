/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.net;

import com.google.j2objc.LibraryNotLinkedError;
import com.google.j2objc.net.IosHttpHandler;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * J2ObjC-specific delegate of {@link java.net.URL}, to support the separation
 * of jre_net dependencies from jre_core. URL needs to be core because core
 * classes like ClassLoader reference URL publicly.
 */
public final class URLImpl implements URLDelegate {

  /**
   * The property which specifies the package prefix list to be scanned for
   * protocol handlers. The value of this property (if any) should be a vertical
   * bar delimited list of package names to search through for a protocol
   * handler to load. The policy of this class is that all protocol handlers
   * will be in a class called <protocolname>.Handler, and each package in the
   * list is examined in turn for a matching handler. If none are found (or the
   * property is not specified), the default package prefix,
   * sun.net.www.protocol, is used. The search proceeds from the first package
   * in the list to the last and stops when a match is found.
   */
  private static final String PROTOCOL_PATH_PROP = "java.protocol.handler.pkgs";

  public void initURL(URL url, URL context, String spec, Object handler)
      throws MalformedURLException {
    String original = spec;
    int i, limit, c;
    int start = 0;
    String newProtocol = null;
    boolean aRef = false;
    boolean isRelative = false;

    try {
      limit = spec.length();
      while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) {
        limit--; // eliminate trailing whitespace
      }
      while ((start < limit) && (spec.charAt(start) <= ' ')) {
        start++; // eliminate leading whitespace
      }

      if (spec.regionMatches(true, start, "url:", 0, 4)) {
        start += 4;
      }
      if (start < spec.length() && spec.charAt(start) == '#') {
        /*
         * we're assuming this is a ref relative to the context URL. This means
         * protocols cannot start w/ '#', but we must parse ref URL's like:
         * "hello:there" w/ a ':' in them.
         */
        aRef = true;
      }
      for (i = start; !aRef && (i < limit) && ((c = spec.charAt(i)) != '/'); i++) {
        if (c == ':') {
          String s = spec.substring(start, i).toLowerCase();
          if (isValidProtocol(s)) {
            newProtocol = s;
            start = i + 1;
          }
          break;
        }
      }

      // Only use our context if the protocols match.
      url.setProtocolByDelegate(newProtocol);
      if ((context != null)
          && ((newProtocol == null) || newProtocol.equalsIgnoreCase(context.getProtocol()))) {
        // inherit the protocol handler from the context
        // if not specified to the constructor
        if (handler == null) {
          handler = context.getHandler();
        }

        // If the context is a hierarchical URL scheme and the spec
        // contains a matching scheme then maintain backwards
        // compatibility and treat it as if the spec didn't contain
        // the scheme; see 5.2.3 of RFC2396
        if (context.getPath() != null && context.getPath().startsWith("/")) {
          newProtocol = null;
        }

        if (newProtocol == null) {
          url.setProtocolByDelegate(context.getProtocol());
          url.setAuthorityByDelegate(context.getAuthority());
          url.setUserInfoByDelegate(context.getUserInfo());
          url.setHostByDelegate(context.getHost());
          url.setPortByDelegate(context.getPort());
          url.setFileByDelegate(context.getFile());
          url.setPathByDelegate(context.getPath());
          isRelative = true;
        }
      }

      String protocol = url.getProtocol();
      if (protocol == null) {
        throw new MalformedURLException("no protocol: " + original);
      }

      // Get the protocol handler if not specified or the protocol
      // of the context could not be used
      if (handler == null && (handler = getURLStreamHandler(protocol)) == null) {
        throw new MalformedURLException("unknown protocol: " + protocol);
      }

      url.setHandlerByDelegate(handler);

      i = spec.indexOf('#', start);
      if (i >= 0) {
        url.setRefByDelegate(spec.substring(i + 1, limit));
        limit = i;
      }

      /*
       * Handle special case inheritance of query and fragment implied by
       * RFC2396 section 5.2.2.
       */
      if (isRelative && start == limit) {
        url.setQueryByDelegate(context.getQuery());
        if (url.getRef() == null) {
          url.setRefByDelegate(context.getRef());
        }
      }

      ((URLStreamHandler) handler).parseURL(url, spec, start, limit);

    } catch (MalformedURLException e) {
      throw e;
    } catch (Exception e) {
      MalformedURLException exception = new MalformedURLException(e.getMessage());
      exception.initCause(e);
      throw exception;
    }
  }

  /*
   * Returns true if specified string is a valid protocol name.
   */
  private boolean isValidProtocol(String protocol) {
    int len = protocol.length();
    if (len < 1) {
      return false;
    }
    char c = protocol.charAt(0);
    if (!Character.isLetter(c)) {
      return false;
    }
    for (int i = 1; i < len; i++) {
      c = protocol.charAt(i);
      if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-') {
        return false;
      }
    }
    return true;
  }

  /**
   * A table of protocol handlers.
   */
  static Hashtable<String, URLStreamHandler> handlers = new Hashtable<>();
  private static Object streamHandlerLock = new Object();

  /**
   * The URLStreamHandler factory.
   */
  static URLStreamHandlerFactory factory;

  public void setURLStreamHandlerFactory(Object fac) {
    synchronized (streamHandlerLock) {
      if (factory != null) {
        throw new Error("factory already defined");
      }
      SecurityManager security = System.getSecurityManager();
      if (security != null) {
        security.checkSetFactory();
      }
      handlers.clear();
      factory = (URLStreamHandlerFactory) fac;
    }
  }

  /**
   * Returns the Stream Handler.
   *
   * @param protocol
   *          the protocol to use
   */
  public Object getURLStreamHandler(String protocol) {

    URLStreamHandler handler = (URLStreamHandler) handlers.get(protocol);
    if (handler == null) {

      boolean checkedWithFactory = false;

      // Use the factory (if any)
      if (factory != null) {
        handler = factory.createURLStreamHandler(protocol);
        checkedWithFactory = true;
      }

      // Try java protocol handler
      if (handler == null) {
        final String packagePrefixList = System.getProperty(PROTOCOL_PATH_PROP, "");
        StringTokenizer packagePrefixIter = new StringTokenizer(packagePrefixList, "|");

        while (handler == null && packagePrefixIter.hasMoreTokens()) {

          String packagePrefix = packagePrefixIter.nextToken().trim();
          try {
            String clsName = packagePrefix + "." + protocol + ".Handler";
            Class<?> cls = null;
            try {
              ClassLoader cl = ClassLoader.getSystemClassLoader();
              cls = Class.forName(clsName, true, cl);
            } catch (ClassNotFoundException e) {
              ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
              if (contextLoader != null) {
                cls = Class.forName(clsName, true, contextLoader);
              }
            }
            if (cls != null) {
              handler = (URLStreamHandler) cls.newInstance();
            }
          } catch (ReflectiveOperationException ignored) {
            // Shouldn't happen, but code below handles the error.
          }
        }
      }

      // Fallback to built-in stream handler.
      // Makes okhttp the default http/https handler
      if (handler == null) {
        try {
          if (protocol.equals("file")) {
            // https://github.com/google/j2objc/issues/912
            handler = new sun.net.www.protocol.file.Handler();
          } else if (protocol.equals("jar")) {
            throw new UnsupportedOperationException("Jar streams are not supported.");
          } else if (protocol.equals("http")) {
            handler = new IosHttpHandler();
          } else if (protocol.equals("https")) {
            try {
              String name = "com.google.j2objc.net.IosHttpsHandler";
              handler = (URLStreamHandler) Class.forName(name).newInstance();
            } catch (Exception e) {
              throw new LibraryNotLinkedError("Https support", "jre_ssl",
                  "JavaxNetSslHttpsURLConnection");
            }
          }
        } catch (Exception e) {
          throw new AssertionError(e);
        }
      }

      synchronized (streamHandlerLock) {
        URLStreamHandler handler2 = null;

        // Check again with hashtable just in case another
        // thread created a handler since we last checked
        handler2 = (URLStreamHandler) handlers.get(protocol);

        if (handler2 != null) {
          return handler2;
        }

        // Check with factory if another thread set a
        // factory since our last check
        if (!checkedWithFactory && factory != null) {
          handler2 = factory.createURLStreamHandler(protocol);
        }

        if (handler2 != null) {
          // The handler from the factory must be given more
          // importance. Discard the default handler that
          // this thread created.
          handler = handler2;
        }

        // Insert this handler into the hashtable
        if (handler != null) {
          handlers.put(protocol, handler);
        }
      }
    }

    return handler;
  }

  public int getDefaultPort(URL url) throws MalformedURLException {
    return ((URLStreamHandler) url.getHandler()).getDefaultPort();
  }

  public boolean equals(URL u1, URL u2) throws MalformedURLException {
    return ((URLStreamHandler) u1.getHandler()).equals(u1, u2);
  }

  public int hashCode(URL url) throws MalformedURLException {
    return ((URLStreamHandler) url.getHandler()).hashCode(url);
  }

  public boolean sameFile(URL u1, URL u2) throws MalformedURLException {
    return ((URLStreamHandler) u1.getHandler()).sameFile(u1, u2);
  }

  public String toExternalForm(URL url) throws MalformedURLException {
    return ((URLStreamHandler) url.getHandler()).toExternalForm(url);
  }

  public URLConnection openConnection(URL url) throws IOException {
    return ((URLStreamHandler) url.getHandler()).openConnection(url);
  }

  public URLConnection openConnection(URL url, Object proxy) throws IOException {
    if (proxy == null) {
      throw new IllegalArgumentException("proxy can not be null");
    }

    // Create a copy of Proxy as a security measure
    Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY
        : sun.net.ApplicationProxy.create((Proxy) proxy);
    return ((URLStreamHandler) url.getHandler()).openConnection(url, p);
  }
}
