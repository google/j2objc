/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1996-2013, International Business Machines Corporation and
* others. All Rights Reserved.
*******************************************************************************
*/

package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterDirection;

/**
 * Enumerated Unicode character linguistic direction constants.
 * Used as return results from <a href=UCharacter.html>UCharacter</a>
 * <p>
 * This class is not subclassable
 * </p>
 * @author Syn Wee Quek
 */

public final class UCharacterDirection implements ECharacterDirection {

    // private constructor =========================================
    ///CLOVER:OFF  
    /**
     * Private constructor to prevent initialisation
     */
    private UCharacterDirection()
    {
    }
    ///CLOVER:ON
      
    /**
     * Gets the name of the argument direction
     * @param dir direction type to retrieve name
     * @return directional name
     */
    public static String toString(int dir) {
        switch(dir)
            {
            case LEFT_TO_RIGHT :
                return "Left-to-Right";
            case RIGHT_TO_LEFT :
                return "Right-to-Left";
            case EUROPEAN_NUMBER :
                return "European Number";
            case EUROPEAN_NUMBER_SEPARATOR :
                return "European Number Separator";
            case EUROPEAN_NUMBER_TERMINATOR :
                return "European Number Terminator";
            case ARABIC_NUMBER :
                return "Arabic Number";
            case COMMON_NUMBER_SEPARATOR :
                return "Common Number Separator";
            case BLOCK_SEPARATOR :
                return "Paragraph Separator";
            case SEGMENT_SEPARATOR :
                return "Segment Separator";
            case WHITE_SPACE_NEUTRAL :
                return "Whitespace";
            case OTHER_NEUTRAL :
                return "Other Neutrals";
            case LEFT_TO_RIGHT_EMBEDDING :
                return "Left-to-Right Embedding";
            case LEFT_TO_RIGHT_OVERRIDE :
                return "Left-to-Right Override";
            case RIGHT_TO_LEFT_ARABIC :
                return "Right-to-Left Arabic";
            case RIGHT_TO_LEFT_EMBEDDING :
                return "Right-to-Left Embedding";
            case RIGHT_TO_LEFT_OVERRIDE :
                return "Right-to-Left Override";
            case POP_DIRECTIONAL_FORMAT :
                return "Pop Directional Format";
            case DIR_NON_SPACING_MARK :
                return "Non-Spacing Mark";
            case BOUNDARY_NEUTRAL :
                return "Boundary Neutral";
            case FIRST_STRONG_ISOLATE:
                return "First Strong Isolate";
            case LEFT_TO_RIGHT_ISOLATE:
                return "Left-to-Right Isolate";
            case RIGHT_TO_LEFT_ISOLATE:
                return "Right-to-Left Isolate";
            case POP_DIRECTIONAL_ISOLATE:
                return "Pop Directional Isolate";
            }
        return "Unassigned";
    }
}
