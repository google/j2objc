/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2007, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.icu.dev.test.TestDataModule.DataMap;
import android.icu.dev.test.TestDataModule.DataModuleFormatError;
import android.icu.dev.test.TestDataModule.Factory;
import android.icu.dev.test.TestDataModule.TestData;

/**
 * Ray: An adapter class for TestDataMoule to make it like TestFmwk
 * 
 * A convenience extension of TestFmwk for use by data module-driven tests.
 * 
 * Tests can implement this if they make extensive use of information in a
 * TestDataModule.
 * 
 * Subclasses can allow for test methods that don't use data from the module by
 * overriding validateMethod to return true for these methods. Tests are also
 * free to instantiate their own modules and run from them, though care should
 * be taken not to interfere with the methods in this class.
 * 
 * See CollationTest for an example.
 */
public class ModuleTest {
    private ModuleTest() {
        // prevent construction
    }

    /**
     * 
     * TestFmwk calls this before trying to run a suite of tests. The test suite
     * if valid if a module whose name is the name of this class + "Data" can be
     * opened. Subclasses can override this if there are different or additional
     * data required.
     */

    public static TestDataModule loadTestData(String baseName, String testName) throws DataModuleFormatError {
        return Factory.get(baseName, testName);
    }

    static TestData openTestData(TestDataModule module, String name) throws DataModuleFormatError {
        return module.getTestData(name);
    }

    public static class TestDataPair {
        public TestData td;
        public DataMap dm;

        public TestDataPair(TestData td, DataMap dm) {
            this.td = td;
            this.dm = dm;
        }
    }

    public static List<TestDataPair> getTestData(String moduleLocation, String moduleName) throws Exception {
        List<TestDataPair> list = new ArrayList<TestDataPair>();

        TestDataModule m = ModuleTest.loadTestData(moduleLocation, moduleName);
        Iterator<TestData> tIter = m.getTestDataIterator();
        while (tIter.hasNext()) {
            TestData t = tIter.next();
            for (Iterator siter = t.getSettingsIterator(); siter.hasNext();) {
                DataMap settings = (DataMap) siter.next();
                list.add(new TestDataPair(t, settings));
            }
        }
        return list;
    }
}
