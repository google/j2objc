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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import junit.framework.TestCase;

public class OldLineNumberReaderTest extends TestCase {

    String text = "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29\n30\n31\n32\n33\n34\n35\n36\n37\n38\n39\n40\n41\n42\n43\n44\n45\n46\n47\n48\n49\n50\n51\n52\n53\n54\n55\n56\n57\n58\n59\n60\n61\n62\n63\n64\n65\n66\n67\n68\n69\n70\n71\n72\n73\n74\n75\n76\n77\n78\n79\n80\n81\n82\n83\n84\n85\n86\n87\n88\n89\n90\n91\n92\n93\n94\n95\n96\n97\n98\n99\n100\n101\n102\n103\n104\n105\n106\n107\n108\n109\n110\n111\n112\n113\n114\n115\n116\n117\n118\n119\n120\n121\n122\n123\n124\n125\n126\n127\n128\n129\n130\n131\n132\n133\n134\n135\n136\n137\n138\n139\n140\n141\n142\n143\n144\n145\n146\n147\n148\n149\n150\n151\n152\n153\n154\n155\n156\n157\n158\n159\n160\n161\n162\n163\n164\n165\n166\n167\n168\n169\n170\n171\n172\n173\n174\n175\n176\n177\n178\n179\n180\n181\n182\n183\n184\n185\n186\n187\n188\n189\n190\n191\n192\n193\n194\n195\n196\n197\n198\n199\n200\n201\n202\n203\n204\n205\n206\n207\n208\n209\n210\n211\n212\n213\n214\n215\n216\n217\n218\n219\n220\n221\n222\n223\n224\n225\n226\n227\n228\n229\n230\n231\n232\n233\n234\n235\n236\n237\n238\n239\n240\n241\n242\n243\n244\n245\n246\n247\n248\n249\n250\n251\n252\n253\n254\n255\n256\n257\n258\n259\n260\n261\n262\n263\n264\n265\n266\n267\n268\n269\n270\n271\n272\n273\n274\n275\n276\n277\n278\n279\n280\n281\n282\n283\n284\n285\n286\n287\n288\n289\n290\n291\n292\n293\n294\n295\n296\n297\n298\n299\n300\n301\n302\n303\n304\n305\n306\n307\n308\n309\n310\n311\n312\n313\n314\n315\n316\n317\n318\n319\n320\n321\n322\n323\n324\n325\n326\n327\n328\n329\n330\n331\n332\n333\n334\n335\n336\n337\n338\n339\n340\n341\n342\n343\n344\n345\n346\n347\n348\n349\n350\n351\n352\n353\n354\n355\n356\n357\n358\n359\n360\n361\n362\n363\n364\n365\n366\n367\n368\n369\n370\n371\n372\n373\n374\n375\n376\n377\n378\n379\n380\n381\n382\n383\n384\n385\n386\n387\n388\n389\n390\n391\n392\n393\n394\n395\n396\n397\n398\n399\n400\n401\n402\n403\n404\n405\n406\n407\n408\n409\n410\n411\n412\n413\n414\n415\n416\n417\n418\n419\n420\n421\n422\n423\n424\n425\n426\n427\n428\n429\n430\n431\n432\n433\n434\n435\n436\n437\n438\n439\n440\n441\n442\n443\n444\n445\n446\n447\n448\n449\n450\n451\n452\n453\n454\n455\n456\n457\n458\n459\n460\n461\n462\n463\n464\n465\n466\n467\n468\n469\n470\n471\n472\n473\n474\n475\n476\n477\n478\n479\n480\n481\n482\n483\n484\n485\n486\n487\n488\n489\n490\n491\n492\n493\n494\n495\n496\n497\n498\n499\n500\n";

    LineNumberReader lnr;

    public void test_markI() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));
        String line;
        lnr.skip(80);
        lnr.mark(100);
        line = lnr.readLine();
        lnr.reset();
        assertTrue("Test 1: Failed to return to marked position.",
                line.equals(lnr.readLine()));

        lnr.close();
        try {
            lnr.mark(42);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }

        // The spec does not say the mark has to be invalidated
    }

    /**
     * java.io.LineNumberReader#read()
     */
    public void test_read() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));

        int c = lnr.read();
        assertEquals("Test 1: Read returned incorrect character;",
                '0', c);
        lnr.read();
        assertEquals("Test 2: Read failed to increase the line number;",
                1, lnr.getLineNumber());

        lnr.close();
        try {
            lnr.read();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * java.io.LineNumberReader#read(char[], int, int)
     */
    public void test_read$CII() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));
        char[] c = new char[100];
        lnr.read(c, 0, 4);
        assertTrue("Test 1: Read returned incorrect characters.", "0\n1\n"
                .equals(new String(c, 0, 4)));
        assertEquals("Test 2: Read failed to inc lineNumber",
                2, lnr.getLineNumber());

        lnr.close();
        try {
            lnr.read(c, 0, 4);
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read$CII_Exception() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));
        char[] c = new char[10];

        try {
            lnr.read(c, -1, 1);
            fail("IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            lnr.read(c, 0, -1);
            fail("IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            lnr.read(c, 10, 1);
            fail("IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
    }

    public void test_readLine() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));
        assertEquals("Returned incorrect line number", 0, lnr.getLineNumber());
        String line = null;
        lnr.readLine();
        line = lnr.readLine();
        assertEquals("Test 1: Returned incorrect string;", "1", line);
        assertTrue("Test 2: Returned incorrect line number:" + lnr.getLineNumber(),
                lnr.getLineNumber() == 2);

        lnr.close();
        try {
            lnr.readLine();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_reset() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));
        assertEquals("Test 1: Returned incorrect line number;",
                0, lnr.getLineNumber());
        String line = null;
        lnr.mark(100);
        lnr.readLine();
        lnr.reset();
        line = lnr.readLine();
        assertEquals("Test 2: Failed to reset reader", "0", line);

        lnr.mark(100);
        lnr.close();
        try {
            lnr.reset();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_skipJ() throws IOException {
        lnr = new LineNumberReader(new StringReader(text));
        char[] c = new char[100];
        long skipped = lnr.skip(80);
        assertEquals("Test 1: Incorrect number of characters skipped;",
                80, skipped);
        lnr.read(c, 0, 100);
        assertTrue("Test 2: Failed to skip to correct position.",
                text.substring(80, 180).equals(new String(c, 0, c.length)));

        try {
            lnr.skip(-1);
            fail("Test 3: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }

        lnr.close();
        try {
            lnr.skip(1);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }
}
