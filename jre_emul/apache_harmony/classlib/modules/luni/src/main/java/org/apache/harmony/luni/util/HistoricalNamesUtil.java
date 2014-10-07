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
package org.apache.harmony.luni.util;

import java.util.HashMap;

public class HistoricalNamesUtil {
    private static HashMap<String, String> historicalNames = new HashMap<String, String>();

    static {
        historicalNames.put("Big5-HKSCS", "Big5_HKSCS");
        historicalNames.put("EUC-JP", "EUC_JP");
        historicalNames.put("EUC-KR", "EUC_KR");
        historicalNames.put("GB2312", "EUC_CN");
        historicalNames.put("IBM-Thai", "Cp838");
        historicalNames.put("IBM00858", "Cp858");
        historicalNames.put("IBM01140", "Cp1140");
        historicalNames.put("IBM01141", "Cp1141");
        historicalNames.put("IBM01142", "Cp1142");
        historicalNames.put("IBM01143", "Cp1143");
        historicalNames.put("IBM01144", "Cp1144");
        historicalNames.put("IBM01145", "Cp1145");
        historicalNames.put("IBM01146", "Cp1146");
        historicalNames.put("IBM01147", "Cp1147");
        historicalNames.put("IBM01148", "Cp1148");
        historicalNames.put("IBM01149", "Cp1149");
        historicalNames.put("IBM037", "Cp037");
        historicalNames.put("IBM1026", "Cp1026");
        historicalNames.put("IBM1047", "Cp1047");
        historicalNames.put("IBM273", "Cp273");
        historicalNames.put("IBM277", "Cp277");
        historicalNames.put("IBM278", "Cp278");
        historicalNames.put("IBM280", "Cp280");
        historicalNames.put("IBM284", "Cp284");
        historicalNames.put("IBM285", "Cp285");
        historicalNames.put("IBM297", "Cp297");
        historicalNames.put("IBM420", "Cp420");
        historicalNames.put("IBM424", "Cp424");
        historicalNames.put("IBM437", "Cp437");
        historicalNames.put("IBM500", "Cp500");
        historicalNames.put("IBM775", "Cp775");
        historicalNames.put("IBM850", "Cp850");
        historicalNames.put("IBM852", "Cp852");
        historicalNames.put("IBM855", "Cp855");
        historicalNames.put("IBM857", "Cp857");
        historicalNames.put("IBM860", "Cp860");
        historicalNames.put("IBM861", "Cp861");
        historicalNames.put("IBM862", "Cp862");
        historicalNames.put("IBM863", "Cp863");
        historicalNames.put("IBM864", "Cp864");
        historicalNames.put("IBM865", "Cp865");
        historicalNames.put("IBM866", "Cp866");
        historicalNames.put("IBM868", "Cp868");
        historicalNames.put("IBM869", "Cp869");
        historicalNames.put("IBM870", "Cp870");
        historicalNames.put("IBM871", "Cp871");
        historicalNames.put("IBM918", "Cp918");
        historicalNames.put("ISO-2022-CN", "ISO2022CN");
        historicalNames.put("ISO-2022-JP", "ISO2022JP");
        historicalNames.put("ISO-2022-KR", "ISO2022KR");
        historicalNames.put("ISO-8859-1", "ISO8859_1");
        historicalNames.put("ISO-8859-13", "ISO8859_13");
        historicalNames.put("ISO-8859-15", "ISO8859_15");
        historicalNames.put("ISO-8859-2", "ISO8859_2");
        historicalNames.put("ISO-8859-3", "ISO8859_3");
        historicalNames.put("ISO-8859-4", "ISO8859_4");
        historicalNames.put("ISO-8859-5", "ISO8859_5");
        historicalNames.put("ISO-8859-6", "ISO8859_6");
        historicalNames.put("ISO-8859-7", "ISO8859_7");
        historicalNames.put("ISO-8859-8", "ISO8859_8");
        historicalNames.put("ISO-8859-9", "ISO8859_9");
        historicalNames.put("KOI8-R", "KOI8_R");
        historicalNames.put("Shift_JIS", "SJIS");
        historicalNames.put("TIS-620", "TIS620");
        historicalNames.put("US-ASCII", "ASCII");
        historicalNames.put("UTF-16BE", "UnicodeBigUnmarked");
        historicalNames.put("UTF-16LE", "UnicodeLittleUnmarked");
        historicalNames.put("UTF-8", "UTF8");
        historicalNames.put("windows-1250", "Cp1250");
        historicalNames.put("windows-1251", "Cp1251");
        historicalNames.put("windows-1252", "Cp1252");
        historicalNames.put("windows-1253", "Cp1253");
        historicalNames.put("windows-1254", "Cp1254");
        historicalNames.put("windows-1255", "Cp1255");
        historicalNames.put("windows-1256", "Cp1256");
        historicalNames.put("windows-1257", "Cp1257");
        historicalNames.put("windows-1258", "Cp1258");
        historicalNames.put("windows-31j", "MS932");
        historicalNames.put("x-Big5-Solaris", "Big5_Solaris");
        historicalNames.put("x-euc-jp-linux", "EUC_JP_LINUX");
        historicalNames.put("x-EUC-TW", "EUC_TW");
        historicalNames.put("x-eucJP-Open", "EUC_JP_Solaris");
        historicalNames.put("x-IBM1006", "Cp1006");
        historicalNames.put("x-IBM1025", "Cp1025");
        historicalNames.put("x-IBM1046", "Cp1046");
        historicalNames.put("x-IBM1097", "Cp1097");
        historicalNames.put("x-IBM1098", "Cp1098");
        historicalNames.put("x-IBM1112", "Cp1112");
        historicalNames.put("x-IBM1122", "Cp1122");
        historicalNames.put("x-IBM1123", "Cp1123");
        historicalNames.put("x-IBM1124", "Cp1124");
        historicalNames.put("x-IBM1381", "Cp1381");
        historicalNames.put("x-IBM1383", "Cp1383");
        historicalNames.put("x-IBM33722", "Cp33722");
        historicalNames.put("x-IBM737", "Cp737");
        historicalNames.put("x-IBM856", "Cp856");
        historicalNames.put("x-IBM874", "Cp874");
        historicalNames.put("x-IBM875", "Cp875");
        historicalNames.put("x-IBM921", "Cp921");
        historicalNames.put("x-IBM922", "Cp922");
        historicalNames.put("x-IBM930", "Cp930");
        historicalNames.put("x-IBM933", "Cp933");
        historicalNames.put("x-IBM935", "Cp935");
        historicalNames.put("x-IBM937", "Cp937");
        historicalNames.put("x-IBM939", "Cp939");
        historicalNames.put("x-IBM942", "Cp942");
        historicalNames.put("x-IBM942C", "Cp942C");
        historicalNames.put("x-IBM943", "Cp943");
        historicalNames.put("x-IBM943C", "Cp943C");
        historicalNames.put("x-IBM948", "Cp948");
        historicalNames.put("x-IBM949", "Cp949");
        historicalNames.put("x-IBM949C", "Cp949C");
        historicalNames.put("x-IBM950", "Cp950");
        historicalNames.put("x-IBM964", "Cp964");
        historicalNames.put("x-IBM970", "Cp970");
        historicalNames.put("x-ISCII91", "ISCII91");
        historicalNames.put("x-ISO-2022-CN-CNS", "ISO2022CN");
        historicalNames.put("x-ISO-2022-CN-GB", "ISO2022CN");
        historicalNames.put("x-JISAutoDetect", "JISAutoDetect");
        historicalNames.put("x-MacArabic", "MacArabic");
        historicalNames.put("x-MacCentralEurope", "MacCentralEurope");
        historicalNames.put("x-MacCroatian", "MacCroatian");
        historicalNames.put("x-MacCyrillic", "MacCyrillic");
        historicalNames.put("x-MacDingbat", "MacDingbat");
        historicalNames.put("x-MacGreek", "MacGreek");
        historicalNames.put("x-MacHebrew", "MacHebrew");
        historicalNames.put("x-MacIceland", "MacIceland");
        historicalNames.put("x-MacRoman", "MacRoman");
        historicalNames.put("x-MacRomania", "MacRomania");
        historicalNames.put("x-MacSymbol", "MacSymbol");
        historicalNames.put("x-MacThai", "MacThai");
        historicalNames.put("x-MacTurkish", "MacTurkish");
        historicalNames.put("x-MacUkraine", "MacUkraine");
        historicalNames.put("x-MS950-HKSCS", "MS950_HKSCS");
        historicalNames.put("x-mswin-936", "MS936");
        historicalNames.put("x-PCK", "PCK");
        historicalNames.put("x-windows-874", "MS874");
        historicalNames.put("x-windows-949", "MS949");
        historicalNames.put("x-windows-950", "MS950");
    }

    public static String getHistoricalName(String name) {
        return (!historicalNames.containsKey(name) ? name : historicalNames
                .get(name));
    }
}
