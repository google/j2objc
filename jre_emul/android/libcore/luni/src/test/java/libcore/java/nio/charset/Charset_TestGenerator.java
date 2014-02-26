/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * Super class for concrete charset test suites.
 */
public class Charset_TestGenerator {

    Charset charset;
    CharsetDecoder decoder;
    CharsetEncoder encoder;


    static final int[] codes = {
        9, 12, 28, 31, 48, 51, 54, 57, 65, 68, 71, 74, 77, 80, 83, 86,
        89, 97, 100, 103, 106, 109, 112, 115, 118, 121, 170, 181, 186, 192, 195, 198,
        201, 204, 207, 210, 213, 216, 219, 222, 225, 228, 231, 234, 237, 240, 243, 246,
        249, 252, 255, 258, 261, 264, 267, 270, 273, 276, 279, 282, 285, 288, 291, 294,
        297, 300, 303, 306, 309, 312, 315, 318, 321, 324, 327, 330, 333, 336, 339, 342,
        345, 348, 351, 354, 357, 360, 363, 366, 369, 372, 375, 378, 381, 384, 387, 390,
        393, 396, 399, 402, 405, 408, 411, 414, 417, 420, 423, 426, 429, 432, 435, 438,
        441, 444, 447, 450, 453, 456, 459, 462, 465, 468, 471, 474, 477, 480, 483, 486,
        489, 492, 495, 498, 501, 504, 507, 510, 513, 516, 519, 522, 525, 528, 531, 534,
        537, 540, 543, 546, 549, 552, 555, 558, 561, 564, 592, 595, 598, 601, 604, 607,
        610, 613, 616, 619, 622, 625, 628, 631, 634, 637, 640, 643, 646, 649, 652, 655,
        658, 661, 664, 667, 670, 673, 676, 679, 682, 685, 688, 691, 694, 697, 700, 703,
        710, 713, 716, 719, 736, 739, 750, 890, 902, 905, 908, 911, 914, 917, 920, 923,
        926, 929, 932, 935, 938, 941, 944, 947, 950, 953, 956, 959, 962, 965, 968, 971,
        974, 977, 980, 983, 986, 989, 992, 995, 998, 1001, 1004, 1007, 1010, 1013, 1016, 1019,
        1024, 1027, 1060, 1093, 1126, 1162, 1195, 1228, 1261, 1294, 1329, 1362, 1395, 1488, 1521, 1569,
        1602, 1635, 1668, 1701, 1734, 1774, 1808, 1869, 1920, 1953, 2308, 2341, 2384, 2437, 2470, 2524,
        2565, 2598, 2649, 2693, 2726, 2768, 2821, 2854, 2908, 2947, 2980, 3047, 3080, 3114, 3168, 3205,
        3238, 3294, 3333, 3366, 3424, 3461, 3494, 3585, 3618, 3651, 3713, 3746, 3779, 3840, 3873, 3906,
        3939, 3976, 4096, 4129, 4162, 4256, 4289, 4322, 4355, 4388, 4421, 4454, 4487, 4520, 4553, 4586,
        4619, 4652, 4685, 4718, 4752, 4786, 4819, 4852, 4885, 4918, 4951, 5024, 5057, 5090, 5123, 5156,
        5189, 5222, 5255, 5288, 5321, 5354, 5387, 5420, 5453, 5486, 5519, 5552, 5585, 5618, 5651, 5684,
        5717, 5750, 5783, 5816, 5849, 5888, 5921, 5954, 5987, 6020, 6053, 6103, 6158, 6191, 6224, 6257,
        6290, 6400, 6470, 6503, 7424, 7457, 7490, 7523, 7680, 7713, 7746, 7779, 7812, 7845, 7878, 7911,
        7944, 7977, 8010, 8043, 8076, 8109, 8144, 8178, 8232, 8287, 8450, 8484, 8517, 12288, 12337, 12370,
        12403, 12436, 12469, 12502, 12535, 12568, 12601, 12634, 12667, 12704, 12784, 13312, 13345, 13378, 13411, 13444,
        13477, 13510, 13543, 13576, 13609, 13642, 13675, 13708, 13741, 13774, 13807, 13840, 13873, 13906, 13939, 13972,
        14005, 14038, 14071, 14104, 14137, 14170, 14203, 14236, 14269, 14302, 14335, 14368, 14401, 14434, 14467, 14500,
        14533, 14566, 14599, 14632, 14665, 14698, 14731, 14764, 14797, 14830, 14863, 14896, 14929, 14962, 14995, 15028,
        15061, 15094, 15127, 15160, 15193, 15226, 15259, 15292, 15325, 15358, 15391, 15424, 15457, 15490, 15523, 15556,
        15589, 15622, 15655, 15688, 15721, 15754, 15787, 15820, 15853, 15886, 15919, 15952, 15985, 16018, 16051, 16084,
        16117, 16150, 16183, 16216, 16249, 16282, 16315, 16348, 16381, 16414, 16447, 16480, 16513, 16546, 16579, 16612,
        16645, 16678, 16711, 16744, 16777, 16810, 16843, 16876, 16909, 16942, 16975, 17008, 17041, 17074, 17107, 17140,
        17173, 17206, 17239, 17272, 17305, 17338, 17371, 17404, 17437, 17470, 17503, 17536, 17569, 17602, 17635, 17668,
        17701, 17734, 17767, 17800, 17833, 17866, 17899, 17932, 17965, 17998, 18031, 18064, 18097, 18130, 18163, 18196,
        18229, 18262, 18295, 18328, 18361, 18394, 18427, 18460, 18493, 18526, 18559, 18592, 18625, 18658, 18691, 18724,
        18757, 18790, 18823, 18856, 18889, 18922, 18955, 18988, 19021, 19054, 19087, 19120, 19153, 19186, 19219, 19252,
        19285, 19318, 19351, 19384, 19417, 19450, 19483, 19516, 19549, 19582, 19615, 19648, 19681, 19714, 19747, 19780,
        19813, 19846, 19879, 19968, 20001, 20034, 20067, 20100, 20133, 20166, 20199, 20232, 20265, 20298, 20331, 20364,
        20397, 20430, 20463, 20496, 20529, 20562, 20595, 20628, 20661, 20694, 20727, 20760, 20793, 20826, 20859, 20892,
        20925, 20958, 20991, 21024, 21057, 21090, 21123, 21156, 21189, 21222, 21255, 21288, 21321, 21354, 21387, 21420,
        21453, 21486, 21519, 21552, 21585, 21618, 21651, 21684, 21717, 21750, 21783, 21816, 21849, 21882, 21915, 21948,
        21981, 22014, 22047, 22080, 22113, 22146, 22179, 22212, 22245, 22278, 22311, 22344, 22377, 22410, 22443, 22476,
        22509, 22542, 22575, 22608, 22641, 22674, 22707, 22740, 22773, 22806, 22839, 22872, 22905, 22938, 22971, 23004,
        23037, 23070, 23103, 23136, 23169, 23202, 23235, 23268, 23301, 23334, 23367, 23400, 23433, 23466, 23499, 23532,
        23565, 23598, 23631, 23664, 23697, 23730, 23763, 23796, 23829, 23862, 23895, 23928, 23961, 23994, 24027, 24060,
        24093, 24126, 24159, 24192, 24225, 24258, 24291, 24324, 24357, 24390, 24423, 24456, 24489, 24522, 24555, 24588,
        24621, 24654, 24687, 24720, 24753, 24786, 24819, 24852, 24885, 24918, 24951, 24984, 25017, 25050, 25083, 25116,
        25149, 25182, 25215, 25248, 25281, 25314, 25347, 25380, 25413, 25446, 25479, 25512, 25545, 25578, 25611, 25644,
        25677, 25710, 25743, 25776, 25809, 25842, 25875, 25908, 25941, 25974, 26007, 26040, 26073, 26106, 26139, 26172,
        26205, 26238, 26271, 26304, 26337, 26370, 26403, 26436, 26469, 26502, 26535, 26568, 26601, 26634, 26667, 26700,
        26733, 26766, 26799, 26832, 26865, 26898, 26931, 26964, 26997, 27030, 27063, 27096, 27129, 27162, 27195, 27228,
        27261, 27294, 27327, 27360, 27393, 27426, 27459, 27492, 27525, 27558, 27591, 27624, 27657, 27690, 27723, 27756,
        27789, 27822, 27855, 27888, 27921, 27954, 27987, 28020, 28053, 28086, 28119, 28152, 28185, 28218, 28251, 28284,
        28317, 28350, 28383, 28416, 28449, 28482, 28515, 28548, 28581, 28614, 28647, 28680, 28713, 28746, 28779, 28812,
        28845, 28878, 28911, 28944, 28977, 29010, 29043, 29076, 29109, 29142, 29175, 29208, 29241, 29274, 29307, 29340,
        29373, 29406, 29439, 29472, 29505, 29538, 29571, 29604, 29637, 29670, 29703, 29736, 29769, 29802, 29835, 29868,
        29901, 29934, 29967, 30000, 30033, 30066, 30099, 30132, 30165, 30198, 30231, 30264, 30297, 30330, 30363, 30396,
        30429, 30462, 30495, 30528, 30561, 30594, 30627, 30660, 30693, 30726, 30759, 30792, 30825, 30858, 30891, 30924,
        30957, 30990, 31023, 31056, 31089, 31122, 31155, 31188, 31221, 31254, 31287, 31320, 31353, 31386, 31419, 31452,
        31485, 31518, 31551, 31584, 31617, 31650, 31683, 31716, 31749, 31782, 31815, 31848, 31881, 31914, 31947, 31980,
        32013, 32046, 32079, 32112, 32145, 32178, 32211, 32244, 32277, 32310, 32343, 32376, 32409, 32442, 32475, 32508,
        32541, 32574, 32607, 32640, 32673, 32706, 32739
    };
    static final char[] chars = OldCharset_AbstractTest.theseChars(codes);

    static abstract class CodesGenerator {
        int row = 0, col = 0;
        abstract void consume (int code);

        boolean isAccepted (int code)
        {
            return Character.isLetterOrDigit(code);
        }
    }

    static class CodesGenerator1 extends CodesGenerator {
        @Override
        void consume (int code) {
            System.out.print(code);
            System.out.print(", ");
            col++;
            if (col == 16) {
                System.out.println();
                row++;
                col = 0;
            }
        }

        @Override
        boolean isAccepted (int code)
        {
            return Character.isLetterOrDigit(code) || Character.isWhitespace(code);
        }
    }

    static class CodesGenerator2 extends CodesGenerator {
        @Override
        void consume (int code) {
//            System.out.print(code);
//            System.out.print(", ");
            System.out.print((char) code);
            col++;
            if (col == 80) {
                System.out.println();
                row++;
                col = 0;
            }
        }
    }

    static class CodesGenerator3 extends CodesGenerator {
        char[] buf = new char[8];

        @Override
        void consume (int code) {
            buf[col] = (char) code;
            System.out.print(code);
            System.out.print(", ");
            col++;
            if (col == 8) {
                System.out.print(" // ");
                System.out.println(buf);
                row++;
                col = 0;
            }
        }

        @Override
        boolean isAccepted (int code)
        {
            return Character.isLetterOrDigit(code);
//                    || Character.isWhitespace(code);
        }
    }

    static void genCodes () {
        CodesGenerator gen = new CodesGenerator1();
        int code = 0;
        while (code < (1 << 10)) {
            while (!gen.isAccepted(code)) code ++;
            gen.consume(code);
            code += 3;
        }
        while (code < (1 << 15)) {
            while (!gen.isAccepted(code)) code ++;
            gen.consume(code);
            code += 33;
        }
    }


    static abstract class Dumper {
        int row = 0, col = 0;
        abstract void consume (int code);
    }

    static class Dumper1 extends Dumper {
        int colMax;

        Dumper1 () {
            colMax = 16;
        }

        Dumper1 (int colums) {
            colMax = colums;
        }

        @Override
        void consume (int code) {
            System.out.printf("0x%x, ", code);
            col++;
            if (col == colMax) {
                System.out.println();
                row++;
                col = 0;
            }
        }
    }

    static void genEncoded (Charset charset, CharBuffer cb) {
        System.out.println(charset.name());
        Dumper out = new Dumper1();
        CharsetEncoder encoder = charset.newEncoder();
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        try {
            ByteBuffer bb = encoder.encode(cb);
//            bb.rewind();
            while (bb.hasRemaining()) {
                out.consume(bb.get());
            }
        } catch (CharacterCodingException e) {
            System.out.println(e);
//                e.printStackTrace();
        }
    }

    static void genDecoded (Charset charset, ByteBuffer bb) {
        CharsetDecoder decoder = charset.newDecoder();
//        System.out.println(code);
//        bytes[0] = (byte) code;
//        System.out.println(bytes[0]);
//        ByteBuffer inputBB = ByteBuffer.wrap(bytes);
//        CharBuffer outputCB;
//        try {
//            outputCB = decoder.decode(inputBB);
//            outputCB.rewind();
//            System.out.println(outputCB);
//        } catch (CharacterCodingException e) {
//            System.out.println(e);
////                e.printStackTrace();
//        }
    }


    public static void main(String[] args) {
//        charset = Charset.defaultCharset();
//        decoder = charset.newDecoder();
//        System.out.println(charset.name());
        genEncoded(Charset.forName("MacRoman"), CharBuffer.wrap(chars));
//        genEncoded(Charset.forName(charsetNames[37]), CharBuffer.wrap(chars));
    }

}
