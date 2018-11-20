/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
 /*
  *******************************************************************************
  * Copyright (C) 2005-2008, International Business Machines Corporation and    *
  * others. All Rights Reserved.                                                *
  *******************************************************************************
  */
package android.icu.dev.data.resources;

import java.util.ListResourceBundle;

public class TestDataElements extends ListResourceBundle {    
    private static Object[][] data = new Object[][] { 
        {    
            "from_root",
            "This data comes from root"
        },
        {
            "from_en",
            "In root should be overridden"
        },
        { 
            "from_en_Latn",
            "In root should be overridden"
        },
        {
            "from_en_Latn_US",
            "In root should be overridden"
        }
        
    };
    protected Object[][] getContents() {
        return data;
    }
}
