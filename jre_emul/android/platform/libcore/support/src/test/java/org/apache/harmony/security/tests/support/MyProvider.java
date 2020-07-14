package org.apache.harmony.security.tests.support;

import java.security.Provider;

public class MyProvider extends Provider {

    public MyProvider() {
        super("MyProvider", 1.0, "Test Provider 1.0");
        put("KeyStore.TestKeyStore", TestKeyStoreSpi.class.getName());
    }

}
