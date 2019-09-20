/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.stringprep;

import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;

/**
 * @author ram
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IDNAReference {
    
    private static char[] ACE_PREFIX = new char[]{ 0x0078,0x006E,0x002d,0x002d } ;
    private static final int ACE_PREFIX_LENGTH  = 4;

    private static final int MAX_LABEL_LENGTH   = 63;
    private static final int HYPHEN             = 0x002D;
    private static final int CAPITAL_A          = 0x0041;
    private static final int CAPITAL_Z          = 0x005A;
    private static final int LOWER_CASE_DELTA   = 0x0020;
    private static final int FULL_STOP          = 0x002E;


    public static final int DEFAULT             = 0x0000;
    public static final int ALLOW_UNASSIGNED    = 0x0001;
    public static final int USE_STD3_RULES      = 0x0002;
    public static final NamePrepTransform transform = NamePrepTransform.getInstance();
  
    public static boolean isReady() {
        return transform.isReady();
    }

    private static boolean startsWithPrefix(StringBuffer src){
        boolean startsWithPrefix = true;

        if(src.length() < ACE_PREFIX_LENGTH){
            return false;
        }
        for(int i=0; i<ACE_PREFIX_LENGTH;i++){
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

    private static StringBuffer toASCIILower(StringBuffer src){
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
            if(NamePrepTransform.isLabelSeparator(src[start])){
                return start;
            }
        }
        // we have not found the separator just return length
        return start;
    }
    
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
        
    public static StringBuffer convertToASCII(String src, int options)
        throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToASCII(iter,options);
    }
    public static StringBuffer convertToASCII(StringBuffer src, int options)
        throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToASCII(iter,options);
    }
    public static StringBuffer convertToASCII(UCharacterIterator srcIter, int options)
                throws StringPrepParseException{
    
        char[] caseFlags = null;
    
        // the source contains all ascii codepoints
        boolean srcIsASCII  = true;
        // assume the source contains all LDH codepoints
        boolean srcIsLDH = true; 

        //get the options
        boolean useSTD3ASCIIRules = ((options & USE_STD3_RULES) != 0);

        int ch;
        // step 1
        while((ch = srcIter.next())!= UCharacterIterator.DONE){
            if(ch> 0x7f){
                srcIsASCII = false;
            }
        }
        int failPos = -1;
        srcIter.setToStart();
        StringBuffer processOut = null;
        // step 2 is performed only if the source contains non ASCII
        if(!srcIsASCII){
            // step 2
            processOut =  transform.prepare(srcIter,options);
        }else{
            processOut = new StringBuffer(srcIter.getText());
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
                StringBuffer punyout = PunycodeReference.encode(processOut,caseFlags);
                
                // convert all codepoints to lower case ASCII
                StringBuffer lowerOut = toASCIILower(punyout);

                //Step 7: prepend the ACE prefix
                dest.append(ACE_PREFIX,0,ACE_PREFIX_LENGTH);
                //Step 6: copy the contents in b2 into dest
                dest.append(lowerOut);
            }else{
                throw new StringPrepParseException("The input does not start with the ACE Prefix.",
                                   StringPrepParseException.ACE_PREFIX_ERROR,processOut.toString(),0);
            }
        }
        if(dest.length() > MAX_LABEL_LENGTH){
            throw new StringPrepParseException("The labels in the input are too long. Length > 64.", 
                                    StringPrepParseException.LABEL_TOO_LONG_ERROR,dest.toString(),0);
        }
        return dest;
    }
    
    public static StringBuffer convertIDNtoASCII(UCharacterIterator iter,int options)
            throws StringPrepParseException{
            return convertIDNToASCII(iter.getText(), options);          
    }
    public static StringBuffer convertIDNtoASCII(StringBuffer str,int options)
            throws StringPrepParseException{
            return convertIDNToASCII(str.toString(), options);          
    }
    public static StringBuffer convertIDNToASCII(String src,int options)
            throws StringPrepParseException{
        char[] srcArr = src.toCharArray();
        StringBuffer result = new StringBuffer();
        int sepIndex=0;
        int oldSepIndex = 0;
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
        return result;
    }

    public static StringBuffer convertToUnicode(String src, int options)
           throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToUnicode(iter,options);
    }
    public static StringBuffer convertToUnicode(StringBuffer src, int options)
           throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToUnicode(iter,options);
    }   
    public static StringBuffer convertToUnicode(UCharacterIterator iter, int options)
           throws StringPrepParseException{

        // the source contains all ascii codepoints
        boolean srcIsASCII = true;

        int ch;
        int saveIndex = iter.getIndex();
        // step 1: find out if all the codepoints in src are ASCII
        while ((ch = iter.next()) != UCharacterIterator.DONE) {
            if (ch > 0x7F) {
                srcIsASCII = false;
                break;
            }
        }

        // The RFC states that
        // <quote>
        // ToUnicode never fails. If any step fails, then the original input
        // is returned immediately in that step.
        // </quote>
        do {
            StringBuffer processOut;
            if (srcIsASCII == false) {
                // step 2: process the string
                iter.setIndex(saveIndex);
                try {
                    processOut = transform.prepare(iter, options);
                } catch (StringPrepParseException e) {
                    break;
                }
            } else {
                // just point to source
                processOut = new StringBuffer(iter.getText());
            }

            // step 3: verify ACE Prefix
            if (startsWithPrefix(processOut)) {

                // step 4: Remove the ACE Prefix
                String temp = processOut.substring(ACE_PREFIX_LENGTH, processOut.length());

                // step 5: Decode using punycode
                StringBuffer decodeOut = null;
                try {
                    decodeOut = PunycodeReference.decode(new StringBuffer(temp), null);
                } catch (StringPrepParseException e) {
                    break;
                }

                // step 6:Apply toASCII
                StringBuffer toASCIIOut = convertToASCII(decodeOut, options);

                // step 7: verify
                if (compareCaseInsensitiveASCII(processOut, toASCIIOut) != 0) {
                    break;
                }
                // step 8: return output of step 5
                return decodeOut;
            }
        } while (false);

        return new StringBuffer(iter.getText());
    }

    public static StringBuffer convertIDNToUnicode(UCharacterIterator iter, int options)
        throws StringPrepParseException{
        return convertIDNToUnicode(iter.getText(), options);
    }
    public static StringBuffer convertIDNToUnicode(StringBuffer str, int options)
        throws StringPrepParseException{
        return convertIDNToUnicode(str.toString(), options);
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
            // increment the sepIndex to skip past the separator
            sepIndex++;
            oldSepIndex = sepIndex;
            result.append((char)FULL_STOP);
        }
        return result;
    }
    //  TODO: optimize
    public static int compare(StringBuffer s1, StringBuffer s2, int options)
        throws StringPrepParseException{
        if(s1==null || s2 == null){
            throw new IllegalArgumentException("One of the source buffers is null");
        }
        StringBuffer s1Out = convertIDNToASCII(s1.toString(), options);
        StringBuffer s2Out = convertIDNToASCII(s2.toString(), options);
        return compareCaseInsensitiveASCII(s1Out,s2Out);
    }
    //  TODO: optimize
    public static int compare(String s1, String s2, int options)
        throws StringPrepParseException{
        if(s1==null || s2 == null){
            throw new IllegalArgumentException("One of the source buffers is null");
        }
        StringBuffer s1Out = convertIDNToASCII(s1, options);
        StringBuffer s2Out = convertIDNToASCII(s2, options);
        return compareCaseInsensitiveASCII(s1Out,s2Out);
    }
    //  TODO: optimize
    public static int compare(UCharacterIterator i1, UCharacterIterator i2, int options)
        throws StringPrepParseException{
        if(i1==null || i2 == null){
            throw new IllegalArgumentException("One of the source buffers is null");
        }
        StringBuffer s1Out = convertIDNToASCII(i1.getText(), options);
        StringBuffer s2Out = convertIDNToASCII(i2.getText(), options);
        return compareCaseInsensitiveASCII(s1Out,s2Out);
    }

}
