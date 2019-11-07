package com.strobel.core;

/**
 * @author Mike Strobel
 */
public interface SafeCloseable extends AutoCloseable {
    public void close();
}
