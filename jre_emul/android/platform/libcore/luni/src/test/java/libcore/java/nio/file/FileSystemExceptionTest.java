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

package libcore.java.nio.file;

import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.FileSystemException;
import libcore.libcore.util.SerializationTester;

public class FileSystemExceptionTest extends TestCase {

    public void test_constructor$String() {
        FileSystemException exception = new FileSystemException("file");
        assertEquals("file", exception.getFile());
        assertNull(exception.getOtherFile());
        assertNull(exception.getReason());
        assertTrue(exception instanceof IOException);
    }

    public void test_constructor$String$String$String() {
        FileSystemException exception = new FileSystemException("file", "otherFile", "reason");
        assertEquals("file", exception.getFile());
        assertEquals("otherFile", exception.getOtherFile());
        assertEquals("reason", exception.getReason());
    }

    /* J2ObjC removed: https://github.com/google/j2objc/issues/1425
    public void test_serialization() throws IOException, ClassNotFoundException {
        String hex = "ACED0005737200216A6176612E6E696F2E66696C652E46696C6553797374656D4578636570746"
                + "96F6ED598F27876D360FC0200024C000466696C657400124C6A6176612F6C616E672F537472696E6"
                + "73B4C00056F7468657271007E0001787200136A6176612E696F2E494F457863657074696F6E6C807"
                + "3646525F0AB020000787200136A6176612E6C616E672E457863657074696F6ED0FD1F3E1A3B1CC40"
                + "20000787200136A6176612E6C616E672E5468726F7761626C65D5C635273977B8CB0300044C00056"
                + "3617573657400154C6A6176612F6C616E672F5468726F7761626C653B4C000D64657461696C4D657"
                + "37361676571007E00015B000A737461636B547261636574001E5B4C6A6176612F6C616E672F53746"
                + "1636B5472616365456C656D656E743B4C001473757070726573736564457863657074696F6E73740"
                + "0104C6A6176612F7574696C2F4C6973743B787071007E0008740006726561736F6E7572001E5B4C6"
                + "A6176612E6C616E672E537461636B5472616365456C656D656E743B02462A3C3CFD2239020000787"
                + "0000000097372001B6A6176612E6C616E672E537461636B5472616365456C656D656E746109C59A2"
                + "636DD8502000449000A6C696E654E756D6265724C000E6465636C6172696E67436C61737371007E0"
                + "0014C000866696C654E616D6571007E00014C000A6D6574686F644E616D6571007E0001787000000"
                + "02374002D6C6962636F72652E6A6176612E6E696F2E66696C652E46696C6553797374656D4578636"
                + "57074696F6E5465737474001C46696C6553797374656D457863657074696F6E546573742E6A61766"
                + "1740025746573745F636F6E7374727563746F7224537472696E6724537472696E6724537472696E6"
                + "77371007E000CFFFFFFFE7400186A6176612E6C616E672E7265666C6563742E4D6574686F6474000"
                + "B4D6574686F642E6A617661740006696E766F6B657371007E000C000000F9740028766F6761722E7"
                + "461726765742E6A756E69742E4A756E69743324566F6761724A556E69745465737474000B4A756E6"
                + "974332E6A61766174000372756E7371007E000C00000063740020766F6761722E7461726765742E6"
                + "A756E69742E4A556E697452756E6E657224317400104A556E697452756E6E65722E6A61766174000"
                + "463616C6C7371007E000C0000005C740020766F6761722E7461726765742E6A756E69742E4A556E6"
                + "97452756E6E657224317400104A556E697452756E6E65722E6A61766174000463616C6C7371007E0"
                + "00C000000ED74001F6A6176612E7574696C2E636F6E63757272656E742E4675747572655461736B7"
                + "4000F4675747572655461736B2E6A61766174000372756E7371007E000C0000046D7400276A61766"
                + "12E7574696C2E636F6E63757272656E742E546872656164506F6F6C4578656375746F72740017546"
                + "872656164506F6F6C4578656375746F722E6A61766174000972756E576F726B65727371007E000C0"
                + "000025F74002E6A6176612E7574696C2E636F6E63757272656E742E546872656164506F6F6C45786"
                + "56375746F7224576F726B6572740017546872656164506F6F6C4578656375746F722E6A617661740"
                + "00372756E7371007E000C000002F97400106A6176612E6C616E672E54687265616474000B5468726"
                + "561642E6A61766174000372756E7372001F6A6176612E7574696C2E436F6C6C656374696F6E73244"
                + "56D7074794C6973747AB817B43CA79EDE02000078707874000466696C657400096F7468657246696"
                + "C65";

        FileSystemException exception = (FileSystemException) SerializationTester
                .deserializeHex(hex);

        String hex1 = SerializationTester.serializeHex(exception).toString();
        assertEquals(hex, hex1);
        assertEquals("file", exception.getFile());
        assertEquals("otherFile", exception.getOtherFile());
        assertEquals("reason", exception.getReason());
    }
     */

    public void test_getMessage() {
        FileSystemException exception = new FileSystemException("file", "otherFile", "reason");
        assertEquals("file -> otherFile: reason", exception.getMessage());

        exception = new FileSystemException("file", "otherFile", null);
        assertEquals("file -> otherFile", exception.getMessage());

        exception = new FileSystemException(null, "otherFile", "reason");
        assertEquals(" -> otherFile: reason", exception.getMessage());

        exception = new FileSystemException("file", null, "reason");
        assertEquals("file: reason", exception.getMessage());
    }
}
