/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Stub implementation of ClassLoader.
 *
 * @see Object
 */
public class ClassLoader {
  private static final ClassLoader systemLoader = new ClassLoader();
  
  private ClassLoader parent;
  
  protected ClassLoader(ClassLoader parent) {
    this.parent = parent;
  }

  protected ClassLoader() {
  }

  public void clearAssertionStatus() {}

  public static ClassLoader getSystemClassLoader() {
    return systemLoader;
  }

  public static URL getSystemResource(String name) {
    return null;
  }
  
  public static InputStream getSystemResourceAsStream(String name) {
    return null;
  }
  
  public static Enumeration<URL> getSystemResources(String name) throws IOException {
    return null;
  }
    
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return null;
  }
  
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    return null;
  }
    
  public URL getResource(String name) {
    return null;
}
    
  public Enumeration<URL> getResources(String name) throws IOException {
    return null;
  }
  
  public InputStream getResourceAsStream(String name) {
    return null;
  }
    
  protected Class findClass(String name) throws ClassNotFoundException {
    return null;
  }

  protected final void resolveClass(Class c) {}

  protected final Class findSystemClass(String name) throws ClassNotFoundException {
    return null;
  }

  protected final Class findLoadedClass(String name) {
    return null;
  }

  protected java.net.URL findResource(String name) {
    return null;
  }

  protected java.util.Enumeration findResources(String name) throws java.io.IOException {
    return null;
  }

  public final ClassLoader getParent() {
    return parent != null ? parent : systemLoader;
  }

  public synchronized void setDefaultAssertionStatus(boolean b) {}

  /* Unimplemented methods
  Class defineClass(byte[] code, int offset, int length)
  Class defineClass(String name, byte[] code, int offset, int length)
  Class defineClass(String name, byte[] code, int offset, int length, ProtectionDomain p)
  Class defineClass(String name, ByteBuffer, ProtectionDomain p)
  void setSigners(Class c, Object[] signers)
  Package definePackage(String, String, String, String, String, String, String, URL)
  Package getPackage(String)
  Package[] getPackages()
  String findLibrary(String)
  void setPackageAssertionStatus(String, boolean)
  void setClassAssertionStatus(String, boolean)
  */
}
