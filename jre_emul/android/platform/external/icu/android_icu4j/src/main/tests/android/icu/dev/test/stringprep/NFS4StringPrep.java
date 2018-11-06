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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;

import android.icu.text.StringPrep;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;

/**
 * @author ram
 *
 * This is a dumb implementation of NFS4 profiles. It is a direct port of
 * C code, does not use Object Oriented principles. Quick and Dirty implementation
 * for testing.
 */
public final class NFS4StringPrep {

    private StringPrep nfscss = null;
    private StringPrep nfscsi = null;
    private StringPrep nfscis = null;
    private StringPrep nfsmxp = null;
    private StringPrep nfsmxs = null;
    //singleton instance
    private static final NFS4StringPrep prep = new NFS4StringPrep();
    

    private  NFS4StringPrep (){
        ClassLoader loader = NFS4StringPrep.class.getClassLoader();
        try{
            InputStream  nfscsiFile = loader.getResourceAsStream("android/icu/dev/data/testdata/nfscsi.spp");
            nfscsi = new StringPrep(nfscsiFile);
            nfscsiFile.close();
            
            InputStream  nfscssFile = loader.getResourceAsStream("android/icu/dev/data/testdata/nfscss.spp");
            nfscss = new StringPrep(nfscssFile);
            nfscssFile.close();
            
            InputStream  nfscisFile = loader.getResourceAsStream("android/icu/dev/data/testdata/nfscis.spp");
            nfscis = new StringPrep(nfscisFile);
            nfscisFile.close();
            
            InputStream  nfsmxpFile = loader.getResourceAsStream("android/icu/dev/data/testdata/nfsmxp.spp");
            nfsmxp = new StringPrep(nfsmxpFile);
            nfsmxpFile.close();
            
            InputStream  nfsmxsFile = loader.getResourceAsStream("android/icu/dev/data/testdata/nfsmxs.spp");
            nfsmxs = new StringPrep(nfsmxsFile);
            nfsmxsFile.close();
        }catch(IOException e){
            throw new MissingResourceException(e.toString(),"","");
        }
    }
    
    private static byte[] prepare(byte[] src, StringPrep strprep)
                throws StringPrepParseException, UnsupportedEncodingException{
        String s = new String(src, "UTF-8");
        UCharacterIterator iter =  UCharacterIterator.getInstance(s);
        StringBuffer out = strprep.prepare(iter,StringPrep.DEFAULT);
        return out.toString().getBytes("UTF-8");
    }
    
    public static byte[] cs_prepare(byte[] src, boolean isCaseSensitive)
                         throws StringPrepParseException, UnsupportedEncodingException{
        if(isCaseSensitive == true ){
            return prepare(src, prep.nfscss);
        }else{
            return prepare(src, prep.nfscsi);
        }
    }
    
    public static byte[] cis_prepare(byte[] src)
                         throws IOException, StringPrepParseException, UnsupportedEncodingException{
        return prepare(src, prep.nfscis);
    }  
    
    /* sorted array for binary search*/
    private static final String[] special_prefixes={
        "ANONYMOUS",    
        "AUTHENTICATED",
        "BATCH", 
        "DIALUP", 
        "EVERYONE", 
        "GROUP",
        "INTERACTIVE",  
        "NETWORK", 
        "OWNER",
    };


    /* binary search the sorted array */
    private static final int findStringIndex(String[] sortedArr,String target){

        int left, middle, right,rc;

        left =0;
        right= sortedArr.length-1;

        while(left <= right){
            middle = (left+right)/2;
            rc= sortedArr[middle].compareTo(target);
        
            if(rc<0){
                left = middle+1;
            }else if(rc >0){
                right = middle -1;
            }else{
                return middle;
            }
        }
        return -1;
    }
    private static final char AT_SIGN = '@';
    
    public static byte[] mixed_prepare(byte[] src)
                         throws IOException, StringPrepParseException, UnsupportedEncodingException{
        String s = new String(src, "UTF-8");
        int index = s.indexOf(AT_SIGN);
        StringBuffer out = new StringBuffer();

        if(index > -1){
            /* special prefixes must not be followed by suffixes! */
            String prefixString = s.substring(0,index);
            int i= findStringIndex(special_prefixes, prefixString);
            String suffixString = s.substring(index+1, s.length());
            if(i>-1 && !suffixString.equals("")){
                throw new StringPrepParseException("Suffix following a special index", StringPrepParseException.INVALID_CHAR_FOUND);
            }
            UCharacterIterator prefix = UCharacterIterator.getInstance(prefixString);
            UCharacterIterator suffix = UCharacterIterator.getInstance(suffixString);
            out.append(prep.nfsmxp.prepare(prefix,StringPrep.DEFAULT));
            out.append(AT_SIGN); // add the delimiter
            out.append(prep.nfsmxs.prepare(suffix, StringPrep.DEFAULT));
        }else{
            UCharacterIterator iter = UCharacterIterator.getInstance(s);
            out.append(prep.nfsmxp.prepare(iter,StringPrep.DEFAULT));
            
        }
       return out.toString().getBytes("UTF-8");
    }
    
}
