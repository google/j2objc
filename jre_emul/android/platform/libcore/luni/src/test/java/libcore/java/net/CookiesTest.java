/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.net;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.InMemoryCookieStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookiesTest extends AbstractCookiesTest {
    @Override
    public CookieStore createCookieStore() {
        return new InMemoryCookieStore(24 /* VERSION_CODES.N : android N */);
    }

    // http://b/26456024
    public void testCookiesWithLeadingPeriod() throws Exception {
        CookieManager cm = new CookieManager(createCookieStore(), null);
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("coulomb_sess=81c112d7dabac869ffa821aa8f672df2");
        responseHeaders.put("Set-Cookie", list);

        URI uri = new URI("http://chargepoint.com");
        cm.put(uri, responseHeaders);

        Map<String, List<String>> cookies = cm.get(
                new URI("https://webservices.chargepoint.com/backend.php/mobileapi/"),
                responseHeaders);

        assertEquals(1, cookies.size());
        List<String> cookieList = cookies.values().iterator().next();
        assertEquals("coulomb_sess=81c112d7dabac869ffa821aa8f672df2", cookieList.get(0));
    }
}
