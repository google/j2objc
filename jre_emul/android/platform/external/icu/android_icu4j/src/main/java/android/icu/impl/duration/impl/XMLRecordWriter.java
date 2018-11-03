/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 2007-2010, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 */

package android.icu.impl.duration.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import android.icu.lang.UCharacter;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class XMLRecordWriter implements RecordWriter {
    private Writer w;
    private List<String> nameStack;

    public XMLRecordWriter(Writer w) {
        this.w = w;
        this.nameStack = new ArrayList<String>();
    }

    @Override
    public boolean open(String title) {
        newline();
        writeString("<" + title + ">");
        nameStack.add(title);
        return true;
    }

    @Override
    public boolean close() {
        int ix = nameStack.size() - 1;
        if (ix >= 0) {
            String name = nameStack.remove(ix);
            newline();
            writeString("</" + name + ">");
            return true;
        }
        return false;
    }

    public void flush() {
        try {
            w.flush();
        } catch (IOException e) {
        }
    }

    @Override
    public void bool(String name, boolean value) {
        internalString(name, String.valueOf(value));
    }

    @Override
    public void boolArray(String name, boolean[] values) {
        if (values != null) {
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; ++i) {
                stringValues[i] = String.valueOf(values[i]);
            }
            stringArray(name, stringValues);
        }
    }

    private static String ctos(char value) {
        if (value == '<') {
            return "&lt;";
        }
        if (value == '&') {
            return "&amp;";
        }
        return String.valueOf(value);
    }

    @Override
    public void character(String name, char value) {
        if (value != '\uffff') {
            internalString(name, ctos(value));
        }
    }

    @Override
    public void characterArray(String name, char[] values) {
        if (values != null) {
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; ++i) {
                char value = values[i];
                if (value == '\uffff') {
                    stringValues[i] = NULL_NAME;
                } else {
                    stringValues[i] = ctos(value);
                }
            }
            internalStringArray(name, stringValues);
        }
    }

    @Override
    public void namedIndex(String name, String[] names, int value) {
        if (value >= 0) {
            internalString(name, names[value]);
        }
    }

    @Override
    public void namedIndexArray(String name, String[] names, byte[] values) {
        if (values != null) {
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; ++i) {
                int value = values[i];
                if (value < 0) {
                    stringValues[i] = NULL_NAME;
                } else {
                    stringValues[i] = names[value];
                }
            }
            internalStringArray(name, stringValues);
        }
    }

    public static String normalize(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = null;
        boolean inWhitespace = false;
        char c = '\0';
        boolean special = false;
        for (int i = 0; i < str.length(); ++i) {
            c = str.charAt(i);
            if (UCharacter.isWhitespace(c)) {
                if (sb == null && (inWhitespace || c != ' ')) {
                    sb = new StringBuilder(str.substring(0, i));
                }
                if (inWhitespace) {
                    continue;
                }
                inWhitespace = true;
                special = false;
                c = ' ';
            } else {
                inWhitespace = false;
                special = c == '<' || c == '&';
                if (special && sb == null) {
                    sb = new StringBuilder(str.substring(0, i));
                }
            }
            if (sb != null) {
                if (special) {
                    sb.append(c == '<' ? "&lt;" : "&amp;");
                } else {
                    sb.append(c);
                }
            }
        }
        if (sb != null) {
            /*
             * if (c == ' ') { int len = sb.length(); if (len == 0) { return
             * " "; } if (len > 1 && c == ' ') { sb.deleteCharAt(len - 1); } }
             */
            return sb.toString();
        }
        return str;
    }

    private void internalString(String name, String normalizedValue) {
        if (normalizedValue != null) {
            newline();
            writeString("<" + name + ">" + normalizedValue + "</" + name + ">");
        }
    }

    private void internalStringArray(String name, String[] normalizedValues) {
        if (normalizedValues != null) {
            push(name + "List");
            for (int i = 0; i < normalizedValues.length; ++i) {
                String value = normalizedValues[i];
                if (value == null) {
                    value = NULL_NAME;
                }
                string(name, value);
            }
            pop();
        }
    }

    @Override
    public void string(String name, String value) {
        internalString(name, normalize(value));
    }

    @Override
    public void stringArray(String name, String[] values) {
        if (values != null) {
            push(name + "List");
            for (int i = 0; i < values.length; ++i) {
                String value = normalize(values[i]);
                if (value == null) {
                    value = NULL_NAME;
                }
                internalString(name, value);
            }
            pop();
        }
    }

    @Override
    public void stringTable(String name, String[][] values) {
        if (values != null) {
            push(name + "Table");
            for (int i = 0; i < values.length; ++i) {
                String[] rowValues = values[i];
                if (rowValues == null) {
                    internalString(name + "List", NULL_NAME);
                } else {
                    stringArray(name, rowValues);
                }
            }
            pop();
        }
    }

    private void push(String name) {
        newline();
        writeString("<" + name + ">");
        nameStack.add(name);
    }

    private void pop() {
        int ix = nameStack.size() - 1;
        String name = nameStack.remove(ix);
        newline();
        writeString("</" + name + ">");
    }

    private void newline() {
        writeString("\n");
        for (int i = 0; i < nameStack.size(); ++i) {
            writeString(INDENT);
        }
    }

    private void writeString(String str) {
        if (w != null) {
            try {
                w.write(str);
            } catch (IOException e) {
                // if there's a problem, record it and stop writing
                System.err.println(e.getMessage());
                w = null;
            }
        }
    }

    static final String NULL_NAME = "Null";
    private static final String INDENT = "    ";
}
