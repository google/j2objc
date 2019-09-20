/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2003-2010, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
package android.icu.impl;

import android.icu.text.IDNA;
import android.icu.text.StringPrep;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;

/**
 * IDNA2003 implementation code, moved out of android.icu.text.IDNA.java
 * while extending that class to support IDNA2008/UTS #46 as well.
 * @author Ram Viswanadha
 * @hide Only a subset of ICU is exposed in Android
 */
public final class IDNA2003 {
    /* IDNA ACE Prefix is "xn--" */
    private static char[] ACE_PREFIX                = new char[]{ 0x0078,0x006E,0x002d,0x002d } ;
    //private static final int ACE_PREFIX_LENGTH      = ACE_PREFIX.length;

    private static final int MAX_LABEL_LENGTH       = 63;
    private static final int HYPHEN                 = 0x002D;
    private static final int CAPITAL_A              = 0x0041;
    private static final int CAPITAL_Z              = 0x005A;
    private static final int LOWER_CASE_DELTA       = 0x0020;
    private static final int FULL_STOP              = 0x002E;
    private static final int MAX_DOMAIN_NAME_LENGTH = 255;

    // The NamePrep profile object
    private static final StringPrep namePrep = StringPrep.getInstance(StringPrep.RFC3491_NAMEPREP);
    
    private static boolean startsWithPrefix(StringBuffer src){
        boolean startsWithPrefix = true;

        if(src.length() < ACE_PREFIX.length){
            return false;
        }
        for(int i=0; i<ACE_PREFIX.length;i++){
            if(toASCIILower(src.charAt(i)) != ACE_PREFIX[i]){
                startsWithPrefix = false;
            }
        }
        return startsWithPrefix;
    }

    private static char toASCIILower(char ch){
        if(CAPITAL_A <= ch && ch <= CAPITAL_Z){
            return (char)(ch + LOWER_CASE_DELTA);
        }
        return ch;
    }

    private static StringBuffer toASCIILower(CharSequence src){
        StringBuffer dest = new StringBuffer();
        for(int i=0; i<src.length();i++){
            dest.append(toASCIILower(src.charAt(i)));
        }
        return dest;
    }

    private static int compareCaseInsensitiveASCII(StringBuffer s1, StringBuffer s2){
        char c1,c2;
        int rc;
        for(int i =0;/* no condition */;i++) {
            /* If we reach the ends of both strings then they match */
            if(i == s1.length()) {
                return 0;
            }

            c1 = s1.charAt(i);
            c2 = s2.charAt(i);
        
            /* Case-insensitive comparison */
            if(c1!=c2) {
                rc=toASCIILower(c1)-toASCIILower(c2);
                if(rc!=0) {
                    return rc;
                }
            }
        }
    }
   
    private static int getSeparatorIndex(char[] src,int start, int limit){
        for(; start<limit;start++){
            if(isLabelSeparator(src[start])){
                return start;
            }
        }
        // we have not found the separator just return length
        return start;
    }
    
    /*
    private static int getSeparatorIndex(UCharacterIterator iter){
        int currentIndex = iter.getIndex();
        int separatorIndex = 0;
        int ch;
        while((ch=iter.next())!= UCharacterIterator.DONE){
            if(isLabelSeparator(ch)){
                separatorIndex = iter.getIndex();
                iter.setIndex(currentIndex);
                return separatorIndex;
            }
        }
        // reset index
        iter.setIndex(currentIndex);
        // we have not found the separator just return the length
       
    }
    */
    

    private static boolean isLDHChar(int ch){
        // high runner case
        if(ch>0x007A){
            return false;
        }
        //[\\u002D \\u0030-\\u0039 \\u0041-\\u005A \\u0061-\\u007A]
        if( (ch==0x002D) || 
            (0x0030 <= ch && ch <= 0x0039) ||
            (0x0041 <= ch && ch <= 0x005A) ||
            (0x0061 <= ch && ch <= 0x007A)
          ){
            return true;
        }
        return false;
    }
    
    /**
     * Ascertain if the given code point is a label separator as 
     * defined by the IDNA RFC
     * 
     * @param ch The code point to be ascertained
     * @return true if the char is a label separator
     */
    private static boolean isLabelSeparator(int ch){
        switch(ch){
            case 0x002e:
            case 0x3002:
            case 0xFF0E:
            case 0xFF61:
                return true;
            default:
                return false;           
        }
    }

    public static StringBuffer convertToASCII(UCharacterIterator src, int options)
            throws StringPrepParseException{
        
        boolean[] caseFlags = null;
    
        // the source contains all ascii codepoints
        boolean srcIsASCII  = true;
        // assume the source contains all LDH codepoints
        boolean srcIsLDH = true; 

        //get the options
        boolean useSTD3ASCIIRules = ((options & IDNA.USE_STD3_RULES) != 0);
        int ch;
        // step 1
        while((ch = src.next())!= UCharacterIterator.DONE){
            if(ch> 0x7f){
                srcIsASCII = false;
            }
        }
        int failPos = -1;
        src.setToStart();
        StringBuffer processOut = null;
        // step 2 is performed only if the source contains non ASCII
        if(!srcIsASCII){
            // step 2
            processOut = namePrep.prepare(src, options);
        }else{
            processOut = new StringBuffer(src.getText());
        }
        int poLen = processOut.length();
        
        if(poLen==0){
            throw new StringPrepParseException("Found zero length lable after NamePrep.",StringPrepParseException.ZERO_LENGTH_LABEL);
        }
        StringBuffer dest = new StringBuffer();
        
        // reset the variable to verify if output of prepare is ASCII or not
        srcIsASCII = true;
        
        // step 3 & 4
        for(int j=0;j<poLen;j++ ){
            ch=processOut.charAt(j);
            if(ch > 0x7F){
                srcIsASCII = false;
            }else if(isLDHChar(ch)==false){
                // here we do not assemble surrogates
                // since we know that LDH code points
                // are in the ASCII range only
                srcIsLDH = false;
                failPos = j;
            }
        }
    
        if(useSTD3ASCIIRules == true){
            // verify 3a and 3b
            if( srcIsLDH == false /* source contains some non-LDH characters */
                || processOut.charAt(0) ==  HYPHEN 
                || processOut.charAt(processOut.length()-1) == HYPHEN){

                /* populate the parseError struct */
                if(srcIsLDH==false){
                     throw new StringPrepParseException( "The input does not conform to the STD 3 ASCII rules",
                                              StringPrepParseException.STD3_ASCII_RULES_ERROR,
                                              processOut.toString(),
                                             (failPos>0) ? (failPos-1) : failPos);
                }else if(processOut.charAt(0) == HYPHEN){
                    throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules",
                                              StringPrepParseException.STD3_ASCII_RULES_ERROR,processOut.toString(),0);
     
                }else{
                     throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules",
                                              StringPrepParseException.STD3_ASCII_RULES_ERROR,
                                              processOut.toString(),
                                              (poLen>0) ? poLen-1 : poLen);

                }
            }
        }
        if(srcIsASCII){
            dest =  processOut;
        }else{
            // step 5 : verify the sequence does not begin with ACE prefix
            if(!startsWithPrefix(processOut)){

                //step 6: encode the sequence with punycode
                caseFlags = new boolean[poLen];

                StringBuilder punyout = Punycode.encode(processOut,caseFlags);

                // convert all codepoints to lower case ASCII
                StringBuffer lowerOut = toASCIILower(punyout);

                //Step 7: prepend the ACE prefix
                dest.append(ACE_PREFIX,0,ACE_PREFIX.length);
                //Step 6: copy the contents in b2 into dest
                dest.append(lowerOut);
            }else{

                throw new StringPrepParseException("The input does not start with the ACE Prefix.",
                                         StringPrepParseException.ACE_PREFIX_ERROR,processOut.toString(),0);
            }
        }
        if(dest.length() > MAX_LABEL_LENGTH){
            throw new StringPrepParseException("The labels in the input are too long. Length > 63.", 
                                     StringPrepParseException.LABEL_TOO_LONG_ERROR,dest.toString(),0);
        }
        return dest;
    }

    public static StringBuffer convertIDNToASCII(String src,int options)
            throws StringPrepParseException{

        char[] srcArr = src.toCharArray();
        StringBuffer result = new StringBuffer();
        int sepIndex=0;
        int oldSepIndex=0;
        for(;;){
            sepIndex = getSeparatorIndex(srcArr,sepIndex,srcArr.length);
            String label = new String(srcArr,oldSepIndex,sepIndex-oldSepIndex);
            //make sure this is not a root label separator.
            if(!(label.length()==0 && sepIndex==srcArr.length)){
                UCharacterIterator iter = UCharacterIterator.getInstance(label);
                result.append(convertToASCII(iter,options));
            }
            if(sepIndex==srcArr.length){
                break;
            }
            
            // increment the sepIndex to skip past the separator
            sepIndex++;
            oldSepIndex = sepIndex;
            result.append((char)FULL_STOP);
        }
        if(result.length() > MAX_DOMAIN_NAME_LENGTH){
            throw new StringPrepParseException("The output exceed the max allowed length.", StringPrepParseException.DOMAIN_NAME_TOO_LONG_ERROR);
        }
        return result;
    }

    public static StringBuffer convertToUnicode(UCharacterIterator src, int options)
            throws StringPrepParseException{
        
        boolean[] caseFlags = null;
                
        // the source contains all ascii codepoints
        boolean srcIsASCII  = true;
        // assume the source contains all LDH codepoints
        //boolean srcIsLDH = true; 
        
        //get the options
        //boolean useSTD3ASCIIRules = ((options & USE_STD3_RULES) != 0);
        
        //int failPos = -1;
        int ch;
        int saveIndex = src.getIndex();
        // step 1: find out if all the codepoints in src are ASCII  
        while((ch=src.next())!= UCharacterIterator.DONE){
            if(ch>0x7F){
                srcIsASCII = false;
            }/*else if((srcIsLDH = isLDHChar(ch))==false){
                failPos = src.getIndex();
            }*/
        }
        StringBuffer processOut;
        
        if(srcIsASCII == false){
            try {
                // step 2: process the string
                src.setIndex(saveIndex);
                processOut = namePrep.prepare(src,options);
            } catch (StringPrepParseException ex) {
                return new StringBuffer(src.getText());
            }

        }else{
            //just point to source
            processOut = new StringBuffer(src.getText());
        }
        // TODO:
        // The RFC states that 
        // <quote>
        // ToUnicode never fails. If any step fails, then the original input
        // is returned immediately in that step.
        // </quote>
        
        //step 3: verify ACE Prefix
        if(startsWithPrefix(processOut)){
            StringBuffer decodeOut = null;

            //step 4: Remove the ACE Prefix
            String temp = processOut.substring(ACE_PREFIX.length,processOut.length());

            //step 5: Decode using punycode
            try {
                decodeOut = new StringBuffer(Punycode.decode(temp,caseFlags));
            } catch (StringPrepParseException e) {
                decodeOut = null;
            }

            //step 6:Apply toASCII
            if (decodeOut != null) {
                StringBuffer toASCIIOut = convertToASCII(UCharacterIterator.getInstance(decodeOut), options);
    
                //step 7: verify
                if(compareCaseInsensitiveASCII(processOut, toASCIIOut) !=0){
//                    throw new StringPrepParseException("The verification step prescribed by the RFC 3491 failed",
//                                             StringPrepParseException.VERIFICATION_ERROR); 
                    decodeOut = null;
                }
            }

            //step 8: return output of step 5
             if (decodeOut != null) {
                 return decodeOut;
             }
        }
            
//        }else{
//            // verify that STD3 ASCII rules are satisfied
//            if(useSTD3ASCIIRules == true){
//                if( srcIsLDH == false /* source contains some non-LDH characters */
//                    || processOut.charAt(0) ==  HYPHEN 
//                    || processOut.charAt(processOut.length()-1) == HYPHEN){
//    
//                    if(srcIsLDH==false){
//                        throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules",
//                                                 StringPrepParseException.STD3_ASCII_RULES_ERROR,processOut.toString(),
//                                                 (failPos>0) ? (failPos-1) : failPos);
//                    }else if(processOut.charAt(0) == HYPHEN){
//                        throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules",
//                                                 StringPrepParseException.STD3_ASCII_RULES_ERROR,
//                                                 processOut.toString(),0);
//         
//                    }else{
//                        throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules",
//                                                 StringPrepParseException.STD3_ASCII_RULES_ERROR,
//                                                 processOut.toString(),
//                                                 processOut.length());
//    
//                    }
//                }
//            }
//            // just return the source
//            return new StringBuffer(src.getText());
//        }  
        
        return new StringBuffer(src.getText());
    }

    public static StringBuffer convertIDNToUnicode(String src, int options)
            throws StringPrepParseException{
        
        char[] srcArr = src.toCharArray();
        StringBuffer result = new StringBuffer();
        int sepIndex=0;
        int oldSepIndex=0;
        for(;;){
            sepIndex = getSeparatorIndex(srcArr,sepIndex,srcArr.length);
            String label = new String(srcArr,oldSepIndex,sepIndex-oldSepIndex);
            if(label.length()==0 && sepIndex!=srcArr.length ){
                throw new StringPrepParseException("Found zero length lable after NamePrep.",StringPrepParseException.ZERO_LENGTH_LABEL);
            }
            UCharacterIterator iter = UCharacterIterator.getInstance(label);
            result.append(convertToUnicode(iter,options));
            if(sepIndex==srcArr.length){
                break;
            }
            // Unlike the ToASCII operation we don't normalize the label separators
            result.append(srcArr[sepIndex]);
            // increment the sepIndex to skip past the separator
            sepIndex++;
            oldSepIndex =sepIndex;
        }
        if(result.length() > MAX_DOMAIN_NAME_LENGTH){
            throw new StringPrepParseException("The output exceed the max allowed length.", StringPrepParseException.DOMAIN_NAME_TOO_LONG_ERROR);
        }
        return result;
    }

    public static int compare(String s1, String s2, int options) throws StringPrepParseException{
        StringBuffer s1Out = convertIDNToASCII(s1, options);
        StringBuffer s2Out = convertIDNToASCII(s2, options);
        return compareCaseInsensitiveASCII(s1Out,s2Out);
    }
}
