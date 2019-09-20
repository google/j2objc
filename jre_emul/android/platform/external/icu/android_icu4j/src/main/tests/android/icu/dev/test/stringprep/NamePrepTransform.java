/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.stringprep;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.icu.impl.ICUResourceBundle;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterDirection;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;
import android.icu.text.UnicodeSet;

/**
 * @author ram
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NamePrepTransform {
    
    private static final NamePrepTransform transform = new NamePrepTransform();
    
    private UnicodeSet labelSeparatorSet;
    private UnicodeSet prohibitedSet;
    private UnicodeSet unassignedSet;
    private MapTransform mapTransform;
    public static final int NONE = 0;
    public static final int ALLOW_UNASSIGNED = 1;
    
    private NamePrepTransform(){
        // load the resource bundle
        ICUResourceBundle bundle = (ICUResourceBundle)ICUResourceBundle.getBundleInstance("android/icu/dev/data/testdata","idna_rules", NamePrepTransform.class.getClassLoader(), true);
        String  mapRules      = bundle.getString("MapNoNormalization");
        mapRules             += bundle.getString("MapNFKC");
        // disable
        mapTransform          = new MapTransform("CaseMap", mapRules, 0 /*Transliterator.FORWARD*/);
        labelSeparatorSet     = new UnicodeSet(bundle.getString("LabelSeparatorSet"));
        prohibitedSet         = new UnicodeSet(bundle.getString("ProhibitedSet"));
        unassignedSet         = new UnicodeSet(bundle.getString("UnassignedSet"));
    }
    
    public static final NamePrepTransform getInstance(){
        return transform;
    }
    public static boolean isLabelSeparator(int ch){
        return transform.labelSeparatorSet.contains(ch);
    }

     /*
       1) Map -- For each character in the input, check if it has a mapping
          and, if so, replace it with its mapping.  

       2) Normalize -- Possibly normalize the result of step 1 using Unicode
          normalization. 

       3) Prohibit -- Check for any characters that are not allowed in the
          output.  If any are found, return an error.  

       4) Check bidi -- Possibly check for right-to-left characters, and if
          any are found, make sure that the whole string satisfies the
          requirements for bidirectional strings.  If the string does not
          satisfy the requirements for bidirectional strings, return an
          error.  
          [Unicode3.2] defines several bidirectional categories; each character
           has one bidirectional category assigned to it.  For the purposes of
           the requirements below, an "RandALCat character" is a character that
           has Unicode bidirectional categories "R" or "AL"; an "LCat character"
           is a character that has Unicode bidirectional category "L".  Note


           that there are many characters which fall in neither of the above
           definitions; Latin digits (<U+0030> through <U+0039>) are examples of
           this because they have bidirectional category "EN".

           In any profile that specifies bidirectional character handling, all
           three of the following requirements MUST be met:

           1) The characters in section 5.8 MUST be prohibited.

           2) If a string contains any RandALCat character, the string MUST NOT
              contain any LCat character.

           3) If a string contains any RandALCat character, a RandALCat
              character MUST be the first character of the string, and a
              RandALCat character MUST be the last character of the string.
    */

    public boolean isReady() {
        return mapTransform.isReady();
    }

    public StringBuffer prepare(UCharacterIterator src,
                                       int options)
                                       throws StringPrepParseException{
             return prepare(src.getText(),options);
    }

    private String map ( String src, int options)
                                throws StringPrepParseException{
        // map 
        boolean allowUnassigned =  ((options & ALLOW_UNASSIGNED)>0);
        // disable test
        String caseMapOut = mapTransform.transliterate(src);
        UCharacterIterator iter = UCharacterIterator.getInstance(caseMapOut);
        int ch;
        while((ch=iter.nextCodePoint())!=UCharacterIterator.DONE){                          
            if(transform.unassignedSet.contains(ch)==true && allowUnassigned ==false){
                throw new StringPrepParseException("An unassigned code point was found in the input",
                                         StringPrepParseException.UNASSIGNED_ERROR);
            }
        }
        return caseMapOut;
    }
    public StringBuffer prepare(String src,int options)
                                   throws StringPrepParseException{
 
        int ch;
        String mapOut = map(src,options);
        UCharacterIterator iter = UCharacterIterator.getInstance(mapOut);

        int direction=UCharacterDirection.CHAR_DIRECTION_COUNT,
            firstCharDir=UCharacterDirection.CHAR_DIRECTION_COUNT;    
        int rtlPos=-1, ltrPos=-1;
        boolean rightToLeft=false, leftToRight=false;
           
        while((ch=iter.nextCodePoint())!= UCharacterIterator.DONE){


            if(transform.prohibitedSet.contains(ch)==true && ch!=0x0020){
                throw new StringPrepParseException("A prohibited code point was found in the input",
                                         StringPrepParseException.PROHIBITED_ERROR,
                                         iter.getText(),iter.getIndex());
            }

            direction = UCharacter.getDirection(ch);
            if(firstCharDir == UCharacterDirection.CHAR_DIRECTION_COUNT){
                firstCharDir = direction;
            }
            if(direction == UCharacterDirection.LEFT_TO_RIGHT){
                leftToRight = true;
                ltrPos = iter.getIndex()-1;
            }
            if(direction == UCharacterDirection.RIGHT_TO_LEFT || direction == UCharacterDirection.RIGHT_TO_LEFT_ARABIC){
                rightToLeft = true;
                rtlPos = iter.getIndex()-1;
            }
        }           

        // satisfy 2
        if( leftToRight == true && rightToLeft == true){
            throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.",
                                     StringPrepParseException.CHECK_BIDI_ERROR,iter.getText(),(rtlPos>ltrPos) ? rtlPos : ltrPos);
        }

        //satisfy 3
        if( rightToLeft == true && 
            !((firstCharDir == UCharacterDirection.RIGHT_TO_LEFT || firstCharDir == UCharacterDirection.RIGHT_TO_LEFT_ARABIC) &&
            (direction == UCharacterDirection.RIGHT_TO_LEFT || direction == UCharacterDirection.RIGHT_TO_LEFT_ARABIC))
           ){
            throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.",
                                      StringPrepParseException.CHECK_BIDI_ERROR,iter.getText(),(rtlPos>ltrPos) ? rtlPos : ltrPos);
        }
        
        return new StringBuffer(mapOut);

    }

    private static class MapTransform {
        private Object translitInstance;
        private Method translitMethod;
        private boolean isReady;

        MapTransform(String id, String rule, int direction) {
            isReady = initialize(id, rule, direction);
        }

        boolean initialize(String id, String rule, int direction) {
            try {
                Class cls = Class.forName("android.icu.text.Transliterator");
                Method createMethod = cls.getMethod("createFromRules", String.class, String.class, Integer.TYPE);
                translitInstance = createMethod.invoke(null, id, rule, Integer.valueOf(direction));
                translitMethod = cls.getMethod("transliterate", String.class);
            } catch (Throwable e) {
                return false;
            }
            return true;
        }

        boolean isReady() {
            return isReady;
        }

        String transliterate(String text) {
            if (!isReady) {
                throw new IllegalStateException("Transliterator is not ready");
            }
            String result = null;
            try {
                result = (String)translitMethod.invoke(translitInstance, text);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
            return result;
        }
    }
}
