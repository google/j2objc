/*
 * Copyright (C) 2021 The Android Open Source Project
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

package libcore.javax.xml.datatype;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeConstants.Field;
import javax.xml.datatype.Duration;

/**
 * Avoid depending on {@link javax.xml.datatype.DatatypeFactory}, and
 * throws {@link UnsupportedOperationException} on abstract methods,
 * since proper implementation does not exist in Android.
 */
public class DurationImpl extends javax.xml.datatype.Duration {

    private final int sign;
    private final Map<Field, Number> fields;

    DurationImpl(long millis) {
        fields = new HashMap<>();
        if (millis > 0) {
            sign = 1;
        } else if (millis == 0) {
            sign = 0;
            return;
        } else {
            sign = -1;
            millis = -millis;
        }
        long d = millis / 86400000L;
        millis %= 86400000L;
        if (d > 0) {
            fields.put(DatatypeConstants.DAYS, d);
        }
        long h = millis / 3600000L;
        millis %= 3600000L;
        if (h > 0) {
            fields.put(DatatypeConstants.HOURS, h);
        }
        long m = millis / 60000L;
        millis %= 60000L;
        if (m > 0) {
            fields.put(DatatypeConstants.MINUTES, m);
        }
        fields.put(DatatypeConstants.SECONDS, (float)millis / 1000);
    }

    DurationImpl(int sgn, int y, int months, int d, int h, int m, float s) {
        sign = sgn;
        fields = new HashMap<>();
        if (y >= 0) { fields.put(DatatypeConstants.YEARS, y); }
        if (months >= 0) { fields.put(DatatypeConstants.MONTHS, months); }
        if (d >= 0) { fields.put(DatatypeConstants.DAYS, d); }
        if (h >= 0) { fields.put(DatatypeConstants.HOURS, h); }
        if (m >= 0) { fields.put(DatatypeConstants.MINUTES, m); }
        if (s >= 0) { fields.put(DatatypeConstants.SECONDS, s); }
    }

    @Override
    public int getSign() {
        return sign;
    }

    @Override
    public Number getField(Field field) {
        return fields.get(field);
    }

    @Override
    public boolean isSet(Field field) {
        return fields.containsKey(field);
    }

    @Override
    public Duration add(Duration rhs) {
        throw new UnsupportedOperationException("Stub implementation");
    }

    @Override
    public void addTo(Calendar calendar) {
        throw new UnsupportedOperationException("Stub implementation");
    }

    @Override
    public Duration multiply(BigDecimal factor) {
        throw new UnsupportedOperationException("Stub implementation");
    }

    @Override
    public Duration negate() {
        throw new UnsupportedOperationException("Stub implementation");
    }

    @Override
    public Duration normalizeWith(Calendar startTimeInstant) {
        throw new UnsupportedOperationException("Stub implementation");
    }

    @Override
    public int compare(Duration duration) {
        throw new UnsupportedOperationException("Stub implementation");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Stub implementation");
    }
}
