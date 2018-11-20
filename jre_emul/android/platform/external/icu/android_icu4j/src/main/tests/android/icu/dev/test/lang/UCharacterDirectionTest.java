/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2013, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.lang;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UCharacterDirection;

/**
* Testing UCharacterDirection
* @author Syn Wee Quek
* @since July 22 2002
*/
public class UCharacterDirectionTest extends TestFmwk
{    
    // constructor -----------------------------------------------------------
    
    /**
    * Private constructor to prevent initialization
    */
    public UCharacterDirectionTest()
    {
    }
    
    // public methods --------------------------------------------------------
      
    /**
    * Gets the name of the argument category
    * @returns category name
    */
    @Test
    public void TestToString()
    {
        String name[] = {"Left-to-Right",
                         "Right-to-Left", 
                         "European Number",
                         "European Number Separator",
                         "European Number Terminator",
                         "Arabic Number",
                         "Common Number Separator",
                         "Paragraph Separator",
                         "Segment Separator",
                         "Whitespace",
                         "Other Neutrals",
                         "Left-to-Right Embedding",
                         "Left-to-Right Override",
                         "Right-to-Left Arabic",
                         "Right-to-Left Embedding",
                         "Right-to-Left Override",
                         "Pop Directional Format",
                         "Non-Spacing Mark",
                         "Boundary Neutral",
                         "First Strong Isolate",
                         "Left-to-Right Isolate",
                         "Right-to-Left Isolate",
                         "Pop Directional Isolate",
                         "Unassigned"};
        
        for (int i = UCharacterDirection.LEFT_TO_RIGHT;
            // Placed <= because we need to consider 'Unassigned'
            // when it goes out of bounds of UCharacterDirection
            i <= UCharacterDirection.CHAR_DIRECTION_COUNT; i++) {
             if (!UCharacterDirection.toString(i).equals(name[i])) {
                errln("Error toString for direction " + i + " expected " +
                      name[i]);
             }
        }
    }
}
