/*
 * Copyright (C) 2017 The Android Open Source Project
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

package libcore.java.nio.file.attribute;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.file.attribute.UserPrincipalNotFoundException;

public class UserPrincipalNotFoundExceptionTest {

    @Test
    public void testGetters() throws Exception {
        final String name = "foobar";
        UserPrincipalNotFoundException upnfException =
            new UserPrincipalNotFoundException(name);
        assertEquals(name, upnfException.getName());
    }
}
