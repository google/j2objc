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

package org.apache.harmony.tests.java.lang;

import java.util.Locale;

import junit.framework.TestCase;

public class DoubleTest extends TestCase {
    private static final long rawBitsFor3_4en324ToN1[] = { 0x1L, 0x7L, 0x45L, 0x2b0L, 0x1ae2L,
            0x10cd1L, 0xa8028L, 0x69018dL, 0x41a0f7eL, 0x29049aedL, 0x19a2e0d44L,
            0x1005cc84acL, 0xa039fd2ebdL, 0x64243e3d361L, 0x3e96a6e641c6L, 0x271e284fe91b8L,
            0x1872d931f1b131L, 0x4e8f8f7e6e1d7dL, 0x8319b9af04d26eL, 0xb7e0281ac6070aL,
            0xedd832217788ccL, 0x122a71f54eab580L, 0x15750e72a2562e0L, 0x18d2520f4aebb98L,
            0x1c2373498ed353fL, 0x1f6c501bf28828eL, 0x22c76422ef2a332L, 0x261c9e95d57a5ffL,
            0x2963c63b4ad8f7fL, 0x2cbcb7ca1d8f35fL, 0x3015f2de527981bL, 0x335b6f95e717e22L,
            0x36b24b7b60dddabL, 0x3a0f6f2d1c8aa8bL, 0x3d534af863ad52dL, 0x40a81db67c98a79L,
            0x440912920ddf68bL, 0x474b5736915742eL, 0x4a9e2d0435ad13aL, 0x4e02dc22a18c2c4L,
            0x5143932b49ef375L, 0x549477f61c6b052L, 0x57f995f3a385c67L, 0x5b3bfdb846339c0L,
            0x5e8afd2657c0830L, 0x61edbc6fedb0a3dL, 0x653495c5f48e666L, 0x6881bb3771b1fffL,
            0x6be22a054e1e7ffL, 0x6f2d5a4350d30ffL, 0x7278b0d42507d3fL, 0x75d6dd092e49c8fL,
            0x79264a25bcee1daL, 0x7c6fdcaf2c29a50L, 0x7fcbd3daf7340e4L, 0x831f6468da8088eL,
            0x86673d831120ab2L, 0x89c10ce3d568d5fL, 0x8d18a80e656185bL, 0x905ed211feb9e72L,
            0x93b686967e6860eL, 0x9712141e0f013c9L, 0x9a56992592c18bbL, 0x9dac3f6ef771eeaL,
            0xa10ba7a55aa7352L, 0xa44e918eb151027L, 0xa7a235f25da5430L, 0xab0561b77a8749eL,
            0xae46ba2559291c6L, 0xb19868aeaf73637L, 0xb4fe82da5b503c5L, 0xb83f11c8791225bL,
            0xbb8ed63a9756af2L, 0xbef28bc93d2c5afL, 0xc237975dc63bb8dL, 0xc5857d3537caa70L,
            0xc8e6dc8285bd50cL, 0xcc3049d19396528L, 0xcf7c5c45f87be72L, 0xd2db7357769ae0eL,
            0xd6292816aa20cc9L, 0xd973721c54a8ffbL, 0xdcd04ea369d33faL, 0xe0223126222407cL,
            0xe36abd6faaad09bL, 0xe6c56ccb95584c2L, 0xea1b63ff3d572f9L, 0xed623cff0cacfb8L,
            0xf0bacc3ecfd83a5L, 0xf414bfa741e7247L, 0xf759ef911260ed9L, 0xfab06b7556f9290L,
            0xfe0e4329565bb9aL, 0x10151d3f3abf2a80L, 0x104a648f096ef520L, 0x10807ed965e55934L,
            0x10b49e8fbf5eaf81L, 0x10e9c633af365b61L, 0x11201be04d81f91dL, 0x115422d860e27764L,
            0x11892b8e791b153dL, 0x11bf76721761da8cL, 0x11f3aa074e9d2898L, 0x12289489224472beL,
            0x125eb9ab6ad58f6dL, 0x1293340b22c579a4L, 0x12c8010deb76d80dL, 0x12fe015166548e11L,
            0x1332c0d2dff4d8caL, 0x1367710797f20efdL, 0x139d4d497dee92bcL, 0x13d2504deeb51bb6L,
            0x1406e4616a6262a3L, 0x143c9d79c4fafb4cL, 0x1471e26c1b1cdd0fL, 0x14a65b0721e41453L,
            0x14dbf1c8ea5d1968L, 0x1511771d927a2fe1L, 0x1545d4e4f718bbd9L, 0x157b4a1e34deead0L,
            0x15b10e52e10b52c2L, 0x15e551e7994e2772L, 0x161aa6617fa1b14fL, 0x1650a7fcefc50ed1L,
            0x1684d1fc2bb65286L, 0x16ba067b36a3e727L, 0x16f0440d02267078L, 0x1724551042b00c96L,
            0x17596a54535c0fbcL, 0x178fc4e9683313abL, 0x17c3db11e11fec4bL, 0x17f8d1d65967e75eL,
            0x182f064befc1e135L, 0x186363ef75d92cc1L, 0x18983ceb534f77f1L, 0x18ce4c26282355eeL,
            0x1902ef97d91615b5L, 0x1937ab7dcf5b9b22L, 0x196d965d433281eaL, 0x19a27dfa49ff9132L,
            0x19d71d78dc7f757fL, 0x1a0ce4d7139f52dfL, 0x1a420f066c4393cbL, 0x1a7692c8075478beL,
            0x1aac377a092996edL, 0x1ae1a2ac45b9fe54L, 0x1b160b5757287de9L, 0x1b4b8e2d2cf29d64L,
            0x1b8138dc3c17a25eL, 0x1bb587134b1d8af6L, 0x1beae8d81de4edb4L, 0x1c20d18712af1490L,
            0x1c5505e8d75ad9b4L, 0x1c8a47630d319021L, 0x1cc06c9de83efa15L, 0x1cf487c5624eb89aL,
            0x1d29a9b6bae266c1L, 0x1d600a1234cd8038L, 0x1d940c96c200e046L, 0x1dc90fbc72811858L,
            0x1dff53ab8f215e6eL, 0x1e33944b3974db05L, 0x1e68795e07d211c6L, 0x1e9e97b589c69637L,
            0x1ed31ed1761c1de3L, 0x1f07e685d3a3255bL, 0x1f3de027488beeb2L, 0x1f72ac188d57752fL,
            0x1fa7571eb0ad527bL, 0x1fdd2ce65cd8a71aL, 0x20123c0ffa076870L, 0x2046cb13f889428cL,
            0x207c7dd8f6ab932fL, 0x20b1cea79a2b3bfeL, 0x20e6425180b60afdL, 0x211bd2e5e0e38dbcL,
            0x215163cfac8e3896L, 0x2185bcc397b1c6bbL, 0x21bb2bf47d9e386aL, 0x21f0fb78ce82e342L,
            0x22253a5702239c13L, 0x225a88ecc2ac8317L, 0x22909593f9abd1efL, 0x22c4baf8f816c66aL,
            0x22f9e9b7361c7805L, 0x2330321281d1cb03L, 0x23643e9722463dc4L, 0x23994e3cead7cd35L,
            0x23cfa1cc258dc082L, 0x2403c51f97789851L, 0x2438b6677d56be65L, 0x246ee4015cac6dffL,
            0x24a34e80d9ebc4bfL, 0x24d822211066b5efL, 0x250e2aa95480636bL, 0x2542daa9d4d03e23L,
            0x257791544a044dabL, 0x25ad75a95c856116L, 0x25e26989d9d35caeL, 0x261703ec504833d9L,
            0x264cc4e7645a40d0L, 0x2681fb109eb86882L, 0x26b679d4c66682a2L, 0x26ec1849f800234bL,
            0x27218f2e3b00160fL, 0x2755f2f9c9c01b93L, 0x278b6fb83c302277L, 0x27c125d3259e158bL,
            0x27f56f47ef059aedL, 0x282acb19eac701a8L, 0x2860bef032bc6109L, 0x2894eeac3f6b794cL,
            0x28ca2a574f46579eL, 0x29005a76918bf6c3L, 0x2934711435eef474L, 0x29698d59436ab191L,
            0x299ff0af94455df5L, 0x29d3f66dbcab5ab9L, 0x2a08f4092bd63167L, 0x2a3f310b76cbbdc1L,
            0x2a737ea72a3f5699L, 0x2aa85e50f4cf2c3fL, 0x2ade75e53202f74fL, 0x2b1309af3f41da91L,
            0x2b47cc1b0f125135L, 0x2b7dbf21d2d6e583L, 0x2bb2977523c64f72L, 0x2be73d526cb7e34eL,
            0x2c1d0ca707e5dc22L, 0x2c5227e864efa995L, 0x2c86b1e27e2b93faL, 0x2cbc5e5b1db678f9L,
            0x2cf1baf8f2920b9cL, 0x2d2629b72f368e83L, 0x2d5bb424fb043223L, 0x2d9150971ce29f56L,
            0x2dc5a4bce41b472bL, 0x2dfb0dec1d2218f6L, 0x2e30e8b392354f9aL, 0x2e6522e076c2a380L,
            0x2e9a6b9894734c61L, 0x2ed0833f5cc80fbcL, 0x2f04a40f33fa13abL, 0x2f39cd1300f89896L,
            0x2f70202be09b5f5eL, 0x2fa42836d8c23735L, 0x2fd932448ef2c503L, 0x300f7ed5b2af7643L,
            0x3043af458fada9eaL, 0x30789b16f3991465L, 0x30aec1dcb07f597eL, 0x30e33929ee4f97efL,
            0x3118077469e37deaL, 0x314e0951845c5d65L, 0x3182c5d2f2b9ba5fL, 0x31b77747af6828f7L,
            0x31ed55199b423335L, 0x3222553001096001L, 0x3256ea7c014bb801L, 0x328ca51b019ea601L,
            0x32c1e730e10327c1L, 0x32f660fd1943f1b1L, 0x332bf93c5f94ee1dL, 0x33617bc5bbbd14d2L,
            0x3395dab72aac5a07L, 0x33cb5164f5577089L, 0x340112df1956a655L, 0x34355796dfac4febL,
            0x346aad7c979763e5L, 0x34a0ac6ddebe9e6fL, 0x34d4d789566e460bL, 0x350a0d6bac09d78eL,
            0x354048634b8626b9L, 0x35745a7c1e67b067L, 0x35a9711b26019c81L, 0x35dfcd61ef8203a1L,
            0x3613e05d35b14245L, 0x3648d874831d92d6L, 0x367f0e91a3e4f78bL, 0x36b3691b066f1ab7L,
            0x36e84361c80ae165L, 0x371e543a3a0d99beL, 0x3752f4a464488017L, 0x3787b1cd7d5aa01cL,
            0x37bd9e40dcb14823L, 0x37f282e889eecd16L, 0x382723a2ac6a805cL, 0x385cec8b57852073L,
            0x389213d716b33448L, 0x38c698ccdc60015aL, 0x38fc3f00137801b0L, 0x3931a7600c2b010eL,
            0x396611380f35c151L, 0x399b9586130331a6L, 0x39d13d73cbe1ff08L, 0x3a058cd0beda7ec9L,
            0x3a3af004ee911e7cL, 0x3a70d603151ab30dL, 0x3aa50b83da615fd1L, 0x3ada4e64d0f9b7c5L,
            0x3b1070ff029c12dbL, 0x3b448d3ec3431792L, 0x3b79b08e7413dd76L, 0x3bb00e59088c6a6aL,
            0x3be411ef4aaf8504L, 0x3c19166b1d5b6646L, 0x3c4f5c05e4b23fd7L, 0x3c839983aeef67e6L,
            0x3cb87fe49aab41e0L, 0x3cee9fddc1561258L, 0x3d2323ea98d5cb77L, 0x3d57ece53f0b3e55L,
            0x3d8de81e8ece0deaL, 0x3dc2b1131940c8b2L, 0x3df75d57df90fadfL, 0x3e2d34add7753996L,
            0x3e6240eca6a943feL, 0x3e96d127d05394fdL, 0x3ecc8571c4687a3dL, 0x3f01d3671ac14c66L,
            0x3f364840e1719f80L, 0x3f6bda5119ce075fL, 0x3fa16872b020c49cL, 0x3fd5c28f5c28f5c3L,
            0x400B333333333333L };

    private static final long rawBitsFor1_2e0To309[] = { 0x3ff3333333333333L, 0x4028000000000000L,
            0x405e000000000000L, 0x4092c00000000000L, 0x40c7700000000000L, 0x40fd4c0000000000L,
            0x41324f8000000000L, 0x4166e36000000000L, 0x419c9c3800000000L, 0x41d1e1a300000000L,
            0x42065a0bc0000000L, 0x423bf08eb0000000L, 0x427176592e000000L, 0x42a5d3ef79800000L,
            0x42db48eb57e00000L, 0x43110d9316ec0000L, 0x434550f7dca70000L, 0x437aa535d3d0c000L,
            0x43b0a741a4627800L, 0x43e4d1120d7b1600L, 0x441a055690d9db80L, 0x445043561a882930L,
            0x4484542ba12a337cL, 0x44b969368974c05bL, 0x44efc3842bd1f072L, 0x4523da329b633647L,
            0x4558d0bf423c03d9L, 0x458f04ef12cb04cfL, 0x45c363156bbee301L, 0x45f83bdac6ae9bc2L,
            0x462e4ad1785a42b2L, 0x4662eec2eb3869afL, 0x4697aa73a606841bL, 0x46cd95108f882522L,
            0x47027d2a59b51735L, 0x47371c74f0225d03L, 0x476ce3922c2af443L, 0x47a20e3b5b9ad8aaL,
            0x47d691ca32818ed5L, 0x480c363cbf21f28aL, 0x4841a1e5f7753796L, 0x48760a5f7552857cL,
            0x48ab8cf752a726daL, 0x48e1381a93a87849L, 0x491586213892965bL, 0x494ae7a986b73bf1L,
            0x4980d0c9f4328577L, 0x49b504fc713f26d5L, 0x49ea463b8d8ef08aL, 0x4a206be538795656L,
            0x4a5486de8697abecL, 0x4a89a896283d96e6L, 0x4ac0095dd9267e50L, 0x4af40bb54f701de4L,
            0x4b290ea2a34c255dL, 0x4b5f524b4c1f2eb4L, 0x4b93936f0f937d31L, 0x4bc8784ad3785c7dL,
            0x4bfe965d8856739cL, 0x4c331dfa75360842L, 0x4c67e57912838a52L, 0x4c9dded757246ce6L,
            0x4cd2ab469676c410L, 0x4d0756183c147514L, 0x4d3d2b9e4b199259L, 0x4d723b42eeeffb78L,
            0x4da6ca13aaabfa56L, 0x4ddc7c989556f8ebL, 0x4e11cddf5d565b93L, 0x4e46415734abf278L,
            0x4e7bd1ad01d6ef15L, 0x4eb1630c2126556dL, 0x4ee5bbcf296feac9L, 0x4f1b2ac2f3cbe57bL,
            0x4f50fab9d85f6f6dL, 0x4f8539684e774b48L, 0x4fba87c262151e1aL, 0x4ff094d97d4d32d0L,
            0x5024ba0fdca07f84L, 0x5059e893d3c89f65L, 0x5090315c645d639fL, 0x50c43db37d74bc87L,
            0x50f94d205cd1eba9L, 0x512fa06874066693L, 0x5163c4414884001cL, 0x5198b5519aa50023L,
            0x51cee2a6014e402cL, 0x52034da7c0d0e81bL, 0x52382111b1052222L, 0x526e29561d466aabL,
            0x52a2d9d5d24c02abL, 0x52d7904b46df0355L, 0x530d745e1896c42bL, 0x534268bacf5e3a9bL,
            0x537702e98335c941L, 0x53acc3a3e4033b92L, 0x53e1fa466e82053bL, 0x541678d80a22868aL,
            0x544c170e0cab282cL, 0x54818e68c7eaf91cL, 0x54b5f202f9e5b763L, 0x54eb6e83b85f253bL,
            0x55212512533b7745L, 0x55556e56e80a5516L, 0x558ac9eca20cea5cL, 0x55c0be33e5481279L,
            0x55f4edc0de9a1718L, 0x562a293116409cdeL, 0x566059beade8620bL, 0x5694702e59627a8dL,
            0x56c98c39efbb1931L, 0x56ffef486ba9df7dL, 0x5733f58d434a2baeL, 0x5768f2f0941cb699L,
            0x579f2facb923e440L, 0x57d37dcbf3b66ea8L, 0x58085d3ef0a40a52L, 0x583e748eaccd0ce6L,
            0x587308d92c002810L, 0x58a7cb0f77003214L, 0x58ddbdd354c03e99L, 0x591296a414f82720L,
            0x59473c4d1a3630e8L, 0x597d0b6060c3bd21L, 0x59b2271c3c7a5635L, 0x59e6b0e34b98ebc2L,
            0x5a1c5d1c1e7f26b3L, 0x5a51ba31930f7830L, 0x5a8628bdf7d3563cL, 0x5abbb2ed75c82bcaL,
            0x5af14fd4699d1b5fL, 0x5b25a3c984046236L, 0x5b5b0cbbe5057ac4L, 0x5b90e7f56f236cbaL,
            0x5bc521f2caec47e9L, 0x5bfa6a6f7da759e3L, 0x5c308285ae88982eL, 0x5c64a3271a2abe39L,
            0x5c99cbf0e0b56dc8L, 0x5cd01f768c71649dL, 0x5d0427542f8dbdc4L, 0x5d3931293b712d35L,
            0x5d6f7d738a4d7882L, 0x5da3ae6836706b51L, 0x5dd89a02440c8626L, 0x5e0ec082d50fa7afL,
            0x5e433851c529c8ceL, 0x5e78066636743b01L, 0x5eae07ffc41149c1L, 0x5ee2c4ffda8ace19L,
            0x5f17763fd12d819fL, 0x5f4d53cfc578e207L, 0x5f825461db6b8d44L, 0x5fb6e97a52467095L,
            0x5feca3d8e6d80cbbL, 0x6021e667904707f5L, 0x605660017458c9f2L, 0x608bf801d16efc6eL,
            0x60c17b0122e55dc5L, 0x60f5d9c16b9eb536L, 0x612b5031c6866284L, 0x6161121f1c13fd92L,
            0x619556a6e318fcf7L, 0x61caac509bdf3c34L, 0x6200abb2616b85a1L, 0x6234d69ef9c66709L,
            0x626a0c46b83800cbL, 0x62a047ac3323007fL, 0x62d459973febc09fL, 0x63096ffd0fe6b0c6L,
            0x633fcbfc53e05cf8L, 0x6373df7db46c3a1bL, 0x63a8d75d218748a2L, 0x63df0d3469e91acaL,
            0x64136840c231b0beL, 0x64484250f2be1ceeL, 0x647e52e52f6da42aL, 0x64b2f3cf3da4869aL,
            0x64e7b0c30d0da840L, 0x651d9cf3d0511251L, 0x655282186232ab72L, 0x6587229e7abf564fL,
            0x65bceb46196f2be3L, 0x65f2130bcfe57b6eL, 0x662697cec3deda49L, 0x665c3dc274d690dbL,
            0x6691a69989061a89L, 0x66c6103feb47a12bL, 0x66fb944fe6198976L, 0x67313cb1efcff5eaL,
            0x67658bde6bc3f364L, 0x679aeed606b4f03dL, 0x67d0d545c4311626L, 0x68050a97353d5bb0L,
            0x683a4d3d028cb29cL, 0x687070462197efa2L, 0x68a48c57a9fdeb8aL, 0x68d9af6d947d666cL,
            0x69100da47cce6004L, 0x6944110d9c01f805L, 0x6979155103027606L, 0x69af5aa543c31387L,
            0x69e398a74a59ec35L, 0x6a187ed11cf06742L, 0x6a4e9e85642c8112L, 0x6a8323135e9bd0abL,
            0x6ab7ebd83642c4d6L, 0x6aede6ce43d3760cL, 0x6b22b040ea6429c7L, 0x6b575c5124fd3439L,
            0x6b8d33656e3c8147L, 0x6bc2401f64e5d0cdL, 0x6bf6d0273e1f4500L, 0x6c2c84310da71640L,
            0x6c61d29ea8886de8L, 0x6c96474652aa8962L, 0x6ccbd917e7552bbaL, 0x6d0167aef0953b54L,
            0x6d35c19aacba8a29L, 0x6d6b320157e92cb4L, 0x6da0ff40d6f1bbf0L, 0x6dd53f110cae2aedL,
            0x6e0a8ed54fd9b5a8L, 0x6e40994551e81189L, 0x6e74bf96a66215ebL, 0x6ea9ef7c4ffa9b66L,
            0x6ee035adb1fca120L, 0x6f1443191e7bc967L, 0x6f4953df661abbc1L, 0x6f7fa8d73fa16ab2L,
            0x6fb3c98687c4e2afL, 0x6fe8bbe829b61b5bL, 0x701eeae23423a232L, 0x705352cd6096455fL,
            0x70882780b8bbd6b7L, 0x70be3160e6eacc64L, 0x70f2dedc9052bfbfL, 0x71279693b4676faeL,
            0x715d7c38a1814b9aL, 0x71926da364f0cf40L, 0x71c7090c3e2d0310L, 0x71fccb4f4db843d4L,
            0x7231ff1190932a65L, 0x72667ed5f4b7f4feL, 0x729c1e8b71e5f23dL, 0x72d19317272fb766L,
            0x7305f7dcf0fba540L, 0x733b75d42d3a8e90L, 0x737129a49c44991aL, 0x73a5740dc355bf60L,
            0x73dad111342b2f39L, 0x7410c2aac09afd83L, 0x7444f35570c1bce4L, 0x747a302accf22c1dL,
            0x74b05e1ac0175b92L, 0x74e475a1701d3277L, 0x75199309cc247f15L, 0x754ff7cc3f2d9edaL,
            0x7583fadfa77c8348L, 0x75b8f997915ba41aL, 0x75ef37fd75b28d21L, 0x762382fe698f9834L,
            0x765863be03f37e41L, 0x768e7cad84f05dd2L, 0x76c30dec73163aa3L, 0x76f7d1678fdbc94cL,
            0x772dc5c173d2bb9fL, 0x77629b98e863b543L, 0x7797427f227ca294L, 0x77cd131eeb1bcb39L,
            0x78022bf352f15f04L, 0x7836b6f027adb6c5L, 0x786c64ac31992476L, 0x78a1beeb9effb6caL,
            0x78d62ea686bfa47cL, 0x790bba50286f8d9bL, 0x794154721945b881L, 0x7975a98e9f9726a1L,
            0x79ab13f2477cf049L, 0x79e0ec776cae162eL, 0x7a15279547d99bb9L, 0x7a4a717a99d002a8L,
            0x7a8086eca02201a9L, 0x7ab4a8a7c82a8213L, 0x7ae9d2d1ba352298L, 0x7b2023c31461359fL,
            0x7b542cb3d9798307L, 0x7b8937e0cfd7e3c8L, 0x7bbf85d903cddcbaL, 0x7bf3b3a7a260a9f4L,
            0x7c28a0918af8d472L, 0x7c5ec8b5edb7098eL, 0x7c933d71b49265f9L, 0x7cc80cce21b6ff77L,
            0x7cfe1001aa24bf55L, 0x7d32ca010a56f795L, 0x7d677c814cecb57aL, 0x7d9d5ba1a027e2d9L,
            0x7dd259450418edc7L, 0x7e06ef96451f2939L, 0x7e3cab7bd666f388L, 0x7e71eb2d66005835L,
            0x7ea665f8bf806e42L, 0x7edbff76ef6089d2L, 0x7f117faa559c5623L, 0x7f45df94eb036bacL,
            0x7f7b577a25c44697L, 0x7fb116ac579aac1fL, 0x7fe55c576d815726L, 0x7ff0000000000000L };

    private void doTestCompareRawBits(String originalDoubleString, long expectedRawBits,
            String expectedString) {
        double result;
        long rawBits;
        String convertedString;
        result = Double.parseDouble(originalDoubleString);
        rawBits = Double.doubleToLongBits(result);
        convertedString = new Double(result).toString();
        assertEquals(expectedRawBits, rawBits);
        assertEquals(expectedString.toLowerCase(Locale.US), convertedString
                .toLowerCase(Locale.US));
    }

    private void test_toString(double dd, String answer) {
        assertEquals(answer, Double.toString(dd));
        Double d = new Double(dd);
        assertEquals(answer, Double.toString(d.doubleValue()));
        assertEquals(answer, d.toString());
    }

    /**
     * java.lang.Double#Double(double)
     */
    public void test_ConstructorD() {
        Double d = new Double(39089.88888888888888888888888888888888);
        assertEquals("Created incorrect double", 39089.88888888888888888888888888888888, d
                .doubleValue(), 0D);
    }

    /**
     * java.lang.Double#Double(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        Double d = new Double("39089.88888888888888888888888888888888");
        assertEquals("Created incorrect double", 39089.88888888888888888888888888888888, d
                .doubleValue(), 0D);

        // Regression test for HARMONY-489
        try {
            d = new Double("1E+-20");
            fail("new Double(\"1E+-20\") should throw exception");
        } catch (NumberFormatException e) {
            // expected
        }

        // Regression test for HARMONY-329
        d = Double.parseDouble("-1.233999999999999965116738099630936817275852021384209929081813042837802886790127428328465579708849276001782791006814286802871737087810957327493372866733334925806221045495205250590286471187577636646208155890426896101636282423463443661040209738873506655844025580428394216030152374941053494694642722606658935546875E-112");
        assertEquals("Failed to parse long string", -1.234E-112D, d.doubleValue(), 0D);
    }

    /**
     * java.lang.Double#byteValue()
     */
    public void test_byteValue() {
        Double d = new Double(1923311.47712);
        assertEquals("Returned incorrect byte value", (byte) -17, d.byteValue());
    }

    /**
     * java.lang.Double#compareTo(java.lang.Double)
     * java.lang.Double#compare(double, double)
     */
    public void test_compare() {
        double[] values = new double[] { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -2d,
                -Double.MIN_VALUE, -0d, 0d, Double.MIN_VALUE, 2d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN };
        for (int i = 0; i < values.length; i++) {
            double d1 = values[i];
            assertTrue("compare() should be equal: " + d1, Double.compare(d1, d1) == 0);
            Double D1 = new Double(d1);
            assertTrue("compareTo() should be equal: " + d1, D1.compareTo(D1) == 0);
            for (int j = i + 1; j < values.length; j++) {
                double d2 = values[j];
                assertTrue("compare() " + d1 + " should be less " + d2,
                        Double.compare(d1, d2) == -1);
                assertTrue("compare() " + d2 + " should be greater " + d1, Double.compare(d2,
                        d1) == 1);
                Double D2 = new Double(d2);
                assertTrue("compareTo() " + d1 + " should be less " + d2,
                        D1.compareTo(D2) == -1);
                assertTrue("compareTo() " + d2 + " should be greater " + d1,
                        D2.compareTo(D1) == 1);
            }
        }

        try {
            new Double(0.0D).compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.Double#doubleToLongBits(double)
     */
    public void test_doubleToLongBitsD() {
        // Test for method long java.lang.Double.doubleToLongBits(double)
        Double d = new Double(Double.MAX_VALUE);
        long lbits = Double.doubleToLongBits(d.doubleValue());
        double r = Double.longBitsToDouble(lbits);

        assertTrue("Bit conversion failed", d.doubleValue() == r);
    }

    /**
     * java.lang.Double#doubleToRawLongBits(double)
     */
    public void test_doubleToRawLongBitsD() {
        long l = 0x7ff80000000004d2L;
        double d = Double.longBitsToDouble(l);
        assertTrue("Wrong raw bits", Double.doubleToRawLongBits(d) == l);
    }

    /**
     * java.lang.Double#doubleValue()
     */
    public void test_doubleValue() {
        assertEquals("Incorrect double value returned", 999999999999999.9999999999999,
                new Double(999999999999999.9999999999999).doubleValue(), 0D);
    }

    /**
     * java.lang.Double#floatValue()
     */
    public void test_floatValue() {
        // Test for method float java.lang.Double.floatValue()
        assertTrue(
                "Incorrect float value returned ",
                Math
                        .abs(new Double(999999999999999.9999999999999d).floatValue() - 999999999999999.9999999999999f) < 1);
    }

    /**
     * java.lang.Double#hashCode()
     */
    public void test_hashCode() {
        // Test for method int java.lang.Double.hashCode()
        for (int i = -1000; i < 1000; i++) {
            Double d = new Double(i);
            Double dd = new Double(i);
            assertTrue("Should not be identical ", d != dd);
            assertTrue("Should be equals 1 ", d.equals(dd));
            assertTrue("Should be equals 2 ", dd.equals(d));
            assertTrue("Should have identical values ", dd.doubleValue() == d.doubleValue());
            assertTrue("Invalid hash for equal but not identical doubles ", d.hashCode() == dd
                    .hashCode());
        }
        assertEquals("Magic assumption hasCode (0.0) = 0 failed", 0, new Double(0.0).hashCode());
    }

    /**
     * java.lang.Double#intValue()
     */
    public void test_intValue() {
        // Test for method int java.lang.Double.intValue()
        Double d = new Double(1923311.47712);
        assertEquals("Returned incorrect int value", 1923311, d.intValue());
    }

    /**
     * java.lang.Double#isInfinite()
     */
    public void test_isInfinite() {
        // Test for method boolean java.lang.Double.isInfinite()
        assertTrue("NEGATIVE_INFINITY returned false", new Double(Double.NEGATIVE_INFINITY)
                .isInfinite());
        assertTrue("POSITIVE_INFINITY returned false", new Double(Double.POSITIVE_INFINITY)
                .isInfinite());
        assertTrue("Non infinite number returned true", !(new Double(1000).isInfinite()));
    }

    /**
     * java.lang.Double#isInfinite(double)
     */
    public void test_isInfiniteD() {
        // Test for method boolean java.lang.Double.isInfinite(double)
        assertTrue("Infinity check failed", Double.isInfinite(Double.NEGATIVE_INFINITY)
                && (Double.isInfinite(Double.POSITIVE_INFINITY))
                && !(Double.isInfinite(Double.MAX_VALUE)));
    }

    /**
     * java.lang.Double#isNaN()
     */
    public void test_isNaN() {
        // Test for method boolean java.lang.Double.isNaN()
        Double d = new Double(0.0 / 0.0);
        assertTrue("NAN returned false", d.isNaN());
        d = new Double(0);
        assertTrue("Non NAN returned true", !d.isNaN());
    }

    /**
     * java.lang.Double#isNaN(double)
     */
    public void test_isNaND() {
        // Test for method boolean java.lang.Double.isNaN(double)

        Double d = new Double(0.0 / 0.0);
        assertTrue("NAN check failed", Double.isNaN(d.doubleValue()));
    }

    /**
     * java.lang.Double#longBitsToDouble(long)
     */
    public void test_longBitsToDoubleJ() {
        // Test for method double java.lang.Double.longBitsToDouble(long)

        Double d = new Double(Double.MAX_VALUE);
        long lbits = Double.doubleToLongBits(d.doubleValue());
        double r = Double.longBitsToDouble(lbits);

        assertTrue("Bit conversion failed", d.doubleValue() == r);
    }

    /**
     * java.lang.Double#longValue()
     */
    public void test_longValue() {
        // Test for method long java.lang.Double.longValue()
        Double d = new Double(1923311.47712);
        assertEquals("Returned incorrect long value", 1923311, d.longValue());
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDoubleLjava_lang_String() {
        assertEquals("Incorrect double returned, expected zero.", 0.0, Double
                .parseDouble("2.4703282292062327208828439643411e-324"), 0.0);
        assertTrue("Incorrect double returned, expected minimum double.", Double
                .parseDouble("2.4703282292062327208828439643412e-324") == Double.MIN_VALUE);

        for (int i = 324; i > 0; i--) {
            Double.parseDouble("3.4e-" + i);
        }
        for (int i = 0; i <= 309; i++) {
            Double.parseDouble("1.2e" + i);
        }

        /*
         * The first two cases and the last four cases have to placed outside
         * the loop due to the difference in the expected output string.
         */
        doTestCompareRawBits("3.4e-324", rawBitsFor3_4en324ToN1[0], "4.9e-324");
        doTestCompareRawBits("3.4e-323", rawBitsFor3_4en324ToN1[1], "3.5e-323");
        for (int i = 322; i > 3; i--) {
            String testString, expectedString;
            testString = expectedString = "3.4e-" + i;
            doTestCompareRawBits(testString, rawBitsFor3_4en324ToN1[324 - i], expectedString);
        }
        doTestCompareRawBits("3.4e-3", rawBitsFor3_4en324ToN1[321], "0.0034");
        doTestCompareRawBits("3.4e-2", rawBitsFor3_4en324ToN1[322], "0.034");
        doTestCompareRawBits("3.4e-1", rawBitsFor3_4en324ToN1[323], "0.34");
        doTestCompareRawBits("3.4e-0", rawBitsFor3_4en324ToN1[324], "3.4");

        doTestCompareRawBits("1.2e0", rawBitsFor1_2e0To309[0], "1.2");
        doTestCompareRawBits("1.2e1", rawBitsFor1_2e0To309[1], "12.0");
        doTestCompareRawBits("1.2e2", rawBitsFor1_2e0To309[2], "120.0");
        doTestCompareRawBits("1.2e3", rawBitsFor1_2e0To309[3], "1200.0");
        doTestCompareRawBits("1.2e4", rawBitsFor1_2e0To309[4], "12000.0");
        doTestCompareRawBits("1.2e5", rawBitsFor1_2e0To309[5], "120000.0");
        doTestCompareRawBits("1.2e6", rawBitsFor1_2e0To309[6], "1200000.0");
        for (int i = 7; i <= 308; i++) {
            String testString, expectedString;
            testString = expectedString = "1.2e" + i;
            doTestCompareRawBits(testString, rawBitsFor1_2e0To309[i], expectedString);
        }
        doTestCompareRawBits("1.2e309", rawBitsFor1_2e0To309[309], "Infinity");

        doTestCompareRawBits(
                "111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000.92233720368547758079223372036854775807",
                0x7e054218c295e43fL, "1.1122233344455567E299");
        doTestCompareRawBits(
                "-111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000111222333444555666777888999000.92233720368547758079223372036854775807",
                0xfe054218c295e43fL, "-1.1122233344455567E299");

        doTestCompareRawBits("1.234123412431233E107", 0x562ae7a25fe706ebL,
                "1.234123412431233E107");
        doTestCompareRawBits("1.2341234124312331E107", 0x562ae7a25fe706ecL,
                "1.2341234124312331E107");
        doTestCompareRawBits("1.2341234124312332E107", 0x562ae7a25fe706ecL,
                "1.2341234124312331E107");
        doTestCompareRawBits("-1.234123412431233E107", 0xd62ae7a25fe706ebL,
                "-1.234123412431233E107");
        doTestCompareRawBits("-1.2341234124312331E107", 0xd62ae7a25fe706ecL,
                "-1.2341234124312331E107");
        doTestCompareRawBits("-1.2341234124312332E107", 0xd62ae7a25fe706ecL,
                "-1.2341234124312331E107");

        doTestCompareRawBits("1e23", 0x44b52d02c7e14af6L, "1.0e23");

        /*
         * These particular tests verify that the extreme boundary conditions
         * are converted correctly.
         */
        doTestCompareRawBits("0.0e-309", 0L, "0.0");
        doTestCompareRawBits("-0.0e-309", 0x8000000000000000L, "-0.0");
        doTestCompareRawBits("0.0e309", 0L, "0.0");
        doTestCompareRawBits("-0.0e309", 0x8000000000000000L, "-0.0");
        doTestCompareRawBits("0.1e309", 0x7fe1ccf385ebc8a0L, "1.0e308");
        doTestCompareRawBits("0.2e309", 0x7ff0000000000000L, "Infinity");
        doTestCompareRawBits("65e-325", 1L, "4.9e-324");
        doTestCompareRawBits("1000e-326", 2L, "1.0e-323");

        doTestCompareRawBits("4.0e-306", 0x86789e3750f791L, "4.0e-306");
        doTestCompareRawBits("2.22507e-308", 0xffffe2e8159d0L, "2.22507e-308");
        doTestCompareRawBits(
                "111222333444555666777888999000111228999000.92233720368547758079223372036854775807",
                0x48746da623f1dd8bL, "1.1122233344455567E41");
        doTestCompareRawBits(
                "-111222333444555666777888999000111228999000.92233720368547758079223372036854775807",
                0xc8746da623f1dd8bL, "-1.1122233344455567E41");
        doTestCompareRawBits(
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890.987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210",
                0x54820fe0ba17f469L, "1.2345678901234567E99");
        doTestCompareRawBits(
                "-1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890.987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210",
                0xd4820fe0ba17f469L, "-1.2345678901234567E99");

        doTestCompareRawBits(
                "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.01",
                0x7fefffffffffffffL, "1.7976931348623157E308");
        doTestCompareRawBits(
                "-179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.01",
                0xffefffffffffffffL, "-1.7976931348623157E308");
        doTestCompareRawBits(
                "1112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001234567890",
                0x7ff0000000000000L, "Infinity");
        doTestCompareRawBits(
                "-1112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001112223334445556667778889990001234567890",
                0xfff0000000000000L, "-Infinity");
        doTestCompareRawBits(
                "179769313486231590000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.01",
                0x7ff0000000000000L, "Infinity");
        doTestCompareRawBits(
                "-179769313486231590000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.01",
                0xfff0000000000000L, "-Infinity");
        doTestCompareRawBits(
                "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0x2b392a32afcc661eL, "1.7976931348623157E-100");
        doTestCompareRawBits(
                "-0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0xab392a32afcc661eL, "-1.7976931348623157E-100");
        doTestCompareRawBits(
                "0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0x1b3432f0cb68e61L, "1.7976931348623157E-300");
        doTestCompareRawBits(
                "-0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0x81b3432f0cb68e61L, "-1.7976931348623157E-300");
        doTestCompareRawBits(
                "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0x2117b590b942L, "1.79769313486234E-310");
        doTestCompareRawBits(
                "-0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0x80002117b590b942L, "-1.79769313486234E-310");
        doTestCompareRawBits(
                "0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0xe37L, "1.798E-320");
        doTestCompareRawBits(
                "-0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017976931348623157",
                0x8000000000000e37L, "-1.798E-320");
        doTestCompareRawBits(
                "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
                0x2L, "1.0E-323");
        doTestCompareRawBits(
                "-0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
                0x8000000000000002L, "-1.0E-323");
        doTestCompareRawBits(
                "0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000055595409854908458349204328908234982349050934129878452378432452458968024357823490509341298784523784324524589680243578234905093412987845237843245245896802435782349050934129878452378432452458968024357868024357823490509341298784523784324524589680243578234905093412987845237843245245896802435786802435782349050934129878452378432452458968024357823490509341298784523784324524589680243578",
                0x1L, "4.9E-324");
        doTestCompareRawBits(
                "-0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000055595409854908458349204328908234982349050934129878452378432452458968024357823490509341298784523784324524589680243578234905093412987845237843245245896802435782349050934129878452378432452458968024357868024357823490509341298784523784324524589680243578234905093412987845237843245245896802435786802435782349050934129878452378432452458968024357823490509341298784523784324524589680243578",
                0x8000000000000001L, "-4.9E-324");
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_Illegal() {
        try {
            Double.parseDouble("0.0p0D");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble("+0x.p1d");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble("0Xg.gp1D");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble("-0x1.1p");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble("+0x 1.1 p2d");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble("x1.1p2d");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble(" 0x-2.1p2");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble(" 0x2.1pad");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }

        try {
            Double.parseDouble(" 0x111.222p 22d");
            fail("Should throw NumberFormatException.");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_FromHexString() {
        double actual;
        double expected;

        actual = Double.parseDouble("0x0.0p0D");
        assertEquals("Returned incorrect value", 0.0d, actual, 0.0D);

        actual = Double.parseDouble("0xa.ap+9d");
        assertEquals("Returned incorrect value", 5440.0d, actual, 0.0D);

        actual = Double.parseDouble("+0Xb.10ap8");
        assertEquals("Returned incorrect value", 2832.625d, actual, 0.0D);

        actual = Double.parseDouble("-0X.a0P2D");
        assertEquals("Returned incorrect value", -2.5d, actual, 0.0D);

        actual = Double.parseDouble("\r 0x22.1p2d \t");
        assertEquals("Returned incorrect value", 136.25d, actual, 0.0D);

        actual = Double.parseDouble("0x1.0p-1");
        assertEquals("Returned incorrect value", 0.5, actual, 0.0D);

        actual = Double
                .parseDouble("0x00000000000000000000000000000000001.0p-1");
        assertEquals("Returned incorrect value", 0.5, actual, 0.0D);

        actual = Double.parseDouble("0x1.0p-00000000000000000000000000001");
        assertEquals("Returned incorrect value", 0.5, actual, 0.0D);

        actual = Double.parseDouble("0x.100000000000000000000000000000000p1");
        assertEquals("Returned incorrect value", 0.125, actual, 0.0D);

        actual = Double.parseDouble("0x0.0p999999999999999999999999999999999999999999999999999999999999999");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);

        actual = Double.parseDouble("0xf1.0p9999999999999999999999999999999999999999999999999999999999999999");
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, actual, 0.0D);

        actual = Double.parseDouble("0xffffffffffffffffffffffffffffffffffff.ffffffffffffffffffffffffffffffffffffffffffffffp1");
        expected = Double.longBitsToDouble(0x4900000000000000L);
        assertEquals("Returned incorrect value", expected, actual, 0.0D);

        actual = Double.parseDouble("0x0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001p1600");
        expected = Double.longBitsToDouble(0x7f30000000000000L);
        assertEquals("Returned incorrect value", expected, actual, 0.0D);

        actual = Double.parseDouble("0x0.0p-999999999999999999999999999999999999999999999999999999");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);

        actual = Double.parseDouble("0xf1.0p-9999999999999999999999999999999999999999999999999999999999999999");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);

        actual = Double.parseDouble("0x10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000p-1600");
        expected = Double.longBitsToDouble(0xf0000000000000L);
        assertEquals("Returned incorrect value", expected, actual, 0.0D);

        actual = Double.parseDouble("0x1.p9223372036854775807");
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, actual, 0.0D);

        actual = Double.parseDouble("0x1.p9223372036854775808");
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, actual, 0.0D);

        actual = Double.parseDouble("0x10.p9223372036854775808");
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, actual, 0.0D);

        actual = Double.parseDouble("0xabcd.ffffffffp+2000");
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, actual, 0.0D);

        actual = Double.parseDouble("0x1.p-9223372036854775808");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);

        actual = Double.parseDouble("0x1.p-9223372036854775809");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);

        actual = Double.parseDouble("0x.1p-9223372036854775809");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);

        actual = Double.parseDouble("0xabcd.ffffffffffffffp-2000");
        assertEquals("Returned incorrect value", 0.0, actual, 0.0D);
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_NormalPositiveExponent() {
        long[] expecteds = {
                0x3f323456789abcdfL, 0x40e111012345678aL, 0x41a1110091a2b3c5L,
                0x4259998091a2b3c5L, 0x4311110048d159e2L, 0x43c5554048d159e2L,
                0x4479998048d159e2L, 0x452dddc048d159e2L, 0x45e111002468acf1L,
                0x469333202468acf1L, 0x4751011001234568L, 0x4802112101234568L,
                0x48b3213201234568L, 0x4964314301234568L, 0x4a15415401234568L,
                0x4ac6516501234568L, 0x4b77617601234568L, 0x4c28718701234568L,
                0x4cd9819801234568L, 0x4d9049048091a2b4L, 0x4e4101100091a2b4L,
                0x4ef189188091a2b4L, 0x4fa211210091a2b4L, 0x505299298091a2b4L,
                0x510321320091a2b4L, 0x51b3a93a8091a2b4L, 0x526431430091a2b4L,
                0x5314b94b8091a2b4L, 0x53c841840091a2b4L, 0x5478c98c8091a2b4L,
                0x552981980091a2b4L, 0x55da09a08091a2b4L, 0x568a91a90091a2b4L,
                0x573b19b18091a2b4L, 0x57eba1ba0091a2b4L, 0x589c29c28091a2b4L,
                0x594cb1cb0091a2b4L, 0x5a001d01c048d15aL, 0x5ab061060048d15aL,
                0x5b60a50a4048d15aL, 0x5c1101100048d15aL, 0x5cc145144048d15aL,
                0x5d7189188048d15aL, 0x5e21cd1cc048d15aL, 0x5ed211210048d15aL,
                0x5f8255254048d15aL, 0x603419418048d15aL, 0x60e45d45c048d15aL,
                0x6194a14a0048d15aL, 0x6244e54e4048d15aL, 0x62f541540048d15aL,
                0x63a585584048d15aL, 0x6455c95c8048d15aL, 0x65060d60c048d15aL,
                0x65b651650048d15aL, 0x666815814048d15aL, 0x671859858048d15aL,
                0x67c89d89c048d15aL, 0x6878e18e0048d15aL, 0x692925924048d15aL,
                0x69d981980048d15aL, 0x6a89c59c4048d15aL, 0x6b3a09a08048d15aL,
                0x6bea4da4c048d15aL, 0x6c9c11c10048d15aL, 0x6d4c55c54048d15aL,
                0x6dfc99c98048d15aL, 0x6eacddcdc048d15aL, 0x6f5d21d20048d15aL,
                0x700d65d64048d15aL, 0x70bdc1dc0048d15aL, 0x716e05e04048d15aL,
                0x721e49e48048d15aL, 0x72d00700602468adL, 0x73802902802468adL,
                0x74304b04a02468adL, 0x74e06d06c02468adL, 0x75908f08e02468adL,
                0x7640b10b002468adL, 0x76f0d30d202468adL, 0x77a10110002468adL,
                0x78512312202468adL, 0x79020520402468adL, 0x79b22722602468adL,
                0x7a624924802468adL, 0x7b126b26a02468adL, 0x7bc28d28c02468adL,
                0x7c72af2ae02468adL, 0x7d22d12d002468adL, 0x7dd2f32f202468adL,
                0x7e832132002468adL, 0x7f40011001012345L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L };

        for (int i = 0; i < expecteds.length; i++) {
            int part = i * 11;
            String inputString = "0x" + part + "." + part + "0123456789abcdefp" + part;

            double actual = Double.parseDouble(inputString);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputString
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_NormalNegativeExponent() {
        long[] expecteds = {
                0x3f323456789abcdfL, 0x3f8111012345678aL, 0x3ee1110091a2b3c5L,
                0x3e39998091a2b3c5L, 0x3d91110048d159e2L, 0x3ce5554048d159e2L,
                0x3c39998048d159e2L, 0x3b8dddc048d159e2L, 0x3ae111002468acf1L,
                0x3a3333202468acf1L, 0x3991011001234568L, 0x38e2112101234568L,
                0x3833213201234568L, 0x3784314301234568L, 0x36d5415401234568L,
                0x3626516501234568L, 0x3577617601234568L, 0x34c8718701234568L,
                0x3419819801234568L, 0x337049048091a2b4L, 0x32c101100091a2b4L,
                0x321189188091a2b4L, 0x316211210091a2b4L, 0x30b299298091a2b4L,
                0x300321320091a2b4L, 0x2f53a93a8091a2b4L, 0x2ea431430091a2b4L,
                0x2df4b94b8091a2b4L, 0x2d4841840091a2b4L, 0x2c98c98c8091a2b4L,
                0x2be981980091a2b4L, 0x2b3a09a08091a2b4L, 0x2a8a91a90091a2b4L,
                0x29db19b18091a2b4L, 0x292ba1ba0091a2b4L, 0x287c29c28091a2b4L,
                0x27ccb1cb0091a2b4L, 0x27201d01c048d15aL, 0x267061060048d15aL,
                0x25c0a50a4048d15aL, 0x251101100048d15aL, 0x246145144048d15aL,
                0x23b189188048d15aL, 0x2301cd1cc048d15aL, 0x225211210048d15aL,
                0x21a255254048d15aL, 0x20f419418048d15aL, 0x20445d45c048d15aL,
                0x1f94a14a0048d15aL, 0x1ee4e54e4048d15aL, 0x1e3541540048d15aL,
                0x1d8585584048d15aL, 0x1cd5c95c8048d15aL, 0x1c260d60c048d15aL,
                0x1b7651650048d15aL, 0x1ac815814048d15aL, 0x1a1859858048d15aL,
                0x19689d89c048d15aL, 0x18b8e18e0048d15aL, 0x180925924048d15aL,
                0x175981980048d15aL, 0x16a9c59c4048d15aL, 0x15fa09a08048d15aL,
                0x154a4da4c048d15aL, 0x149c11c10048d15aL, 0x13ec55c54048d15aL,
                0x133c99c98048d15aL, 0x128cddcdc048d15aL, 0x11dd21d20048d15aL,
                0x112d65d64048d15aL, 0x107dc1dc0048d15aL, 0xfce05e04048d15aL,
                0xf1e49e48048d15aL, 0xe700700602468adL, 0xdc02902802468adL,
                0xd104b04a02468adL, 0xc606d06c02468adL, 0xbb08f08e02468adL,
                0xb00b10b002468adL, 0xa50d30d202468adL, 0x9a10110002468adL,
                0x8f12312202468adL, 0x8420520402468adL, 0x7922722602468adL,
                0x6e24924802468adL, 0x6326b26a02468adL, 0x5828d28c02468adL,
                0x4d2af2ae02468adL, 0x422d12d002468adL, 0x372f32f202468adL,
                0x2c32132002468adL, 0x220011001012345L, 0x170121012012345L,
                0xc0231023012345L, 0x10341034012345L, 0x208a208a024L,
                0x41584158L, 0x83388L, 0x108L,
                0x0L, 0x0L, 0x0L,
                0x0L, 0x0L, 0x0L,
                0x0L, 0x0L, 0x0L,
                0x0L, 0x0L, 0x0L,
                0x0L, 0x0L };

        for (int i = 0; i < expecteds.length; i++) {
            int part = i * 11;
            String inputString = "0x" + part + "." + part + "0123456789abcdefp-" + part;

            double actual = Double.parseDouble(inputString);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputString
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_MaxNormalBoundary() {
        long[] expecteds = {
                0x7fefffffffffffffL, 0x7fefffffffffffffL, 0x7fefffffffffffffL,
                0x7fefffffffffffffL, 0x7fefffffffffffffL, 0x7fefffffffffffffL,
                0x7fefffffffffffffL, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,
                0x7ff0000000000000L, 0x7ff0000000000000L, 0x7ff0000000000000L,

                0xffefffffffffffffL, 0xffefffffffffffffL, 0xffefffffffffffffL,
                0xffefffffffffffffL, 0xffefffffffffffffL, 0xffefffffffffffffL,
                0xffefffffffffffffL, 0xfff0000000000000L, 0xfff0000000000000L,
                0xfff0000000000000L, 0xfff0000000000000L, 0xfff0000000000000L,
                0xfff0000000000000L, 0xfff0000000000000L, 0xfff0000000000000L };

        String[] inputs = {
                "0x1.fffffffffffffp1023",
                "0x1.fffffffffffff000000000000000000000000001p1023",
                "0x1.fffffffffffff1p1023",
                "0x1.fffffffffffff100000000000000000000000001p1023",
                "0x1.fffffffffffff1fffffffffffffffffffffffffffffffffffffffffffffp1023",
                "0x1.fffffffffffff7p1023",
                "0x1.fffffffffffff700000000000000000000000001p1023",
                "0x1.fffffffffffff8p1023",
                "0x1.fffffffffffff800000000000000000000000001p1023",
                "0x1.fffffffffffff8fffffffffffffffffffffffffffffffffffffffffffffp1023",
                "0x1.fffffffffffff9p1023",
                "0x1.fffffffffffff900000000000000000000000001p1023",
                "0x1.ffffffffffffffp1023",
                "0x1.ffffffffffffff00000000000000000000000001p1023",
                "0x1.fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp1023",

                "-0x1.fffffffffffffp1023",
                "-0x1.fffffffffffff000000000000000000000000001p1023",
                "-0x1.fffffffffffff1p1023",
                "-0x1.fffffffffffff100000000000000000000000001p1023",
                "-0x1.fffffffffffff1fffffffffffffffffffffffffffffffffffffffffffffp1023",
                "-0x1.fffffffffffff7p1023",
                "-0x1.fffffffffffff700000000000000000000000001p1023",
                "-0x1.fffffffffffff8p1023",
                "-0x1.fffffffffffff800000000000000000000000001p1023",
                "-0x1.fffffffffffff8fffffffffffffffffffffffffffffffffffffffffffffp1023",
                "-0x1.fffffffffffff9p1023",
                "-0x1.fffffffffffff900000000000000000000000001p1023",
                "-0x1.ffffffffffffffp1023",
                "-0x1.ffffffffffffff00000000000000000000000001p1023",
                "-0x1.fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp1023" };

        for (int i = 0; i < inputs.length; i++) {
            double actual = Double.parseDouble(inputs[i]);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputs[i]
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_MinNormalBoundary() {
        long[] expecteds = {
                0x10000000000000L, 0x10000000000000L, 0x10000000000000L,
                0x10000000000000L, 0x10000000000000L, 0x10000000000000L,
                0x10000000000000L, 0x10000000000000L, 0x10000000000001L,
                0x10000000000001L, 0x10000000000001L, 0x10000000000001L,
                0x10000000000001L, 0x10000000000001L, 0x10000000000001L,

                0x8010000000000000L, 0x8010000000000000L, 0x8010000000000000L,
                0x8010000000000000L, 0x8010000000000000L, 0x8010000000000000L,
                0x8010000000000000L, 0x8010000000000000L, 0x8010000000000001L,
                0x8010000000000001L, 0x8010000000000001L, 0x8010000000000001L,
                0x8010000000000001L, 0x8010000000000001L, 0x8010000000000001L };

        String[] inputs = {
                "0x1.0p-1022",
                "0x1.00000000000001p-1022",
                "0x1.000000000000010000000000000000001p-1022",
                "0x1.00000000000001fffffffffffffffffffffffffffffffffp-1022",
                "0x1.00000000000007p-1022",
                "0x1.000000000000070000000000000000001p-1022",
                "0x1.00000000000007fffffffffffffffffffffffffffffffffp-1022",
                "0x1.00000000000008p-1022",
                "0x1.000000000000080000000000000000001p-1022",
                "0x1.00000000000008fffffffffffffffffffffffffffffffffp-1022",
                "0x1.00000000000009p-1022",
                "0x1.000000000000090000000000000000001p-1022",
                "0x1.00000000000009fffffffffffffffffffffffffffffffffp-1022",
                "0x1.0000000000000fp-1022",
                "0x1.0000000000000ffffffffffffffffffffffffffffffffffp-1022",

                "-0x1.0p-1022",
                "-0x1.00000000000001p-1022",
                "-0x1.000000000000010000000000000000001p-1022",
                "-0x1.00000000000001fffffffffffffffffffffffffffffffffp-1022",
                "-0x1.00000000000007p-1022",
                "-0x1.000000000000070000000000000000001p-1022",
                "-0x1.00000000000007fffffffffffffffffffffffffffffffffp-1022",
                "-0x1.00000000000008p-1022",
                "-0x1.000000000000080000000000000000001p-1022",
                "-0x1.00000000000008fffffffffffffffffffffffffffffffffp-1022",
                "-0x1.00000000000009p-1022",
                "-0x1.000000000000090000000000000000001p-1022",
                "-0x1.00000000000009fffffffffffffffffffffffffffffffffp-1022",
                "-0x1.0000000000000fp-1022",
                "-0x1.0000000000000ffffffffffffffffffffffffffffffffffp-1022" };

        for (int i = 0; i < inputs.length; i++) {
            double actual = Double.parseDouble(inputs[i]);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputs[i]
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_MaxSubNormalBoundary() {
        long[] expecteds = {
                0xfffffffffffffL, 0xfffffffffffffL, 0xfffffffffffffL,
                0xfffffffffffffL, 0xfffffffffffffL, 0xfffffffffffffL,
                0xfffffffffffffL, 0x10000000000000L, 0x10000000000000L,
                0x10000000000000L, 0x10000000000000L, 0x10000000000000L,
                0x10000000000000L, 0x10000000000000L, 0x10000000000000L,

                0x800fffffffffffffL, 0x800fffffffffffffL, 0x800fffffffffffffL,
                0x800fffffffffffffL, 0x800fffffffffffffL, 0x800fffffffffffffL,
                0x800fffffffffffffL, 0x8010000000000000L, 0x8010000000000000L,
                0x8010000000000000L, 0x8010000000000000L, 0x8010000000000000L,
                0x8010000000000000L, 0x8010000000000000L, 0x8010000000000000L };

        String[] inputs = {
                "0x0.fffffffffffffp-1022",
                "0x0.fffffffffffff00000000000000000000000000000000001p-1022",
                "0x0.fffffffffffff1p-1022",
                "0x0.fffffffffffff10000000000000000000000000000000001p-1022",
                "0x0.fffffffffffff1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "0x0.fffffffffffff7p-1022",
                "0x0.fffffffffffff7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "0x0.fffffffffffff8p-1022",
                "0x0.fffffffffffff80000000000000000000000000000000001p-1022",
                "0x0.fffffffffffff8ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "0x0.fffffffffffff9p-1022",
                "0x0.fffffffffffff9ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "0x0.ffffffffffffffp-1022",
                "0x0.ffffffffffffff0000000000000000000000000000000001p-1022",
                "0x0.ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",

                "-0x0.fffffffffffffp-1022",
                "-0x0.fffffffffffff00000000000000000000000000000000001p-1022",
                "-0x0.fffffffffffff1p-1022",
                "-0x0.fffffffffffff10000000000000000000000000000000001p-1022",
                "-0x0.fffffffffffff1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "-0x0.fffffffffffff7p-1022",
                "-0x0.fffffffffffff7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "-0x0.fffffffffffff8p-1022",
                "-0x0.fffffffffffff80000000000000000000000000000000001p-1022",
                "-0x0.fffffffffffff8ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "-0x0.fffffffffffff9p-1022",
                "-0x0.fffffffffffff9ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022",
                "-0x0.ffffffffffffffp-1022",
                "-0x0.ffffffffffffff0000000000000000000000000000000001p-1022",
                "-0x0.ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffp-1022" };

        for (int i = 0; i < inputs.length; i++) {
            double actual = Double.parseDouble(inputs[i]);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputs[i]
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_MinSubNormalBoundary() {
        long[] expecteds = {
                0x1L, 0x1L, 0x2L,
                0x1L, 0x1L, 0x1L,
                0x2L, 0x2L, 0x2L,
                0x2L, 0x2L, 0x2L,
                0x2L, 0x2L, 0x2L,

                0x8000000000000001L, 0x8000000000000001L, 0x8000000000000002L,
                0x8000000000000001L, 0x8000000000000001L, 0x8000000000000001L,
                0x8000000000000002L, 0x8000000000000002L, 0x8000000000000002L,
                0x8000000000000002L, 0x8000000000000002L, 0x8000000000000002L,
                0x8000000000000002L, 0x8000000000000002L, 0x8000000000000002L };

        String[] inputs = {
                "0x0.0000000000001p-1022",
                "0x0.00000000000010000000000000000001p-1022",
                "0x0.0000000000001fffffffffffffffffffffffffffffffffp-1022",
                "0x0.00000000000017p-1022",
                "0x0.000000000000170000000000000000001p-1022",
                "0x0.00000000000017fffffffffffffffffffffffffffffffffp-1022",
                "0x0.00000000000018p-1022",
                "0x0.000000000000180000000000000000001p-1022",
                "0x0.00000000000018fffffffffffffffffffffffffffffffffp-1022",
                "0x0.00000000000019p-1022",
                "0x0.000000000000190000000000000000001p-1022",
                "0x0.00000000000019fffffffffffffffffffffffffffffffffp-1022",
                "0x0.0000000000001fp-1022",
                "0x0.0000000000001f0000000000000000001p-1022",
                "0x0.0000000000001ffffffffffffffffffffffffffffffffffp-1022",

                "-0x0.0000000000001p-1022",
                "-0x0.00000000000010000000000000000001p-1022",
                "-0x0.0000000000001fffffffffffffffffffffffffffffffffp-1022",
                "-0x0.00000000000017p-1022",
                "-0x0.000000000000170000000000000000001p-1022",
                "-0x0.00000000000017fffffffffffffffffffffffffffffffffp-1022",
                "-0x0.00000000000018p-1022",
                "-0x0.000000000000180000000000000000001p-1022",
                "-0x0.00000000000018fffffffffffffffffffffffffffffffffp-1022",
                "-0x0.00000000000019p-1022",
                "-0x0.000000000000190000000000000000001p-1022",
                "-0x0.00000000000019fffffffffffffffffffffffffffffffffp-1022",
                "-0x0.0000000000001fp-1022",
                "-0x0.0000000000001f0000000000000000001p-1022",
                "-0x0.0000000000001ffffffffffffffffffffffffffffffffffp-1022" };

        for (int i = 0; i < inputs.length; i++) {
            double actual = Double.parseDouble(inputs[i]);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputs[i]
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#parseDouble(java.lang.String)
     */
    public void test_parseDouble_LString_ZeroBoundary() {
        long[] expecteds = {
                0x0L, 0x0L, 0x0L,
                0x1L, 0x1L, 0x1L,
                0x1L, 0x1L, 0x1L,
                0x8000000000000000L, 0x8000000000000000L, 0x8000000000000000L,
                0x8000000000000001L, 0x8000000000000001L, 0x8000000000000001L,
                0x8000000000000001L, 0x8000000000000001L, 0x8000000000000001L };

        String[] inputs = {
                "0x0.00000000000004p-1022",
                "0x0.00000000000007ffffffffffffffffffffffp-1022",
                "0x0.00000000000008p-1022",
                "0x0.000000000000080000000000000000001p-1022",
                "0x0.00000000000008fffffffffffffffffffffffffffffffp-1022",
                "0x0.00000000000009p-1022",
                "0x0.000000000000090000000000000000001p-1022",
                "0x0.00000000000009fffffffffffffffffffffffffffffffffp-1022",
                "0x0.0000000000000fffffffffffffffffffffffffffffffffffp-1022",

                "-0x0.00000000000004p-1022",
                "-0x0.00000000000007ffffffffffffffffffffffp-1022",
                "-0x0.00000000000008p-1022",
                "-0x0.000000000000080000000000000000001p-1022",
                "-0x0.00000000000008fffffffffffffffffffffffffffffffp-1022",
                "-0x0.00000000000009p-1022",
                "-0x0.000000000000090000000000000000001p-1022",
                "-0x0.00000000000009fffffffffffffffffffffffffffffffffp-1022",
                "-0x0.0000000000000fffffffffffffffffffffffffffffffffffp-1022" };

        for (int i = 0; i < inputs.length; i++) {
            double actual = Double.parseDouble(inputs[i]);
            double expected = Double.longBitsToDouble(expecteds[i]);

            String expectedString = "0x" + Long.toHexString(Double.doubleToLongBits(expected));
            String actualString = "0x" + Long.toHexString(Double.doubleToLongBits(actual));
            String errorMsg = i + "th input string is:<" + inputs[i]
                    + ">.The expected result should be:<" + expectedString
                    + ">, but was: <" + actualString + ">. ";

            assertEquals(errorMsg, expected, actual, 0.0D);
        }
    }

    /**
     * java.lang.Double#shortValue()
     */
    public void test_shortValue() {
        // Test for method short java.lang.Double.shortValue()
        Double d = new Double(1923311.47712);
        assertEquals("Returned incorrect short value", 22767, d.shortValue());
    }

    /**
     * java.lang.Double#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.lang.Double.toString()
        test_toString(1.7976931348623157E308, "1.7976931348623157E308");
        test_toString(5.0E-4, "5.0E-4");
    }

    /**
     * java.lang.Double#toString(double)
     */
    public void test_toStringD() {
        // Test for method java.lang.String java.lang.Double.toString(double)
        test_toString(1.7976931348623157E308, "1.7976931348623157E308");
        test_toString(1.0 / 0.0, "Infinity");
        test_toString(0.0 / 0.0, "NaN");
        test_toString(-1.0 / 0.0, "-Infinity");

        double d;
        d = Double.longBitsToDouble(0x470fffffffffffffL);
        test_toString(d, "2.0769187434139308E34");
        d = Double.longBitsToDouble(0x4710000000000000L);
        test_toString(d, "2.076918743413931E34");

        d = Double.longBitsToDouble(0x470000000000000aL);
        test_toString(d, "1.0384593717069678E34");
        d = Double.longBitsToDouble(0x470000000000000bL);
        test_toString(d, "1.038459371706968E34");

        d = Double.longBitsToDouble(0x4700000000000017L);
        test_toString(d, "1.0384593717069708E34");
        d = Double.longBitsToDouble(0x4700000000000018L);
        test_toString(d, "1.038459371706971E34");

        d = Double.longBitsToDouble(0x4700000000000024L);
        test_toString(d, "1.0384593717069738E34");
        d = Double.longBitsToDouble(0x4700000000000025L);
        test_toString(d, "1.038459371706974E34");

        d = Double.longBitsToDouble(0x4700000000000031L);
        test_toString(d, "1.0384593717069768E34");
        d = Double.longBitsToDouble(0x4700000000000032L);
        test_toString(d, "1.038459371706977E34");

        d = Double.longBitsToDouble(0x470000000000003eL);
        test_toString(d, "1.0384593717069798E34");
        d = Double.longBitsToDouble(0x470000000000003fL);
        test_toString(d, "1.03845937170698E34");

        d = Double.longBitsToDouble(0x7e00000000000003L);
        test_toString(d, "8.371160993642719E298");
        d = Double.longBitsToDouble(0x7e00000000000004L);
        test_toString(d, "8.37116099364272E298");

        d = Double.longBitsToDouble(0x7e00000000000008L);
        test_toString(d, "8.371160993642728E298");
        d = Double.longBitsToDouble(0x7e00000000000009L);
        test_toString(d, "8.37116099364273E298");

        d = Double.longBitsToDouble(0x7e00000000000013L);
        test_toString(d, "8.371160993642749E298");
        d = Double.longBitsToDouble(0x7e00000000000014L);
        test_toString(d, "8.37116099364275E298");

        d = Double.longBitsToDouble(0x7e00000000000023L);
        test_toString(d, "8.371160993642779E298");
        d = Double.longBitsToDouble(0x7e00000000000024L);
        test_toString(d, "8.37116099364278E298");

        d = Double.longBitsToDouble(0x7e0000000000002eL);
        test_toString(d, "8.371160993642799E298");
        d = Double.longBitsToDouble(0x7e0000000000002fL);
        test_toString(d, "8.3711609936428E298");

        d = Double.longBitsToDouble(0xda00000000000001L);
        test_toString(d, "-3.3846065602060736E125");
        d = Double.longBitsToDouble(0xda00000000000002L);
        test_toString(d, "-3.384606560206074E125");

        d = Double.longBitsToDouble(0xda00000000000005L);
        test_toString(d, "-3.3846065602060766E125");
        d = Double.longBitsToDouble(0xda00000000000006L);
        test_toString(d, "-3.384606560206077E125");

        d = Double.longBitsToDouble(0xda00000000000009L);
        test_toString(d, "-3.3846065602060796E125");
        d = Double.longBitsToDouble(0xda0000000000000aL);
        test_toString(d, "-3.38460656020608E125");

        d = Double.longBitsToDouble(0xda0000000000000dL);
        test_toString(d, "-3.3846065602060826E125");
        d = Double.longBitsToDouble(0xda0000000000000eL);
        test_toString(d, "-3.384606560206083E125");
    }

    /**
     * java.lang.Double#valueOf(java.lang.String)
     */
    public void test_valueOfLjava_lang_String() {
        // Test for method java.lang.Double
        // java.lang.Double.valueOf(java.lang.String)
        assertTrue("Incorrect double returned", Math.abs(Double.valueOf("999999999999.999")
                .doubleValue() - 999999999999.999d) < 1);

        try {
            Double.valueOf(null);
            fail("Expected Double.valueOf(null) to throw NPE.");
        } catch (NullPointerException ex) {
            // expected
        }

        try {
            Double.valueOf("");
            fail("Expected Double.valueOf(\"\") to throw NFE");
        } catch (NumberFormatException e) {
            // expected
        }

        Double pi = Double.valueOf("3.141592654");
        assertEquals(3.141592654, pi.doubleValue(), 0D);

        Double posZero = Double.valueOf("+0.0");
        Double negZero = Double.valueOf("-0.0");
        assertFalse("Doubletest0", posZero.equals(negZero));

        // Tests for double values by name.
        Double expectedNaN = new Double(Double.NaN);

        Double posNaN = Double.valueOf("NaN");
        assertTrue("Doubletest1", posNaN.equals(expectedNaN));

        Double posNaNSigned = Double.valueOf("+NaN");
        assertTrue("Doubletest2", posNaNSigned.equals(expectedNaN));

        Double negNaNSigned = Double.valueOf("-NaN");
        assertTrue("Doubletest3", negNaNSigned.equals(expectedNaN));

        Double posInfinite = Double.valueOf("Infinity");
        assertTrue("Doubletest4", posInfinite.equals(new Double(Double.POSITIVE_INFINITY)));

        Double posInfiniteSigned = Double.valueOf("+Infinity");
        assertTrue("Doubletest5", posInfiniteSigned
                .equals(new Double(Double.POSITIVE_INFINITY)));

        Double negInfiniteSigned = Double.valueOf("-Infinity");
        assertTrue("Doubletest6", negInfiniteSigned
                .equals(new Double(Double.NEGATIVE_INFINITY)));
    }

    /**
     * java.lang.Double#compareTo(java.lang.Double)
     * java.lang.Double#compare(double, double)
     */
    public void test_compareToLjava_lang_Double() {
        // A selection of double values in ascending order.
        double[] values = new double[] { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -2d,
                -Double.MIN_VALUE, -0d, 0d, Double.MIN_VALUE, 2d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN };
        for (int i = 0; i < values.length; i++) {
            double d1 = values[i];

            // Test that each value compares equal to itself; and each object is
            // equal to another object like itself.
            assertTrue("Assert 0: compare() should be equal: " + d1,
                    Double.compare(d1, d1) == 0);
            Double objDouble = new Double(d1);
            assertTrue("Assert 1: compareTo() should be equal: " + d1, objDouble
                    .compareTo(objDouble) == 0);

            // Test that the Double-defined order is respected
            for (int j = i + 1; j < values.length; j++) {
                double d2 = values[j];
                assertTrue("Assert 2: compare() " + d1 + " should be less " + d2, Double
                        .compare(d1, d2) == -1);
                assertTrue("Assert 3: compare() " + d2 + " should be greater " + d1, Double
                        .compare(d2, d1) == 1);
                Double D2 = new Double(d2);
                assertTrue("Assert 4: compareTo() " + d1 + " should be less " + d2, objDouble
                        .compareTo(D2) == -1);
                assertTrue("Assert 5: compareTo() " + d2 + " should be greater " + d1, D2
                        .compareTo(objDouble) == 1);
            }
        }

        try {
            new Double(0.0D).compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.Double#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        Double d1 = new Double(87654321.12345d);
        Double d2 = new Double(87654321.12345d);
        Double d3 = new Double(0.0002f);
        assertTrue("Assert 0: Equality test failed", d1.equals(d2) && !(d1.equals(d3)));

        assertTrue("Assert 2: NaN should not be == Nan", Double.NaN != Double.NaN);
        assertTrue("Assert 3: NaN should not be == Nan", new Double(Double.NaN)
                .equals(new Double(Double.NaN)));
        assertTrue("Assert 4: -0d should be == 0d", 0d == -0d);
        assertTrue("Assert 5: -0d should not be equals() 0d", !new Double(0d)
                .equals(new Double(-0d)));

        Double dmax = new Double(Double.MAX_VALUE);
        Double dmax1 = new Double(Double.MAX_VALUE);

        assertTrue("Equality test failed", dmax.equals(dmax1) && !(dmax.equals(new Object())));
    }

    /**
     * java.lang.Double#toHexString(double)
     */
    public void test_toHexStringF() {
        // the follow values come from the Double Javadoc/Spec
        assertEquals("0x0.0p0", Double.toHexString(0.0D));
        assertEquals("-0x0.0p0", Double.toHexString(-0.0D));
        assertEquals("0x1.0p0", Double.toHexString(1.0D));
        assertEquals("-0x1.0p0", Double.toHexString(-1.0D));
        assertEquals("0x1.0p1", Double.toHexString(2.0D));
        assertEquals("0x1.8p1", Double.toHexString(3.0D));
        assertEquals("0x1.0p-1", Double.toHexString(0.5D));
        assertEquals("0x1.0p-2", Double.toHexString(0.25D));
        assertEquals("0x1.fffffffffffffp1023", Double.toHexString(Double.MAX_VALUE));
        assertEquals("0x0.0000000000001p-1022", Double.toHexString(Double.MIN_VALUE));

        // test edge cases
        assertEquals("NaN", Double.toHexString(Double.NaN));
        assertEquals("-Infinity", Double.toHexString(Double.NEGATIVE_INFINITY));
        assertEquals("Infinity", Double.toHexString(Double.POSITIVE_INFINITY));

        // test various numbers
        assertEquals("-0x1.da8p6", Double.toHexString(-118.625D));
        assertEquals("0x1.2957874cccccdp23", Double.toHexString(9743299.65D));
        assertEquals("0x1.2957874cccccdp23", Double.toHexString(9743299.65000D));
        assertEquals("0x1.2957874cccf63p23", Double.toHexString(9743299.650001234D));
        assertEquals("0x1.700d1061d3333p33", Double.toHexString(12349743299.65000D));

        // test HARMONY-2132
        assertEquals("0x1.01p10", Double.toHexString(0x1.01p10));
    }

    /**
     * java.lang.Double#valueOf(double)
     */
    public void test_valueOfD() {
        assertEquals(new Double(Double.MIN_VALUE), Double.valueOf(Double.MIN_VALUE));
        assertEquals(new Double(Double.MAX_VALUE), Double.valueOf(Double.MAX_VALUE));
        assertEquals(new Double(0), Double.valueOf(0));

        int s = -128;
        while (s < 128) {
            assertEquals(new Double(s), Double.valueOf(s));
            assertEquals(new Double(s + 0.1D), Double.valueOf(s + 0.1D));
            s++;
        }
    }

    /**
     * {@link java.lang.Double#MAX_EXPONENT}
     * @since 1.6
     */
    public void test_MAX_EXPONENT() {
        assertTrue("Wrong value of java.lang.Double.MAX_EXPONENT",
                Double.MAX_EXPONENT == 1023);
        assertTrue("Wrong value of java.lang.Double.MAX_EXPONENT",
                Double.MAX_EXPONENT == Math.getExponent(Double.MAX_VALUE));
    }

    /**
     * {@link java.lang.Double#MIN_EXPONENT}
     * @since 1.6
     */
    public void test_MIN_EXPONENT() {
        assertTrue("Wrong value of java.lang.Double.MIN_EXPONENT",
                Double.MIN_EXPONENT == -1022);
        assertTrue("Wrong value of java.lang.Double.MIN_EXPONENT",
                Double.MIN_EXPONENT == Math.getExponent(Double.MIN_NORMAL));
    }

    /**
     * {@link java.lang.Double#MIN_NORMAL}
     * @since 1.6
     */
    public void test_MIN_NORMAL() {
        assertTrue("Wrong value of java.lang.Double.MIN_NORMAL",
                Double.MIN_NORMAL == 0x1.0p-1022);
        assertTrue("Wrong value of java.lang.Double.MIN_NORMAL",
                Double.MIN_NORMAL == Double
                        .longBitsToDouble(0x0010000000000000L));
        assertTrue("Wrong value of java.lang.Double.MIN_NORMAL",
                Double.MIN_NORMAL == 2.2250738585072014E-308);
    }
}
