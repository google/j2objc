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
import static junit.framework.TestCase.assertTrue;

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Set;


public class AclEntryTest {

    @Test
    public void testGetters() throws Exception {
        UserPrincipal user = Files.getOwner(Paths.get("."));

        AclEntry aclEntry = AclEntry.newBuilder()
            .setType(AclEntryType.ALLOW)
            .setPrincipal(user)
            .setFlags(AclEntryFlag.INHERIT_ONLY)
            .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES)
            .build();
        assertEquals(AclEntryType.ALLOW, aclEntry.type());
        assertEquals(user, aclEntry.principal());

        Set<AclEntryPermission> permissions = aclEntry.permissions();
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains(AclEntryPermission.READ_DATA));
        assertTrue(permissions.contains(AclEntryPermission.READ_ATTRIBUTES));

        Set<AclEntryFlag> flags = aclEntry.flags();
        assertEquals(1, flags.size());
        assertTrue(flags.contains(AclEntryFlag.INHERIT_ONLY));
    }
}
