package com.strobel.core;

/**
 * @author Mike Strobel
 */
public interface IFreezable {
    boolean canFreeze();
    boolean isFrozen();
    void freeze()
        throws IllegalStateException;
    boolean tryFreeze();
    void freezeIfUnfrozen() throws IllegalStateException;
}
