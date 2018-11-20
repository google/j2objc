/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
 
package android.icu.dev.test.translit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.Normalizer;
import android.icu.text.Transliterator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;

public class WriteCharts {
    public static void main(String[] args) throws IOException {
        if (false) {
            printSet("[[\u0000-\u007E \u30A1-\u30FC \uFF61-\uFF9F\u3001\u3002][:Katakana:][:Mark:]]");
        }
        String testSet = "";
        if (args.length == 0) args = getAllScripts();
        for (int i = 0; i < args.length; ++i) {
    // Enumeration enum = Transliterator.getAvailableIDs();
            if (args[i].startsWith("[")) {
                testSet = args[i];
            } else {
                print(testSet, args[i]);
                testSet = "";
            }
        }
    }
    
    public static void printSet(String source) {
        UnicodeSet s = new UnicodeSet(source);
        System.out.println("Printout for '" + source + "'");
        int count = s.getRangeCount();
        for (int i = 0; i < count; ++i) {
            int start = s.getRangeStart(i);
            int end = s.getRangeEnd(i);
            System.out.println(Integer.toString(start,16) + ".." + Integer.toString(end,16));
        }
    }
    
    public static String[] getAllScripts() {
        Set set = new TreeSet();
        int scripts[];
        Enumeration sources = Transliterator.getAvailableSources();
        while(sources.hasMoreElements()) {
            String source = (String) sources.nextElement();
            scripts = UScript.getCode(source);
            if (scripts == null) {
                System.out.println("[Skipping " + source + "]");
                continue;
            }
            int sourceScript = scripts[0];
            System.out.println("Source: " + source + ";\tScripts: " + showScripts(scripts));
            Enumeration targets = Transliterator.getAvailableTargets(source);
            while(targets.hasMoreElements()) {
                String target = (String) targets.nextElement();
                scripts = UScript.getCode(target);
                if (scripts == null
                        || priority(scripts[0]) < priority(sourceScript)) {
                    // skip doing both directions
                    System.out.println("[Skipping '" + source + "-" + target + "']");
                    continue;
                }
                System.out.println("\tTarget: " + target + ";\tScripts: " + showScripts(scripts));
                Enumeration variants = Transliterator.getAvailableVariants(source, target);
                while(variants.hasMoreElements()) {
                    String variant = (String) variants.nextElement();
                    String id = source + "-" + target;
                    if (variant.length() != 0) {
                        id += "/" + variant;
                        if (false) {
                            System.out.println("SKIPPING VARIANT, SINCE IT CURRENTLY BREAKS!\t" + id);
                            continue;
                        }
                    }
                    System.out.println("\t\t\t\tAdding: '" + id + "'");
                    set.add(id);
                }
            }
        }
        String[] results = new String[set.size()];
        set.toArray(results);
        return results;
    }
    
    static public int priority(int script) {
        if (script == UScript.LATIN) return -2;
        return script;
    }
    
    public static String showScripts(int[] scripts) {
        StringBuffer results = new StringBuffer();
        for (int i = 0; i < scripts.length; ++i) {
            if (i != 0) results.append(", ");
            results.append(UScript.getName(scripts[i]));
        }
        return results.toString();
    }
    
    public static void print(String testSet, String rawId) throws IOException {
        System.out.println("Processing " + rawId);
        Transliterator t = Transliterator.getInstance(rawId);
        String id = t.getID();
        
        // clean up IDs. Ought to be API for getting source, target, variant
        int minusPos = id.indexOf('-');
        String source = id.substring(0,minusPos);
        String target = id.substring(minusPos+1);
        int slashPos = target.indexOf('/');
        if (slashPos >= 0) target = target.substring(0,slashPos);
        
        // check that the source is a script
        if (testSet.equals("")) {
            int[] scripts = UScript.getCode(source);
            if (scripts == null) {
                System.out.println("FAILED: " 
                    + Transliterator.getDisplayName(id)
                    + " does not have a script as the source");
                return;
            } else {
                testSet = "[:" + source + ":]";
                if (source.equalsIgnoreCase("katakana")) {
                    testSet = "[" + testSet + "\u30FC]";
                    printSet(testSet);
                }
            }
        }
        UnicodeSet sourceSet = new UnicodeSet(testSet);

        // check that the target is a script
        int[] scripts = UScript.getCode(target);
        if (scripts == null) {
            target = "[:Latin:]";
        } else {
            target = "[:" + target + ":]";
        }
        UnicodeSet targetSet = new UnicodeSet(target);        
        
        Transliterator inverse = t.getInverse();
        
        //Transliterator hex = Transliterator.getInstance("Any-Hex");
        
                
        // iterate through script
        System.out.println("Transliterating " + sourceSet.toPattern(true) 
            + " with " + Transliterator.getDisplayName(id));
                
        UnicodeSet leftOverSet = new UnicodeSet(targetSet);
        UnicodeSet privateUse = new UnicodeSet("[:private use:]");
            
        Map map = new TreeMap();
        
        UnicodeSet targetSetPlusAnyways = new UnicodeSet(targetSet);
        targetSetPlusAnyways.addAll(okAnyway);
        
        UnicodeSet sourceSetPlusAnyways = new UnicodeSet(sourceSet);
        sourceSetPlusAnyways.addAll(okAnyway);
        
        UnicodeSetIterator usi = new UnicodeSetIterator(sourceSet);
        
        while (usi.next()) {
            int j = usi.codepoint;
            /*
        int count = sourceSet.getRangeCount();
        for (int i = 0; i < count; ++i) {
            int end = sourceSet.getRangeEnd(i);
            for (int j = sourceSet.getRangeStart(i); j <= end; ++j) {
            */
               // String flag = "";
                String ss = UTF16.valueOf(j);
                String ts = t.transliterate(ss);
                char group = 0;
                if (!targetSetPlusAnyways.containsAll(ts)) {
                    group |= 1;
                }
                if (UTF16.countCodePoint(ts) == 1) {
                    leftOverSet.remove(UTF16.charAt(ts,0));
                }
                String rt = inverse.transliterate(ts);
                if (!sourceSetPlusAnyways.containsAll(rt)) {
                    group |= 2;
                } else if (!ss.equals(rt)) {
                    group |= 4;
                }
                
                if (!privateUse.containsNone(ts) || !privateUse.containsNone(rt)) {
                    group |= 16;
                }
                    
                map.put(group + UCharacter.toLowerCase(Normalizer.normalize(ss, Normalizer.NFKD))
                        + "\u0000" + ss, 
                    "<td class='s'>" + ss + "<br><tt>" + hex(ss)
                        + "</tt></td><td class='t'>" + ts + "<br><tt>" + hex(ts)
                        + "</tt></td><td class='r'>" + rt + "<br><tt>" + hex(rt) + "</tt></td>" );
                
                // Check Duals
                /*
                int maxDual = 200;
              dual:
                for (int i2 = 0; i2 < count; ++i2) {
                    int end2 = sourceSet.getRangeEnd(i2);
                    for (int j2 = sourceSet.getRangeStart(i2); j2 <= end; ++j2) {
                        String ss2 = UTF16.valueOf(j2);
                        String ts2 = t.transliterate(ss2);
                        String rt2 = inverse.transliterate(ts2);
                        
                        String ss12 = ss + ss2;
                        String ts12 = t.transliterate(ss + ss12);
                        String rt12 = inverse.transliterate(ts12);
                        if (ts12.equals(ts + ts2) && rt12.equals(rt + rt2)) continue;   
                        if (--maxDual < 0) break dual;
                        
                        // transliteration of whole differs from that of parts
                        group = 0x100;
                        map.put(group + UCharacter.toLowerCase(Normalizer.normalize(ss12, Normalizer.DECOMP_COMPAT, 0))
                                + "\u0000" + ss12, 
                            "<td class='s'>" + ss12 + "<br><tt>" + hex(ss12)
                                + "</tt></td><td class='t'>" + ts12 + "<br><tt>" + hex(ts12)
                                + "</tt></td><td class='r'>" + rt12 + "<br><tt>" + hex(rt12) + "</tt></td>" );
                    }
                }
                */
            //}
        }
        
        
        leftOverSet.remove(0x0100,0x02FF); // remove extended & IPA
        
        /*int count = leftOverSet.getRangeCount();
        for (int i = 0; i < count; ++i) {
            int end = leftOverSet.getRangeEnd(i);
            for (int j = leftOverSet.getRangeStart(i); j <= end; ++j) {
            */
            
        usi.reset(leftOverSet);
        while (usi.next()) {
            int j = usi.codepoint;
            
                String ts = UTF16.valueOf(j);
                // String decomp = Normalizer.normalize(ts, Normalizer.DECOMP_COMPAT, 0);
                // if (!decomp.equals(ts)) continue;
                
                String rt = inverse.transliterate(ts);
                // String flag = "";
                char group = 0x80;
                    
                if (!sourceSetPlusAnyways.containsAll(rt)) {
                    group |= 8;
                }
                if (!privateUse.containsNone(rt)) {
                    group |= 16;
                }
                    
                map.put(group + UCharacter.toLowerCase(Normalizer.normalize(ts, Normalizer.NFKD)) + ts, 
                    "<td class='s'>-</td><td class='t'>" + ts + "<br><tt>" + hex(ts)
                    + "</tt></td><td class='r'>"
                    + rt + "<br><tt>" + hex(rt) + "</tt></td>");
            //}
        }

        // make file name and open
        File f = new File("transliteration/chart_" + id.replace('/', '_') + ".html");
        String filename = f.getCanonicalFile().toString();
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8"));
        //out.print('\uFEFF'); // BOM
        
        System.out.println("Writing " + filename);
        
        try {
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
            out.println("<HTML><HEAD>");
            out.println("<META content=\"text/html; charset=utf-8\" http-equiv=Content-Type></HEAD>");
            out.println("<link rel='stylesheet' href='http://www.unicode.org/charts/uca/charts.css' type='text/css'>");
            
            out.println("<BODY>");
            out.println("<h1>Transliteration Samples for '" + Transliterator.getDisplayName(id) + "'</h1>");
            out.println("<p>This file illustrates the transliterations of " + Transliterator.getDisplayName(id) + ".");
            out.println("The samples are mechanically generated, and only include single characters");
            out.println("from the source set. Thus it will <i>not</i> contain examples where the transliteration");
            out.println("depends on the context around the character. For a more detailed -- and interactive -- example, see the");
            out.println("<a href='http://demo.icu-project.org/icu-bin/translit'>Transliteration Demo</a></p><hr>");
            
            // set up the headers
            int columnCount = 3;
            String headerBase = "<th>Source</th><th>Target</th><th>Return</th>";
            String headers = headerBase;
            for (int i = columnCount - 1; i > 0; --i) {
                if (i != columnCount - 1) headers += "<th>&nbsp;</th>";
                headers += headerBase;
            }
            
            String tableHeader = "<p><table border='1'><tr>" + headers + "</tr>";
            String tableFooter = "</table></p>";
            out.println("<h2>Round Trip</h2>");
            out.println(tableHeader);
            
            Iterator it = map.keySet().iterator();
            char lastGroup = 0;
            int count = 0;
            int column = 0;
            while (it.hasNext()) {
                String key = (String) it.next();
                char group = key.charAt(0);
                if (group != lastGroup || count++ > 50) {
                    lastGroup = group;
                    count = 0;
                    if (column != 0) {
                        out.println("</tr>");
                        column = 0;
                    }
                    out.println(tableFooter);
                    
                    // String title = "";
                    if ((group & 0x100) != 0) out.println("<hr><h2>Duals</h2>");
                    else if ((group & 0x80) != 0) out.println("<hr><h2>Completeness</h2>");
                    else out.println("<hr><h2>Round Trip</h2>");
                    if ((group & 16) != 0) out.println("<h3>Errors: Contains Private Use Characters</h3>");
                    if ((group & 8) != 0) out.println("<h3>Possible Errors: Return not in Source Set</h3>");
                    if ((group & 4) != 0) out.println("<h3>One-Way Mapping: Return not equal to Source</h3>");
                    if ((group & 2) != 0) out.println("<h3>Errors: Return not in Source Set</h3>");
                    if ((group & 1) != 0) out.println("<h3>Errors: Target not in Target Set</h3>");
                                        
                    out.println(tableHeader);
                    column = 0;
                }
                String value = (String) map.get(key);
                if (column++ == 0) out.print("<tr>");
                else out.print("<th>&nbsp;</th>");
                out.println(value);
                if (column == 3) {
                    out.println("</tr>");
                    column = 0;
                }
            }
            if (column != 0) {
                out.println("</tr>");
                column = 0;
            }
            out.println(tableFooter + "</BODY></HTML>");
            
        } finally {
            out.close();
        }
    }
    
    public static String hex(String s) {
        int cp;
        StringBuffer results = new StringBuffer();
        for (int i = 0; i < s.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(s, i);
            if (i != 0) results.append(' ');
            results.append(Integer.toHexString(cp));
        }
        return results.toString().toUpperCase();
    }
    
    static final UnicodeSet okAnyway = new UnicodeSet("[^[:Letter:]]");
    
    /*
    // tests whether a string is in a set. Also checks for Common and Inherited
    public static boolean isIn(String s, UnicodeSet set) {
        int cp;
        for (int i = 0; i < s.length(); i += UTF16.getCharCount(i)) {
            cp = UTF16.charAt(s, i);
            if (set.contains(cp)) continue;
            if (okAnyway.contains(cp)) continue;
            return false;
        }
        return true;
    }
    */
    
}
  