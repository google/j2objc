/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package libcore.java.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import junit.framework.Assert;
import tests.support.Support_ASimpleInputStream;
import tests.support.Support_StringReader;

public class OldStreamTokenizerTest extends junit.framework.TestCase {
    StreamTokenizer st;

    public void test_nextToken() throws IOException {
        st = new StreamTokenizer(new Support_StringReader("\n \r\n#"));
        st.ordinaryChar('\n'); // make \n ordinary
        st.eolIsSignificant(true);
        assertTrue("Wrong token 2,1", st.nextToken() == '\n');
        assertTrue("Wrong token 2,2", st.nextToken() == '\n');
        assertEquals("Wrong token 2,3", '#', st.nextToken());

        Support_ASimpleInputStream sis = new Support_ASimpleInputStream();
        sis.throwExceptionOnNextUse = true;
        st = new StreamTokenizer(sis);
        try {
            st.nextToken();
            fail("IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_basicStringTokenizerMethods() throws IOException {
        String str = "Testing 12345 \n alpha \r\n omega";
        String strb = "-3.8 'BLIND mice' \r sEe /* how */ they run";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(strb);
        StreamTokenizer a = new StreamTokenizer(aa);
        StreamTokenizer b = new StreamTokenizer(ba);

        Assert.assertTrue(a.lineno() == 1);
        Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_WORD);
        Assert.assertTrue(a.toString().equals("Token[Testing], line 1"));
        Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_NUMBER);
        Assert.assertTrue(a.toString().equals("Token[n=12345.0], line 1"));
        Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_WORD);
        Assert.assertTrue(a.toString().equals("Token[alpha], line 2"));
        Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_WORD);
        Assert.assertTrue(a.toString().equals("Token[omega], line 3"));
        Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_EOF);
        Assert.assertTrue(a.toString().equals("Token[EOF], line 3"));

        b.commentChar('u');
        b.eolIsSignificant(true);
        b.lowerCaseMode(true);
        b.ordinaryChar('y');
        b.slashStarComments(true);

        Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_NUMBER);
        Assert.assertTrue(b.nval == -3.8);
        Assert.assertTrue(b.toString().equals("Token[n=-3.8], line 1"));
        Assert.assertTrue(b.nextToken() == 39); // '
        Assert.assertTrue(b.toString().equals("Token[BLIND mice], line 1"));
        Assert.assertTrue(b.nextToken() == 10); // \n
        Assert.assertTrue(b.toString().equals("Token[EOL], line 2"));
        Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_WORD);
        Assert.assertTrue(b.toString().equals("Token[see], line 2"));
        Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_WORD);
        Assert.assertTrue(b.toString().equals("Token[the], line 2"));
        Assert.assertTrue(b.nextToken() == 121); // y
        Assert.assertTrue(b.toString().equals("Token['y'], line 2"));
        Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_WORD);
        Assert.assertTrue(b.toString().equals("Token[r], line 2"));
        Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_EOF);
        Assert.assertTrue(b.toString().equals("Token[EOF], line 2"));
    }

    public void test_harmonyRegressionTest() {
        byte[] data = new byte[] {(byte) '-'};
        StreamTokenizer tokenizer = new StreamTokenizer(new ByteArrayInputStream(data));
        try {
            tokenizer.nextToken();
        } catch(Exception e) {
            Assert.fail(e.getMessage());
        }
        String result = tokenizer.toString();
        Assert.assertEquals("Token['-'], line 1", result);
    }

    public void test_harmonyRegressionTest2() {
        byte[] data = new byte[] {(byte) '"',
                                  (byte) 'H',
                                  (byte) 'e',
                                  (byte) 'l',
                                  (byte) 'l',
                                  (byte) 'o',
                                  (byte) '"'};
        StreamTokenizer tokenizer = new StreamTokenizer(new ByteArrayInputStream(data));
        try {
            tokenizer.nextToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = tokenizer.toString();
        Assert.assertEquals("Token[Hello], line 1", result);
    }
}
