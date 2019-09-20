/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration.impl;

interface RecordWriter {
  boolean open(String title);
  boolean close();

  void bool(String name, boolean value);
  void boolArray(String name, boolean[] values);
  void character(String name, char value);
  void characterArray(String name, char[] values);
  void namedIndex(String name, String[] names, int value);
  void namedIndexArray(String name, String[] names, byte[] values);
  void string(String name, String value);
  void stringArray(String name, String[] values);
  void stringTable(String name, String[][] values);
}
