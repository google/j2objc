/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2009, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test;

import java.util.Iterator;

/**
 * Represents a collection of test data described in a file.
 * 
 */
public interface TestDataModule {
    /**
     * Return the name of this test data module.
     */
    public String getName();

    /**
     * Get additional data related to the module, e.g. DESCRIPTION,
     * global settings.  Might be null.
     */
    public DataMap getInfo();

    /**
     * Returns the TestData corresponding to name, or null if name not
     * found in this module.  Throw error if name is not found.  
     * @throws DataModuleFormatError
     */
    public TestData getTestData(String name) throws DataModuleFormatError;

    /**
     * @return Iterator<TestData>
     */
    public Iterator getTestDataIterator();

    public static class Factory{

        static final TestDataModule get(String baseName, String localeName) throws DataModuleFormatError {
            return new ResourceModule(baseName, localeName);
        }
    }

    public static class DataModuleFormatError extends Exception{
        /**
         * For serialization
         */
        private static final long serialVersionUID = 4312521272388482529L;
        public DataModuleFormatError(String msg){
            super(msg);
        }
        public DataModuleFormatError(String msg, Throwable cause){
            super(msg, cause);
        }
        public DataModuleFormatError(Throwable cause) {
            super(cause);
        }
    }
    
    /**
     * Represents a single test in the module.
     */
    public static interface TestData {
        public String getName();
        /**
         * Get additional data related to the test data, e.g. DESCRIPTION,
         * global settings.  Might be null.
         */
        public DataMap getInfo();
        /**
         * @return Iterator<DataMap>
         */
        public Iterator getSettingsIterator();
        /**
         * @return Iterator<DataMap>
         */
        public Iterator getDataIterator();
    }

    /**
     * Map-like interface for accessing key-value pairs by key.
     * If the vaule is not found by given key, return null. 
     * The behavior is analogous the get() method of the Map interface.
     * 
     * @author Raymond Yang
     */
    public interface DataMap {
//    public abstract boolean    isDefined(String key);
//
    public abstract Object     getObject(String key);
    public abstract String     getString(String key);
//    public abstract char       getChar(String key);
//    public abstract int        getInt(String key);
//    public abstract byte       getByte(String key);
//    public abstract boolean    getBoolean(String key);
//
//    public abstract Object[]   getObjectArray(String key);
//    public abstract String[]   getStringArray(String key);
//    public abstract char[]     getCharArray(String key);
//    public abstract int[]      getIntArray(String key);
//    public abstract byte[]     getByteArray(String key);
//    public abstract boolean[]  getBooleanArray(String key);
    }
}
    
