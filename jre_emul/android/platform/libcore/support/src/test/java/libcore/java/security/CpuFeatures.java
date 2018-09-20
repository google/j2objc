/*
 * Copyright 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpuFeatures {
    private CpuFeatures() {
    }

    public static boolean isAESHardwareAccelerated() {
        List<String> features = getListFromCpuinfo("Features");
        if (features != null && features.contains("aes")) {
            return true;
        }

        List<String> flags = getListFromCpuinfo("flags");
        if (flags != null && flags.contains("aes")) {
            return true;
        }

        // If we're in an emulated ABI, Conscrypt's NativeCrypto might bridge to
        // a library that has accelerated AES instructions. See if Conscrypt
        // detects that condition.
        try {
            Class<?> nativeCrypto = Class.forName("com.android.org.conscrypt.NativeCrypto");
            Method EVP_has_aes_hardware = nativeCrypto.getDeclaredMethod("EVP_has_aes_hardware");
            return ((Integer) EVP_has_aes_hardware.invoke(null)) == 1;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException ignored) {
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }

        return false;
    }

    private static String getFieldFromCpuinfo(String field) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");

            try {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        return m.group(1);
                    }
                }
            } finally {
                br.close();
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private static List<String> getListFromCpuinfo(String fieldName) {
        String features = getFieldFromCpuinfo(fieldName);
        if (features == null)
            return null;

        return Arrays.asList(features.split("\\s"));
    }
}
