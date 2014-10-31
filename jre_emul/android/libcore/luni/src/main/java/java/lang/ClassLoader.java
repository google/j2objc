/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/*-[
#import "java/util/ArrayList.h"
#import "java/util/Collections.h"
]-*/

/**
 * Loads classes and resources from a repository. One or more class loaders are
 * installed at runtime. These are consulted whenever the runtime system needs a
 * specific class that is not yet available in-memory. Typically, class loaders
 * are grouped into a tree where child class loaders delegate all requests to
 * parent class loaders. Only if the parent class loader cannot satisfy the
 * request, the child class loader itself tries to handle it.
 * <p>
 * {@code ClassLoader} is an abstract class that implements the common
 * infrastructure required by all class loaders. J2ObjC provides a native
 * implementation of the class, SystemClassLoader, which is the one typically
 * used. Other applications may implement subclasses of {@code ClassLoader}
 * to provide special ways for loading classes.
 * </p><p>
 * Note: since classes cannot be dynamically created in iOS or OS X, class
 * loaders have a much more limited utility than with JVM-based systems like
 * Java and Android.
 * </p>
 * @see Class
 */
public abstract class ClassLoader {

    /**
     * The parent ClassLoader.
     */
    private ClassLoader parent;

    /**
     * The packages known to the class loader.
     */
    private Map<String, Package> packages = new HashMap<String, Package>();

    /**
     * Returns the system class loader. This is the parent for new
     * {@code ClassLoader} instances and is typically the class loader used to
     * start the application.
     */
    public static ClassLoader getSystemClassLoader() {
        return SystemClassLoader.loader;
    }

    /**
     * Finds the URL of the resource with the specified name. The system class
     * loader's resource lookup algorithm is used to find the resource.
     *
     * @return the {@code URL} object for the requested resource or {@code null}
     *         if the resource can not be found.
     * @param resName
     *            the name of the resource to find.
     * @see Class#getResource
     */
    public static URL getSystemResource(String resName) {
        return SystemClassLoader.loader.getResource(resName);
    }

    /**
     * Returns an enumeration of URLs for the resource with the specified name.
     * The system class loader's resource lookup algorithm is used to find the
     * resource.
     *
     * @return an enumeration of {@code URL} objects containing the requested
     *         resources.
     * @param resName
     *            the name of the resource to find.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public static Enumeration<URL> getSystemResources(String resName) throws IOException {
        return SystemClassLoader.loader.getResources(resName);
    }

    /**
     * Returns a stream for the resource with the specified name. The system
     * class loader's resource lookup algorithm is used to find the resource.
     * Basically, the contents of the java.class.path are searched in order,
     * looking for a path which matches the specified resource.
     *
     * @return a stream for the resource or {@code null}.
     * @param resName
     *            the name of the resource to find.
     * @see Class#getResourceAsStream
     */
    public static InputStream getSystemResourceAsStream(String resName) {
        return SystemClassLoader.loader.getResourceAsStream(resName);
    }

    /**
     * Constructs a new instance of this class with the system class loader as
     * its parent.
     */
    protected ClassLoader() {
        this(getSystemClassLoader(), false);
    }

    /**
     * Constructs a new instance of this class with the specified class loader
     * as its parent.
     *
     * @param parentLoader
     *            The {@code ClassLoader} to use as the new class loader's
     *            parent.
     */
    protected ClassLoader(ClassLoader parentLoader) {
        this(parentLoader, false);
    }

    /*
     * constructor for the BootClassLoader which needs parent to be null.
     */
    ClassLoader(ClassLoader parentLoader, boolean nullAllowed) {
        if (parentLoader == null && !nullAllowed) {
            throw new NullPointerException("parentLoader == null && !nullAllowed");
        }
        parent = parentLoader;
    }

    /**
     * Constructs a new class from an array of bytes containing a class
     * definition in class file format.
     *
     * @param classRep
     *            the memory image of a class file.
     * @param offset
     *            the offset into {@code classRep}.
     * @param length
     *            the length of the class file.
     * @return the {@code Class} object created from the specified subset of
     *         data in {@code classRep}.
     * @throws ClassFormatError
     *             if {@code classRep} does not contain a valid class.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0}, {@code length < 0} or if
     *             {@code offset + length} is greater than the length of
     *             {@code classRep}.
     * @deprecated Use {@link #defineClass(String, byte[], int, int)}
     */
    @Deprecated
    protected final Class<?> defineClass(byte[] classRep, int offset, int length)
            throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /**
     * Constructs a new class from an array of bytes containing a class
     * definition in class file format.
     *
     * @param className
     *            the expected name of the new class, may be {@code null} if not
     *            known.
     * @param classRep
     *            the memory image of a class file.
     * @param offset
     *            the offset into {@code classRep}.
     * @param length
     *            the length of the class file.
     * @return the {@code Class} object created from the specified subset of
     *         data in {@code classRep}.
     * @throws ClassFormatError
     *             if {@code classRep} does not contain a valid class.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0}, {@code length < 0} or if
     *             {@code offset + length} is greater than the length of
     *             {@code classRep}.
     */
    protected final Class<?> defineClass(String className, byte[] classRep, int offset, int length)
            throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /**
     * Constructs a new class from an array of bytes containing a class
     * definition in class file format and assigns the specified protection
     * domain to the new class. If the provided protection domain is
     * {@code null} then a default protection domain is assigned to the class.
     *
     * @param className
     *            the expected name of the new class, may be {@code null} if not
     *            known.
     * @param classRep
     *            the memory image of a class file.
     * @param offset
     *            the offset into {@code classRep}.
     * @param length
     *            the length of the class file.
     * @param protectionDomain
     *            the protection domain to assign to the loaded class, may be
     *            {@code null}.
     * @return the {@code Class} object created from the specified subset of
     *         data in {@code classRep}.
     * @throws ClassFormatError
     *             if {@code classRep} does not contain a valid class.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0}, {@code length < 0} or if
     *             {@code offset + length} is greater than the length of
     *             {@code classRep}.
     * @throws NoClassDefFoundError
     *             if {@code className} is not equal to the name of the class
     *             contained in {@code classRep}.
     */
    protected final Class<?> defineClass(String className, byte[] classRep, int offset, int length,
            ProtectionDomain protectionDomain) throws java.lang.ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /**
     * Defines a new class with the specified name, byte code from the byte
     * buffer and the optional protection domain. If the provided protection
     * domain is {@code null} then a default protection domain is assigned to
     * the class.
     *
     * @param name
     *            the expected name of the new class, may be {@code null} if not
     *            known.
     * @param b
     *            the byte buffer containing the byte code of the new class.
     * @param protectionDomain
     *            the protection domain to assign to the loaded class, may be
     *            {@code null}.
     * @return the {@code Class} object created from the data in {@code b}.
     * @throws ClassFormatError
     *             if {@code b} does not contain a valid class.
     * @throws NoClassDefFoundError
     *             if {@code className} is not equal to the name of the class
     *             contained in {@code b}.
     */
    protected final Class<?> defineClass(String name, ByteBuffer b,
            ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /**
     * Overridden by subclasses, throws a {@code ClassNotFoundException} by
     * default. This method is called by {@code loadClass} after the parent
     * {@code ClassLoader} has failed to find a loaded class of the same name.
     *
     * @param className
     *            the name of the class to look for.
     * @return the {@code Class} object that is found.
     * @throws ClassNotFoundException
     *             if the class cannot be found.
     */
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        throw new ClassNotFoundException(className);
    }

    /**
     * Returns the class with the specified name if it has already been loaded
     * by the VM or {@code null} if it has not yet been loaded.
     *
     * @param className
     *            the name of the class to look for.
     * @return the {@code Class} object or {@code null} if the requested class
     *         has not been loaded.
     */
    protected final Class<?> findLoadedClass(String className) {
        try {
	    return SystemClassLoader.loader.findClass(className);
	} catch (ClassNotFoundException e) {
	    return null;
	}
    }

    /**
     * Finds the class with the specified name, loading it using the system
     * class loader if necessary.
     *
     * @param className
     *            the name of the class to look for.
     * @return the {@code Class} object with the requested {@code className}.
     * @throws ClassNotFoundException
     *             if the class can not be found.
     */
    protected final Class<?> findSystemClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, getSystemClassLoader());
    }

    /**
     * Returns this class loader's parent.
     *
     * @return this class loader's parent or {@code null}.
     */
    public final ClassLoader getParent() {
        return parent;
    }

    /**
     * Returns the URL of the resource with the specified name. This
     * implementation first tries to use the parent class loader to find the
     * resource; if this fails then {@link #findResource(String)} is called to
     * find the requested resource.
     *
     * @param resName
     *            the name of the resource to find.
     * @return the {@code URL} object for the requested resource or {@code null}
     *         if the resource can not be found
     * @see Class#getResource
     */
    public URL getResource(String resName) {
        URL resource = parent.getResource(resName);
        if (resource == null) {
            resource = findResource(resName);
        }
        return resource;
    }

    /**
     * Returns an enumeration of URLs for the resource with the specified name.
     * This implementation first uses this class loader's parent to find the
     * resource, then it calls {@link #findResources(String)} to get additional
     * URLs. The returned enumeration contains the {@code URL} objects of both
     * find operations.
     *
     * @return an enumeration of {@code URL} objects for the requested resource.
     * @param resName
     *            the name of the resource to find.
     * @throws IOException
     *             if an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    public Enumeration<URL> getResources(String resName) throws IOException {

        Enumeration first = parent.getResources(resName);
        Enumeration second = findResources(resName);

        return new TwoEnumerationsInOne(first, second);
    }

    /**
     * Returns a stream for the resource with the specified name. See
     * {@link #getResource(String)} for a description of the lookup algorithm
     * used to find the resource.
     *
     * @return a stream for the resource or {@code null} if the resource can not be found
     * @param resName
     *            the name of the resource to find.
     * @see Class#getResourceAsStream
     */
    public InputStream getResourceAsStream(String resName) {
        try {
            URL url = getResource(resName);
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException ex) {
            // Don't want to see the exception.
        }

        return null;
    }

    /**
     * Loads the class with the specified name. Invoking this method is
     * equivalent to calling {@code loadClass(className, false)}.
     * <p>
     * <strong>Note:</strong> In the Android reference implementation, the
     * second parameter of {@link #loadClass(String, boolean)} is ignored
     * anyway.
     * </p>
     *
     * @return the {@code Class} object.
     * @param className
     *            the name of the class to look for.
     * @throws ClassNotFoundException
     *             if the class can not be found.
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, false);
    }

    /**
     * Loads the class with the specified name, optionally linking it after
     * loading. The following steps are performed:
     * <ol>
     * <li> Call {@link #findLoadedClass(String)} to determine if the requested
     * class has already been loaded.</li>
     * <li>If the class has not yet been loaded: Invoke this method on the
     * parent class loader.</li>
     * <li>If the class has still not been loaded: Call
     * {@link #findClass(String)} to find the class.</li>
     * </ol>
     * <p>
     * <strong>Note:</strong> In the Android reference implementation, the
     * {@code resolve} parameter is ignored; classes are never linked.
     * </p>
     *
     * @return the {@code Class} object.
     * @param className
     *            the name of the class to look for.
     * @param resolve
     *            Indicates if the class should be resolved after loading. This
     *            parameter is ignored on the Android reference implementation;
     *            classes are not resolved.
     * @throws ClassNotFoundException
     *             if the class can not be found.
     */
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);

        if (clazz == null) {
            try {
                clazz = parent.loadClass(className, false);
            } catch (ClassNotFoundException e) {
                // Don't want to see this.
            }

            if (clazz == null) {
                clazz = findClass(className);
            }
        }

        return clazz;
    }

    /**
     * Forces a class to be linked (initialized). If the class has already been
     * linked this operation has no effect.
     * <p>
     * <strong>Note:</strong> In the Android reference implementation, this
     * method has no effect.
     * </p>
     *
     * @param clazz
     *            the class to link.
     */
    protected final void resolveClass(Class<?> clazz) {
        // no-op, doesn't make sense on android.
    }

    /**
     * Finds the URL of the resource with the specified name. This
     * implementation just returns {@code null}; it should be overridden in
     * subclasses.
     *
     * @param resName
     *            the name of the resource to find.
     * @return the {@code URL} object for the requested resource.
     */
    protected URL findResource(String resName) {
        return null;
    }

    /**
     * Finds an enumeration of URLs for the resource with the specified name.
     * This implementation just returns an empty {@code Enumeration}; it should
     * be overridden in subclasses.
     *
     * @param resName
     *            the name of the resource to find.
     * @return an enumeration of {@code URL} objects for the requested resource.
     * @throws IOException
     *             if an I/O error occurs.
     */
    protected Enumeration<URL> findResources(String resName) throws IOException {
        return Collections.emptyEnumeration();
    }

    /**
     * Returns the absolute path of the native library with the specified name,
     * or {@code null}. If this method returns {@code null} then the virtual
     * machine searches the directories specified by the system property
     * "java.library.path".
     * <p>
     * This implementation always returns {@code null}.
     * </p>
     *
     * @param libName
     *            the name of the library to find.
     * @return the absolute path of the library.
     */
    protected String findLibrary(String libName) {
        return null;
    }

    /**
     * Returns the package with the specified name. Package information is
     * searched in this class loader.
     *
     * @param name
     *            the name of the package to find.
     * @return the package with the requested name; {@code null} if the package
     *         can not be found.
     */
    protected Package getPackage(String name) {
        synchronized (packages) {
            return packages.get(name);
        }
    }

    /**
     * Returns all the packages known to this class loader.
     *
     * @return an array with all packages known to this class loader.
     */
    protected Package[] getPackages() {
        synchronized (packages) {
            Collection<Package> col = packages.values();
            Package[] result = new Package[col.size()];
            col.toArray(result);
            return result;
        }
    }

    /**
     * Defines and returns a new {@code Package} using the specified
     * information. If {@code sealBase} is {@code null}, the package is left
     * unsealed. Otherwise, the package is sealed using this URL.
     *
     * @param name
     *            the name of the package.
     * @param specTitle
     *            the title of the specification.
     * @param specVersion
     *            the version of the specification.
     * @param specVendor
     *            the vendor of the specification.
     * @param implTitle
     *            the implementation title.
     * @param implVersion
     *            the implementation version.
     * @param implVendor
     *            the specification vendor.
     * @param sealBase
     *            the URL used to seal this package or {@code null} to leave the
     *            package unsealed.
     * @return the {@code Package} object that has been created.
     * @throws IllegalArgumentException
     *             if a package with the specified name already exists.
     */
    protected Package definePackage(String name, String specTitle, String specVersion,
            String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase)
            throws IllegalArgumentException {
        throw new UnsupportedOperationException("new package definitions unsupported");
    }

    /**
     * Sets the signers of the specified class. This implementation does
     * nothing.
     *
     * @param c
     *            the {@code Class} object for which to set the signers.
     * @param signers
     *            the signers for {@code c}.
     */
    protected final void setSigners(Class<?> c, Object[] signers) {
    }

    /**
     * Sets the assertion status of the class with the specified name.
     * <p>
     * <strong>Note: </strong>This method does nothing in the Android reference
     * implementation.
     * </p>
     *
     * @param cname
     *            the name of the class for which to set the assertion status.
     * @param enable
     *            the new assertion status.
     */
    public void setClassAssertionStatus(String cname, boolean enable) {
    }

    /**
     * Sets the assertion status of the package with the specified name.
     * <p>
     * <strong>Note: </strong>This method does nothing in the Android reference
     * implementation.
     * </p>
     *
     * @param pname
     *            the name of the package for which to set the assertion status.
     * @param enable
     *            the new assertion status.
     */
    public void setPackageAssertionStatus(String pname, boolean enable) {
    }

    /**
     * Sets the default assertion status for this class loader.
     *
     * @param enable
     *            the new assertion status.
     */
    public void setDefaultAssertionStatus(boolean enable) {
    }

    /**
     * Sets the default assertion status for this class loader to {@code false}
     * and removes any package default and class assertion status settings.
     * <p>
     * <strong>Note:</strong> This method does nothing in the Android reference
     * implementation.
     * </p>
     */
    public void clearAssertionStatus() {
    }
}

/*
 * Provides a helper class that combines two existing URL enumerations into one.
 * It is required for the getResources() methods. Items are fetched from the
 * first enumeration until it's empty, then from the second one.
 */
class TwoEnumerationsInOne implements Enumeration<URL> {

    private Enumeration<URL> first;

    private Enumeration<URL> second;

    public TwoEnumerationsInOne(Enumeration<URL> first, Enumeration<URL> second) {
        this.first = first;
        this.second = second;
    }

    public boolean hasMoreElements() {
        return first.hasMoreElements() || second.hasMoreElements();
    }

    public URL nextElement() {
        if (first.hasMoreElements()) {
            return first.nextElement();
        } else {
            return second.nextElement();
        }
    }

}


/**
 * ClassLoader for iOS and OS X.
 */
class SystemClassLoader extends ClassLoader {

  static ClassLoader loader = new SystemClassLoader();

  SystemClassLoader() {
    super(null, true);
  }

  @Override
  protected native Class<?> findClass(String name) throws ClassNotFoundException /*-[
    return [IOSClass forName:name initialize:YES classLoader:self];
  ]-*/;

  @Override
  protected native URL findResource(String name) /*-[
    NSBundle *bundle = [NSBundle mainBundle];
    NSURL *nativeURL = [bundle URLForResource:name withExtension:nil];
    return nativeURL ? AUTORELEASE([[JavaNetURL alloc] initWithNSString:[nativeURL description]])
        : nil;
  ]-*/;

  @Override
  protected native Enumeration<URL> findResources(String name) throws IOException /*-[
    JavaUtilArrayList *urls = AUTORELEASE([[JavaUtilArrayList alloc] init]);
    for (NSBundle *bundle in [NSBundle allBundles]) {
      NSURL *nativeURL = [bundle URLForResource:name withExtension:nil];
      if (nativeURL) {
        JavaNetURL *url =
            AUTORELEASE([[JavaNetURL alloc] initWithNSString:[nativeURL description]]);
        [urls addWithId:url];
      }
    }
    return JavaUtilCollections_enumerationWithJavaUtilCollection_(urls);
  ]-*/;

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    // All iOS classes are resolved.
    return findClass(name);
  }

  @Override
  public URL getResource(String resName) {
    return findResource(resName);
  }

  @Override
  public Enumeration<URL> getResources(String resName) throws IOException {
      return findResources(resName);
  }
}
