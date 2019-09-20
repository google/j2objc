/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: CharInfo.java 468654 2006-10-28 07:09:23Z minchau $
 */
package org.apache.xml.serializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.transform.TransformerException;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.SystemIDResolver;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

/**
 * This class provides services that tell if a character should have
 * special treatement, such as entity reference substitution or normalization
 * of a newline character.  It also provides character to entity reference
 * lookup.
 *
 * DEVELOPERS: See Known Issue in the constructor.
 * 
 * @xsl.usage internal
 */
final class CharInfo
{
    /** Given a character, lookup a String to output (e.g. a decorated entity reference). */
    private HashMap m_charToString;

    /**
     * The name of the HTML entities file.
     * If specified, the file will be resource loaded with the default class loader.
     */
    public static final String HTML_ENTITIES_RESOURCE = 
                SerializerBase.PKG_NAME+".HTMLEntities";

    /**
     * The name of the XML entities file.
     * If specified, the file will be resource loaded with the default class loader.
     */
    public static final String XML_ENTITIES_RESOURCE = 
                SerializerBase.PKG_NAME+".XMLEntities";

    /** The horizontal tab character, which the parser should always normalize. */
    static final char S_HORIZONAL_TAB = 0x09;

    /** The linefeed character, which the parser should always normalize. */
    static final char S_LINEFEED = 0x0A;

    /** The carriage return character, which the parser should always normalize. */
    static final char S_CARRIAGERETURN = 0x0D;
    static final char S_SPACE = 0x20;
    static final char S_QUOTE = 0x22;
    static final char S_LT = 0x3C;
    static final char S_GT = 0x3E;
    static final char S_NEL = 0x85;    
    static final char S_LINE_SEPARATOR = 0x2028;
    
    /** This flag is an optimization for HTML entities. It false if entities 
     * other than quot (34), amp (38), lt (60) and gt (62) are defined
     * in the range 0 to 127.
     * @xsl.usage internal
     */    
    boolean onlyQuotAmpLtGt;
    
    /** Copy the first 0,1 ... ASCII_MAX values into an array */
    static final int ASCII_MAX = 128;
    
    /** Array of values is faster access than a set of bits 
     * to quickly check ASCII characters in attribute values,
     * the value is true if the character in an attribute value
     * should be mapped to a String. 
     */
    private final boolean[] shouldMapAttrChar_ASCII;
    
    /** Array of values is faster access than a set of bits 
     * to quickly check ASCII characters in text nodes, 
     * the value is true if the character in a text node
     * should be mapped to a String. 
     */
    private final boolean[] shouldMapTextChar_ASCII;

    /** An array of bits to record if the character is in the set.
     * Although information in this array is complete, the
     * isSpecialAttrASCII array is used first because access to its values
     * is common and faster.
     */   
    private final int array_of_bits[];
     
    
    // 5 for 32 bit words,  6 for 64 bit words ...
    /*
     * This constant is used to shift an integer to quickly
     * calculate which element its bit is stored in.
     * 5 for 32 bit words (int) ,  6 for 64 bit words (long)
     */
    private static final int SHIFT_PER_WORD = 5;
    
    /*
     * A mask to get the low order bits which are used to
     * calculate the value of the bit within a given word,
     * that will represent the presence of the integer in the 
     * set.
     * 
     * 0x1F for 32 bit words (int),
     * or 0x3F for 64 bit words (long) 
     */
    private static final int LOW_ORDER_BITMASK = 0x1f;
    
    /*
     * This is used for optimizing the lookup of bits representing
     * the integers in the set. It is the index of the first element
     * in the array array_of_bits[] that is not used.
     */
    private int firstWordNotUsed;


    /**
     * A base constructor just to explicitly create the fields,
     * with the exception of m_charToString which is handled
     * by the constructor that delegates base construction to this one.
     * <p>
     * m_charToString is not created here only for performance reasons,
     * to avoid creating a Hashtable that will be replaced when
     * making a mutable copy, {@link #mutableCopyOf(CharInfo)}. 
     *
     */
    private CharInfo() 
    {
    	this.array_of_bits = createEmptySetOfIntegers(65535);
    	this.firstWordNotUsed = 0;
    	this.shouldMapAttrChar_ASCII = new boolean[ASCII_MAX];
    	this.shouldMapTextChar_ASCII = new boolean[ASCII_MAX];
    	this.m_charKey = new CharKey();
    	
    	// Not set here, but in a constructor that uses this one
    	// this.m_charToString =  new Hashtable();  
    	
    	this.onlyQuotAmpLtGt = true;
    	

    	return;
    }
    
    private CharInfo(String entitiesResource, String method, boolean internal)
    {
    	// call the default constructor to create the fields
    	this();
    	m_charToString = new HashMap();

        ResourceBundle entities = null;
        boolean noExtraEntities = true;

        // Make various attempts to interpret the parameter as a properties
        // file or resource file, as follows:
        //
        //   1) attempt to load .properties file using ResourceBundle
        //   2) try using the class loader to find the specified file a resource
        //      file
        //   3) try treating the resource a URI

        if (internal) { 
            try {
                // Load entity property files by using PropertyResourceBundle,
                // cause of security issure for applets
                entities = PropertyResourceBundle.getBundle(entitiesResource);
            } catch (Exception e) {}
        }

        if (entities != null) {
            Enumeration keys = entities.getKeys();
            while (keys.hasMoreElements()){
                String name = (String) keys.nextElement();
                String value = entities.getString(name);
                int code = Integer.parseInt(value);
                boolean extra = defineEntity(name, (char) code);
                if (extra)
                    noExtraEntities = false;
            }
        } else {
            InputStream is = null;

            // Load user specified resource file by using URL loading, it
            // requires a valid URI as parameter
            try {
                if (internal) {
                    is = CharInfo.class.getResourceAsStream(entitiesResource);
                } else {
                    ClassLoader cl = ObjectFactory.findClassLoader();
                    if (cl == null) {
                        is = ClassLoader.getSystemResourceAsStream(entitiesResource);
                    } else {
                        is = cl.getResourceAsStream(entitiesResource);
                    }

                    if (is == null) {
                        try {
                            URL url = new URL(entitiesResource);
                            is = url.openStream();
                        } catch (Exception e) {}
                    }
                }
                
                // j2objc: resource wasn't found, load defaults from string.
                if (entitiesResource.equals(HTML_ENTITIES_RESOURCE)) {
                  is = new ByteArrayInputStream(
                      HTML_ENTITIES_RESOURCE_STR.getBytes(StandardCharsets.UTF_8));
                } else if (entitiesResource.equals(XML_ENTITIES_RESOURCE)) {
                  is = new ByteArrayInputStream(
                      XML_ENTITIES_RESOURCE_STR.getBytes(StandardCharsets.UTF_8));
                }

                if (is == null) {
                    throw new RuntimeException(
                        Utils.messages.createMessage(
                            MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                            new Object[] {entitiesResource, entitiesResource}));
                }

                // Fix Bugzilla#4000: force reading in UTF-8
                //  This creates the de facto standard that Xalan's resource 
                //  files must be encoded in UTF-8. This should work in all
                // JVMs.
                //
                // %REVIEW% KNOWN ISSUE: IT FAILS IN MICROSOFT VJ++, which
                // didn't implement the UTF-8 encoding. Theoretically, we should
                // simply let it fail in that case, since the JVM is obviously
                // broken if it doesn't support such a basic standard.  But
                // since there are still some users attempting to use VJ++ for
                // development, we have dropped in a fallback which makes a
                // second attempt using the platform's default encoding. In VJ++
                // this is apparently ASCII, which is subset of UTF-8... and
                // since the strings we'll be reading here are also primarily
                // limited to the 7-bit ASCII range (at least, in English
                // versions of Xalan), this should work well enough to keep us
                // on the air until we're ready to officially decommit from
                // VJ++.

                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    reader = new BufferedReader(new InputStreamReader(is));
                }

                String line = reader.readLine();

                while (line != null) {
                    if (line.length() == 0 || line.charAt(0) == '#') {
                        line = reader.readLine();

                        continue;
                    }

                    int index = line.indexOf(' ');

                    if (index > 1) {
                        String name = line.substring(0, index);

                        ++index;

                        if (index < line.length()) {
                            String value = line.substring(index);
                            index = value.indexOf(' ');

                            if (index > 0) {
                                value = value.substring(0, index);
                            }

                            int code = Integer.parseInt(value);

                            boolean extra = defineEntity(name, (char) code);
                            if (extra)
                                noExtraEntities = false;
                        }
                    }

                    line = reader.readLine();
                }

                is.close();
            } catch (Exception e) {
                throw new RuntimeException(
                    Utils.messages.createMessage(
                        MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                        new Object[] { entitiesResource,
                                       e.toString(),
                                       entitiesResource,
                                       e.toString()}));
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception except) {}
                }
            }
        }

        onlyQuotAmpLtGt = noExtraEntities;
            
        /* Now that we've used get(ch) just above to initialize the
         * two arrays we will change by adding a tab to the set of 
         * special chars for XML (but not HTML!).
         * We do this because a tab is always a
         * special character in an XML attribute, 
         * but only a special character in XML text 
         * if it has an entity defined for it.
         * This is the reason for this delay.
         */
        if (Method.XML.equals(method)) 
        {       
            // We choose not to escape the quotation mark as &quot; in text nodes
            shouldMapTextChar_ASCII[S_QUOTE] = false;
        }
        
        if (Method.HTML.equals(method)) {
        	// The XSLT 1.0 recommendation says 
        	// "The html output method should not escape < characters occurring in attribute values."
        	// So we don't escape '<' in an attribute for HTML
        	shouldMapAttrChar_ASCII['<'] = false;    
        	
        	// We choose not to escape the quotation mark as &quot; in text nodes.
            shouldMapTextChar_ASCII[S_QUOTE] = false;
        }
    }

    /**
     * Defines a new character reference. The reference's name and value are
     * supplied. Nothing happens if the character reference is already defined.
     * <p>Unlike internal entities, character references are a string to single
     * character mapping. They are used to map non-ASCII characters both on
     * parsing and printing, primarily for HTML documents. '&amp;lt;' is an
     * example of a character reference.</p>
     *
     * @param name The entity's name
     * @param value The entity's value
     * @return true if the mapping is not one of:
     * <ul>
     * <li> '<' to "&lt;"
     * <li> '>' to "&gt;"
     * <li> '&' to "&amp;"
     * <li> '"' to "&quot;"
     * </ul>
     */
    private boolean defineEntity(String name, char value)
    {
        StringBuffer sb = new StringBuffer("&");
        sb.append(name);
        sb.append(';');
        String entityString = sb.toString();
        
        boolean extra = defineChar2StringMapping(entityString, value);
        return extra;
    }

    /**
     * A utility object, just used to map characters to output Strings,
     * needed because a HashMap needs to map an object as a key, not a 
     * Java primitive type, like a char, so this object gets around that
     * and it is reusable.
     */
    private final CharKey m_charKey;

    /**
     * Map a character to a String. For example given
     * the character '>' this method would return the fully decorated
     * entity name "&lt;".
     * Strings for entity references are loaded from a properties file,
     * but additional mappings defined through calls to defineChar2String()
     * are possible. Such entity reference mappings could be over-ridden.
     *
     * This is reusing a stored key object, in an effort to avoid
     * heap activity. Unfortunately, that introduces a threading risk.
     * Simplest fix for now is to make it a synchronized method, or to give
     * up the reuse; I see very little performance difference between them.
     * Long-term solution would be to replace the hashtable with a sparse array
     * keyed directly from the character's integer value; see DTM's
     * string pool for a related solution.
     *
     * @param value The character that should be resolved to
     * a String, e.g. resolve '>' to  "&lt;".
     *
     * @return The String that the character is mapped to, or null if not found.
     * @xsl.usage internal
     */
    String getOutputStringForChar(char value)
    {
        // CharKey m_charKey = new CharKey(); //Alternative to synchronized
        m_charKey.setChar(value);
        return (String) m_charToString.get(m_charKey);
    }
    
    /**
     * Tell if the character argument that is from
     * an attribute value has a mapping to a String.
     * 
     * @param value the value of a character that is in an attribute value
     * @return true if the character should have any special treatment, 
     * such as when writing out entity references.
     * @xsl.usage internal
     */
    final boolean shouldMapAttrChar(int value)
    {
        // for performance try the values in the boolean array first,
        // this is faster access than the BitSet for common ASCII values

        if (value < ASCII_MAX)
            return shouldMapAttrChar_ASCII[value];

        // rather than java.util.BitSet, our private
        // implementation is faster (and less general).
        return get(value);
    }    

    /**
     * Tell if the character argument that is from a 
     * text node has a mapping to a String, for example
     * to map '<' to "&lt;".
     * 
     * @param value the value of a character that is in a text node
     * @return true if the character has a mapping to a String, 
     * such as when writing out entity references.
     * @xsl.usage internal
     */
    final boolean shouldMapTextChar(int value)
    {
        // for performance try the values in the boolean array first,
        // this is faster access than the BitSet for common ASCII values

        if (value < ASCII_MAX)
            return shouldMapTextChar_ASCII[value];

        // rather than java.util.BitSet, our private
        // implementation is faster (and less general).
        return get(value);
    }
    

     
    private static CharInfo getCharInfoBasedOnPrivilege(
        final String entitiesFileName, final String method, 
        final boolean internal){
            return (CharInfo) AccessController.doPrivileged(
                new PrivilegedAction() {
                        public Object run() {
                            return new CharInfo(entitiesFileName, 
                              method, internal);}
            });            
    }
     
    /**
     * Factory that reads in a resource file that describes the mapping of
     * characters to entity references.
     *
     * Resource files must be encoded in UTF-8 and have a format like:
     * <pre>
     * # First char # is a comment
     * Entity numericValue
     * quot 34
     * amp 38
     * </pre>
     * (Note: Why don't we just switch to .properties files? Oct-01 -sc)
     *
     * @param entitiesResource Name of entities resource file that should
     * be loaded, which describes that mapping of characters to entity references.
     * @param method the output method type, which should be one of "xml", "html", "text"...
     * 
     * @xsl.usage internal
     */
    static CharInfo getCharInfo(String entitiesFileName, String method)
    {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(entitiesFileName);
        if (charInfo != null) {
        	return mutableCopyOf(charInfo);
        }

        // try to load it internally - cache
        try {
            charInfo = getCharInfoBasedOnPrivilege(entitiesFileName, 
                                        method, true);
            // Put the common copy of charInfo in the cache, but return
            // a copy of it.
            m_getCharInfoCache.put(entitiesFileName, charInfo);
            return mutableCopyOf(charInfo);
        } catch (Exception e) {}

        // try to load it externally - do not cache
        try {
            return getCharInfoBasedOnPrivilege(entitiesFileName, 
                                method, false);
        } catch (Exception e) {}

        String absoluteEntitiesFileName;

        if (entitiesFileName.indexOf(':') < 0) {
            absoluteEntitiesFileName =
                SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
        } else {
            try {
                absoluteEntitiesFileName =
                    SystemIDResolver.getAbsoluteURI(entitiesFileName, null);
            } catch (TransformerException te) {
                throw new WrappedRuntimeException(te);
            }
        }

        return getCharInfoBasedOnPrivilege(entitiesFileName, 
                                method, false);
    }

    /**
     * Create a mutable copy of the cached one.
     * @param charInfo The cached one.
     * @return
     */
    private static CharInfo mutableCopyOf(CharInfo charInfo) {
    	CharInfo copy = new CharInfo();
    	
    	int max = charInfo.array_of_bits.length;
    	System.arraycopy(charInfo.array_of_bits,0,copy.array_of_bits,0,max);
    	
    	copy.firstWordNotUsed = charInfo.firstWordNotUsed;
    	
    	max = charInfo.shouldMapAttrChar_ASCII.length;
    	System.arraycopy(charInfo.shouldMapAttrChar_ASCII,0,copy.shouldMapAttrChar_ASCII,0,max);
    	
    	max = charInfo.shouldMapTextChar_ASCII.length;
    	System.arraycopy(charInfo.shouldMapTextChar_ASCII,0,copy.shouldMapTextChar_ASCII,0,max);
    	
    	// utility field copy.m_charKey is already created in the default constructor 
    	
    	copy.m_charToString = (HashMap) charInfo.m_charToString.clone();
    	
    	copy.onlyQuotAmpLtGt = charInfo.onlyQuotAmpLtGt;
    	    	
		return copy;
	}

	/** 
	 * Table of user-specified char infos.
	 * The table maps entify file names (the name of the
	 * property file without the .properties extension)
	 * to CharInfo objects populated with entities defined in 
	 * corresponding property file.  
	 */
    private static Hashtable m_getCharInfoCache = new Hashtable();

    /**
     * Returns the array element holding the bit value for the
     * given integer
     * @param i the integer that might be in the set of integers
     * 
     */
    private static int arrayIndex(int i) {
        return (i >> SHIFT_PER_WORD);
    }

    /**
     * For a given integer in the set it returns the single bit
     * value used within a given word that represents whether
     * the integer is in the set or not.
     */
    private static int bit(int i) {
        int ret = (1 << (i & LOW_ORDER_BITMASK));
        return ret;
    }

    /**
     * Creates a new empty set of integers (characters)
     * @param max the maximum integer to be in the set.
     */
    private int[] createEmptySetOfIntegers(int max) {
        firstWordNotUsed = 0; // an optimization 

        int[] arr = new int[arrayIndex(max - 1) + 1];
            return arr;
 
    }

    /**
     * Adds the integer (character) to the set of integers.
     * @param i the integer to add to the set, valid values are 
     * 0, 1, 2 ... up to the maximum that was specified at
     * the creation of the set.
     */
    private final void set(int i) {   
        setASCIItextDirty(i);
        setASCIIattrDirty(i); 
             
        int j = (i >> SHIFT_PER_WORD); // this word is used
        int k = j + 1;       
        
        if(firstWordNotUsed < k) // for optimization purposes.
            firstWordNotUsed = k;
            
        array_of_bits[j] |= (1 << (i & LOW_ORDER_BITMASK));
    }


    /**
     * Return true if the integer (character)is in the set of integers.
     * 
     * This implementation uses an array of integers with 32 bits per
     * integer.  If a bit is set to 1 the corresponding integer is 
     * in the set of integers.
     * 
     * @param i an integer that is tested to see if it is the
     * set of integers, or not.
     */
    private final boolean get(int i) {

        boolean in_the_set = false;
        int j = (i >> SHIFT_PER_WORD); // wordIndex(i)
        // an optimization here, ... a quick test to see
        // if this integer is beyond any of the words in use
        if(j < firstWordNotUsed)
            in_the_set = (array_of_bits[j] & 
                          (1 << (i & LOW_ORDER_BITMASK))
            ) != 0;  // 0L for 64 bit words
        return in_the_set;
    }
    
    /**
     * This method returns true if there are some non-standard mappings to
     * entities other than quot, amp, lt, gt, and its only purpose is for
     * performance.
     * @param charToMap The value of the character that is mapped to a String
     * @param outputString The String to which the character is mapped, usually
     * an entity reference such as "&lt;".
     * @return true if the mapping is not one of:
     * <ul>
     * <li> '<' to "&lt;"
     * <li> '>' to "&gt;"
     * <li> '&' to "&amp;"
     * <li> '"' to "&quot;"
     * </ul>
     */
    private boolean extraEntity(String outputString, int charToMap)
    {
        boolean extra = false;
        if (charToMap < ASCII_MAX)
        {
            switch (charToMap)
            {
                case '"' : // quot
                	if (!outputString.equals("&quot;"))
                		extra = true;  
                	break;
                case '&' : // amp
                	if (!outputString.equals("&amp;"))
                		extra = true;
                	break;
                case '<' : // lt
                	if (!outputString.equals("&lt;"))
                		extra = true;
                	break;
                case '>' : // gt
                	if (!outputString.equals("&gt;"))
                		extra = true;
                    break;
                default : // other entity in range 0 to 127  
                    extra = true;
            }
        }
        return extra;
    }    
    
    /**
     * If the character is in the ASCII range then
     * mark it as needing replacement with
     * a String on output if it occurs in a text node.
     * @param ch
     */
    private void setASCIItextDirty(int j) 
    {
        if (0 <= j && j < ASCII_MAX) 
        {
            shouldMapTextChar_ASCII[j] = true;
        } 
    }
    
    /**
     * If the character is in the ASCII range then
     * mark it as needing replacement with
     * a String on output if it occurs in a attribute value.
     * @param ch
     */
    private void setASCIIattrDirty(int j) 
    {
        if (0 <= j && j < ASCII_MAX) 
        {
            shouldMapAttrChar_ASCII[j] = true;
        } 
    }

    
    /**
     * Call this method to register a char to String mapping, for example
     * to map '<' to "&lt;".
     * @param outputString The String to map to.
     * @param inputChar The char to map from.
     * @return true if the mapping is not one of:
     * <ul>
     * <li> '<' to "&lt;"
     * <li> '>' to "&gt;"
     * <li> '&' to "&amp;"
     * <li> '"' to "&quot;"
     * </ul>
     */
    boolean defineChar2StringMapping(String outputString, char inputChar) 
    {
        CharKey character = new CharKey(inputChar);
        m_charToString.put(character, outputString);
        set(inputChar);  // mark the character has having a mapping to a String
        
        boolean extraMapping = extraEntity(outputString, inputChar);
        return extraMapping;
        	
    }

    /**
     * Simple class for fast lookup of char values, when used with
     * hashtables.  You can set the char, then use it as a key.
     *  
     * @xsl.usage internal
     */
    private static class CharKey extends Object
    {

      /** String value          */
      private char m_char;

      /**
       * Constructor CharKey
       *
       * @param key char value of this object.
       */
      public CharKey(char key)
      {
        m_char = key;
      }
  
      /**
       * Default constructor for a CharKey.
       *
       * @param key char value of this object.
       */
      public CharKey()
      {
      }
  
      /**
       * Get the hash value of the character.  
       *
       * @return hash value of the character.
       */
      public final void setChar(char c)
      {
        m_char = c;
      }



      /**
       * Get the hash value of the character.  
       *
       * @return hash value of the character.
       */
      public final int hashCode()
      {
        return (int)m_char;
      }

      /**
       * Override of equals() for this object 
       *
       * @param obj to compare to
       *
       * @return True if this object equals this string value 
       */
      public final boolean equals(Object obj)
      {
        return ((CharKey)obj).m_char == m_char;
      }
    }
   
    // j2objc: the default resource values, for when resources weren't linked into app.
    // These strings are created by taking each .properties file and removing the
    // comments and whitespace.
    
    private static final String XML_ENTITIES_RESOURCE_STR =
        "quot=34\n"
        + "amp=38\n"
        + "lt=60\n"
        + "gt=62\n";

    private static final String HTML_ENTITIES_RESOURCE_STR =
        "quot=34\n"
        + "amp=38\n"
        + "lt=60\n"
        + "gt=62\n"
        + "nbsp=160\n"
        + "iexcl=161\n"
        + "cent=162\n"
        + "pound=163\n"
        + "curren=164\n"
        + "yen=165\n"
        + "brvbar=166\n"
        + "sect=167\n"
        + "uml=168\n"
        + "copy=169\n"
        + "ordf=170\n"
        + "laquo=171\n"
        + "not=172\n"
        + "shy=173\n"
        + "reg=174\n"
        + "macr=175\n"
        + "deg=176\n"
        + "plusmn=177\n"
        + "sup2=178\n"
        + "sup3=179\n"
        + "acute=180\n"
        + "micro=181\n"
        + "para=182\n"
        + "middot=183\n"
        + "cedil=184\n"
        + "sup1=185\n"
        + "ordm=186\n"
        + "raquo=187\n"
        + "frac14=188\n"
        + "frac12=189\n"
        + "frac34=190\n"
        + "iquest=191\n"
        + "Agrave=192\n"
        + "Aacute=193\n"
        + "Acirc=194\n"
        + "Atilde=195\n"
        + "Auml=196\n"
        + "Aring=197\n"
        + "AElig=198\n"
        + "Ccedil=199\n"
        + "Egrave=200\n"
        + "Eacute=201\n"
        + "Ecirc=202\n"
        + "Euml=203\n"
        + "Igrave=204\n"
        + "Iacute=205\n"
        + "Icirc=206\n"
        + "Iuml=207\n"
        + "ETH=208\n"
        + "Ntilde=209\n"
        + "Ograve=210\n"
        + "Oacute=211\n"
        + "Ocirc=212\n"
        + "Otilde=213\n"
        + "Ouml=214\n"
        + "times=215\n"
        + "Oslash=216\n"
        + "Ugrave=217\n"
        + "Uacute=218\n"
        + "Ucirc=219\n"
        + "Uuml=220\n"
        + "Yacute=221\n"
        + "THORN=222\n"
        + "szlig=223\n"
        + "agrave=224\n"
        + "aacute=225\n"
        + "acirc=226\n"
        + "atilde=227\n"
        + "auml=228\n"
        + "aring=229\n"
        + "aelig=230\n"
        + "ccedil=231\n"
        + "egrave=232\n"
        + "eacute=233\n"
        + "ecirc=234\n"
        + "euml=235\n"
        + "igrave=236\n"
        + "iacute=237\n"
        + "icirc=238\n"
        + "iuml=239\n"
        + "eth=240\n"
        + "ntilde=241\n"
        + "ograve=242\n"
        + "oacute=243\n"
        + "ocirc=244\n"
        + "otilde=245\n"
        + "ouml=246\n"
        + "divide=247\n"
        + "oslash=248\n"
        + "ugrave=249\n"
        + "uacute=250\n"
        + "ucirc=251\n"
        + "uuml=252\n"
        + "yacute=253\n"
        + "thorn=254\n"
        + "yuml=255\n"
        + "bull=8226\n"
        + "hellip=8230\n"
        + "prime=8242\n"
        + "Prime=8243\n"
        + "oline=8254\n"
        + "frasl=8260\n"
        + "weierp=8472\n"
        + "image=8465\n"
        + "real=8476\n"
        + "trade=8482\n"
        + "alefsym=8501\n"
        + "larr=8592\n"
        + "uarr=8593\n"
        + "rarr=8594\n"
        + "darr=8595\n"
        + "harr=8596\n"
        + "crarr=8629\n"
        + "lArr=8656\n"
        + "uArr=8657\n"
        + "rArr=8658\n"
        + "dArr=8659\n"
        + "hArr=8660\n"
        + "forall=8704\n"
        + "part=8706\n"
        + "exist=8707\n"
        + "empty=8709\n"
        + "nabla=8711\n"
        + "isin=8712\n"
        + "notin=8713\n"
        + "ni=8715\n"
        + "prod=8719\n"
        + "sum=8721\n"
        + "minus=8722\n"
        + "lowast=8727\n"
        + "radic=8730\n"
        + "prop=8733\n"
        + "infin=8734\n"
        + "ang=8736\n"
        + "and=8743\n"
        + "or=8744\n"
        + "cap=8745\n"
        + "cup=8746\n"
        + "int=8747\n"
        + "there4=8756\n"
        + "sim=8764\n"
        + "cong=8773\n"
        + "asymp=8776\n"
        + "ne=8800\n"
        + "equiv=8801\n"
        + "le=8804\n"
        + "ge=8805\n"
        + "sub=8834\n"
        + "sup=8835\n"
        + "nsub=8836\n"
        + "sube=8838\n"
        + "supe=8839\n"
        + "oplus=8853\n"
        + "otimes=8855\n"
        + "perp=8869\n"
        + "sdot=8901\n"
        + "lceil=8968\n"
        + "rceil=8969\n"
        + "lfloor=8970\n"
        + "rfloor=8971\n"
        + "lang=9001\n"
        + "rang=9002\n"
        + "loz=9674\n"
        + "spades=9824\n"
        + "clubs=9827\n"
        + "hearts=9829\n"
        + "diams=9830\n"
        + "ensp=8194\n"
        + "emsp=8195\n"
        + "thinsp=8201\n"
        + "zwnj=8204\n"
        + "zwj=8205\n"
        + "lrm=8206\n"
        + "rlm=8207\n"
        + "ndash=8211\n"
        + "mdash=8212\n"
        + "lsquo=8216\n"
        + "rsquo=8217\n"
        + "sbquo=8218\n"
        + "ldquo=8220\n"
        + "rdquo=8221\n"
        + "bdquo=8222\n"
        + "dagger=8224\n"
        + "Dagger=8225\n"
        + "permil=8240\n"
        + "lsaquo=8249\n"
        + "rsaquo=8250\n"
        + "euro=8364\n";
        

}
