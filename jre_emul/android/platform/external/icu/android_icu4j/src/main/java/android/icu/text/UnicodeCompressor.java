/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

/**
* A compression engine implementing the Standard Compression Scheme
* for Unicode (SCSU) as outlined in <A
* HREF="http://www.unicode.org/unicode/reports/tr6">Unicode Technical
* Report #6</A>.
*
* <P>The SCSU works by using dynamically positioned <EM>windows</EM>
* consisting of 128 consecutive characters in Unicode.  During compression, 
* characters within a window are encoded in the compressed stream as the bytes 
* <TT>0x7F - 0xFF</TT>. The SCSU provides transparency for the characters 
* (bytes) between <TT>U+0000 - U+00FF</TT>.  The SCSU approximates the 
* storage size of traditional character sets, for example 1 byte per
* character for ASCII or Latin-1 text, and 2 bytes per character for CJK
* ideographs.</P>
*
* <P><STRONG>USAGE</STRONG></P>
*
* <P>The static methods on <TT>UnicodeCompressor</TT> may be used in a
* straightforward manner to compress simple strings:</P>
*
* <PRE>
*  String s = ... ; // get string from somewhere
*  byte [] compressed = UnicodeCompressor.compress(s);
* </PRE>
*
* <P>The static methods have a fairly large memory footprint.
* For finer-grained control over memory usage, 
* <TT>UnicodeCompressor</TT> offers more powerful APIs allowing
* iterative compression:</P>
*
* <PRE>
*  // Compress an array "chars" of length "len" using a buffer of 512 bytes
*  // to the OutputStream "out"
*
*  UnicodeCompressor myCompressor         = new UnicodeCompressor();
*  final static int  BUFSIZE              = 512;
*  byte []           byteBuffer           = new byte [ BUFSIZE ];
*  int               bytesWritten         = 0;
*  int []            unicharsRead         = new int [1];
*  int               totalCharsCompressed = 0;
*  int               totalBytesWritten    = 0;
*
*  do {
*    // do the compression
*    bytesWritten = myCompressor.compress(chars, totalCharsCompressed, 
*                                         len, unicharsRead,
*                                         byteBuffer, 0, BUFSIZE);
*
*    // do something with the current set of bytes
*    out.write(byteBuffer, 0, bytesWritten);
*
*    // update the no. of characters compressed
*    totalCharsCompressed += unicharsRead[0];
*
*    // update the no. of bytes written
*    totalBytesWritten += bytesWritten;
*
*  } while(totalCharsCompressed &lt; len);
*
*  myCompressor.reset(); // reuse compressor
* </PRE>
*
* @see UnicodeDecompressor
*
* @author Stephen F. Booth
* @hide Only a subset of ICU is exposed in Android
*/

/*
*
* COMPRESSION STRATEGY
*
* Single Byte Mode
*
* There are three relevant cases.
* If the character is in the current window or is Latin-1 (U+0000,
* U+0009, U+000A, U+000D, U+0020 - U+007F), the character is placed
* directly in the stream as a single byte.
*
*  1. Current character is in defined, inactive window.
*  2. Current character is in undefined window.
*  3. Current character is uncompressible Unicode (U+3400 - U+DFFF).
* 
*  1. Current character is in defined, inactive window
*    A. Look ahead two characters
*    B. If both following characters in same window as current character, 
*       switch to defined window
*    C. If only next character is in same window as current character, 
*       quote defined window
*    D. If neither of following characters is in same window as current, 
*       quote defined window
*   
*  2. Current character is in undefined window
*    A. Look ahead two characters
*    B. If both following characters in same window as current character, 
*       define new window
*    C. If only next character in same window as current character, 
*       switch to Unicode mode
*       NOTE: This costs us one extra byte.  However, 
*        since we have a limited number of windows to work with, it is 
*        assumed the cost will pay off later in savings from a window with
*        more characters in it.
*    D. If neither of following characters in same window as current, 
*       switch to Unicode mode.  Alternative to above: just quote 
*       Unicode (same byte cost)
*   
*  3. Current character is uncompressible Unicode (U+3400 - U+DFFF)
*    A. Look ahead one character
*    B. If next character in non-compressible region, switch to 
*       Unicode mode
*    C. If next character not in non-compressible region, quote Unicode
*   
*
* The following chart illustrates the bytes required for encoding characters
* in each possible way
*
* 
*                                   SINGLE BYTE MODE
*                                       Characters in a row with same index
*               tag encountered             1       2       3       4
*               ---------------------------------------------------------------
*               none (in current window)    1       2       3       4
*
*               quote Unicode               3       6       9       12
*
*   window not  switch to Unicode           3       5       7       9     byte
*   defined     define window               3       4       5       6     cost
*      
*   window      switch to window            2       3       4       5
*   defined     quote window                2       4       6       8
*
*  Unicode Mode
*
* There are two relevant cases.
* If the character is in the non-compressible region
* (U+3400 - U+DFFF), the character is simply written to the
* stream as a pair of bytes.
*
* 1. Current character is in defined, inactive window.
* 2. Current character is in undefined window.
*
*  1.Current character is in defined, inactive window
*    A. Look ahead one character
*    B. If next character has same index as current character, 
*       switch to defined window (and switch to single-byte mode)
*    C. If not, just put bytes in stream
*   
*  
*  2. Current character is in undefined window
*    A. Look ahead two characters
*    B. If both in same window as current character, define window 
*       (and switch to single-byte mode)
*    C. If only next character in same window, just put bytes in stream
*        NOTE: This costs us one extra byte.  However, 
*        since we have a limited number of windows to work with, it is 
*        assumed the cost will pay off later in savings from a window with 
*        more characters in it.
*    D. If neither in same window, put bytes in stream
*   
*
* The following chart illustrates the bytes required for encoding characters
* in each possible way
*
* 
*                                   UNICODE MODE
*                                       Characters in a row with same index
*               tag encountered             1       2       3       4
*               ---------------------------------------------------------------
*               none                        2       4       6       8
*
*               quote Unicode               3       6       9       12
*
*   window not  define window               3       4       5       6     byte
*   defined                                                               cost
*   window      switch to window            2       3       4       5
*   defined
*/
public final class UnicodeCompressor implements SCSU
{
    //==========================
    // Class variables
    //==========================

    /** For quick identification of a byte as a single-byte mode tag */
    private static boolean [] sSingleTagTable = {
        // table generated by CompressionTableGenerator
        false, true, true, true, true, true, true, true, true, false,
    false, true, true, false, true, true, true, true, true, true,
    true, true, true, true, true, true, true, true, true, true,
    true, true, false, false, false, false, false, false,false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false   
    };

    /** For quick identification of a byte as a unicode mode tag */
    private static boolean [] sUnicodeTagTable = {
        // table generated by CompressionTableGenerator
        false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, true,
    true, true, true, true, true, true, true, true, true, true,
    true, true, true, true, true, true, true, true, false, false,
    false, false, false, false, false, false, false, false, false,
    false, false 
    };

    //==========================
    // Instance variables
    //==========================
    
    /** Alias to current dynamic window */
    private int       fCurrentWindow   = 0;

    /** Dynamic compression window offsets */
    private int []    fOffsets         = new int [ NUMWINDOWS ];

    /** Current compression mode */
    private int       fMode            = SINGLEBYTEMODE;

    /** Keeps count of times character indices are encountered */
    private int []    fIndexCount      = new int [ MAXINDEX + 1 ];

    /** The time stamps indicate when a window was last defined */
    private int []    fTimeStamps      = new int [ NUMWINDOWS ];
    
    /** The current time stamp */
    private int       fTimeStamp       = 0;
    

    /**
     * Create a UnicodeCompressor.
     * Sets all windows to their default values.
     * @see #reset
     */
    public UnicodeCompressor()
    {
    reset();              // initialize to defaults
    }

    /**
     * Compress a string into a byte array.
     * @param buffer The string to compress.
     * @return A byte array containing the compressed characters.
     * @see #compress(char [], int, int)
     */
    public static byte [] compress(String buffer)
    {
    return compress(buffer.toCharArray(), 0, buffer.length());
    }

    /**
     * Compress a Unicode character array into a byte array.
     * @param buffer The character buffer to compress.
     * @param start The start of the character run to compress.
     * @param limit The limit of the character run to compress.
     * @return A byte array containing the compressed characters.
     * @see #compress(String)
     */
    public static byte [] compress(char [] buffer,
                   int start,
                   int limit)
    {
    UnicodeCompressor comp = new UnicodeCompressor();

    // use a buffer that we know will never overflow
    // in the worst case, each character will take 3 bytes
    // to encode: UQU, hibyte, lobyte.  In this case, the
    // compressed data will look like: SCU, UQU, hibyte, lobyte, ...
    // buffer must be at least 4 bytes in size
    int len = Math.max(4, 3 * (limit - start) + 1);
    byte [] temp = new byte [len];

    int byteCount = comp.compress(buffer, start, limit, null, 
                      temp, 0, len);

    byte [] result = new byte [byteCount];
    System.arraycopy(temp, 0, result, 0, byteCount);
    return result;
    }

    /**
     * Compress a Unicode character array into a byte array.
     *
     * This function will only consume input that can be completely
     * output.
     *
     * @param charBuffer The character buffer to compress.
     * @param charBufferStart The start of the character run to compress.
     * @param charBufferLimit The limit of the character run to compress.
     * @param charsRead A one-element array.  If not null, on return 
     * the number of characters read from charBuffer.
     * @param byteBuffer A buffer to receive the compressed data.  This 
     * buffer must be at minimum four bytes in size.
     * @param byteBufferStart The starting offset to which to write 
     * compressed data.
     * @param byteBufferLimit The limiting offset for writing compressed data.
     * @return The number of bytes written to byteBuffer.
     */
    public int compress(char []     charBuffer,
            int         charBufferStart,
            int         charBufferLimit,
            int []      charsRead,
            byte []     byteBuffer,
            int         byteBufferStart,
            int         byteBufferLimit)
    {
        // the current position in the target byte buffer
    int     bytePos       = byteBufferStart;
    
    // the current position in the source unicode character buffer
    int     ucPos         = charBufferStart;
    
    // the current unicode character from the source buffer
    int     curUC         = INVALIDCHAR;
    
    // the index for the current character
        int     curIndex      = -1;
        
    // look ahead
    int     nextUC        = INVALIDCHAR;
    int     forwardUC     = INVALIDCHAR;
    
        // temporary for window searching
    int     whichWindow   = 0;
    
    // high and low bytes of the current unicode character
    int     hiByte        = 0;
    int     loByte        = 0;


    // byteBuffer must be at least 4 bytes in size
    if(byteBuffer.length < 4 || (byteBufferLimit - byteBufferStart) < 4)
        throw new IllegalArgumentException("byteBuffer.length < 4");

    mainLoop:
    while(ucPos < charBufferLimit && bytePos < byteBufferLimit) {
        switch(fMode) {
        // main single byte mode compression loop
        case SINGLEBYTEMODE:
        singleByteModeLoop:
        while(ucPos < charBufferLimit && bytePos < byteBufferLimit) {
        // get current char
        curUC = charBuffer[ucPos++];

        // get next char
        if(ucPos < charBufferLimit) 
            nextUC = charBuffer[ucPos];
        else
            nextUC = INVALIDCHAR;
        
        // chars less than 0x0080 (excluding tags) go straight
        // in stream
        if(curUC < 0x0080) {
            loByte = curUC & 0xFF;

            // we need to check and make sure we don't
            // accidentally write a single byte mode tag to
            // the stream unless it's quoted
            if(sSingleTagTable[loByte]) {
                                // make sure there is enough room to
                                // write both bytes if not, rewind the
                                // source stream and break out
            if( (bytePos + 1) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            // since we know the byte is less than 0x80, SQUOTE0
            // will use static window 0, or ASCII
            byteBuffer[bytePos++] = (byte) SQUOTE0;
            }

            byteBuffer[bytePos++] = (byte) loByte;
        }

        // if the char belongs to current window, convert it
        // to a byte by adding the generic compression offset
        // and subtracting the window's offset
        else if(inDynamicWindow(curUC, fCurrentWindow) ) {
            byteBuffer[bytePos++] = (byte) 
            (curUC - fOffsets[ fCurrentWindow ] 
             + COMPRESSIONOFFSET);
        }
        
        // if char is not in compressible range, either switch to or
        // quote from unicode
        else if( ! isCompressible(curUC) ) {
            // only check next character if it is valid
            if(nextUC != INVALIDCHAR && isCompressible(nextUC)) {
                                // make sure there is enough room to
                                // write all three bytes if not,
                                // rewind the source stream and break
                                // out
            if( (bytePos + 2) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte) SQUOTEU;
            byteBuffer[bytePos++] = (byte) (curUC >>> 8);
            byteBuffer[bytePos++] = (byte) (curUC & 0xFF);
            }
            else {
                                // make sure there is enough room to
                                // write all four bytes if not, rewind
                                // the source stream and break out
            if((bytePos + 3) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte) SCHANGEU;

            hiByte = curUC >>> 8;
            loByte = curUC & 0xFF;

            if(sUnicodeTagTable[hiByte])
                // add quote Unicode tag
                byteBuffer[bytePos++]   = (byte) UQUOTEU;    

            byteBuffer[bytePos++] = (byte) hiByte;
            byteBuffer[bytePos++] = (byte) loByte;
                
            fMode = UNICODEMODE;
            break singleByteModeLoop;
            }
        }

        // if the char is in a currently defined dynamic
        // window, figure out which one, and either switch to
        // it or quote from it
        else if((whichWindow = findDynamicWindow(curUC)) 
            != INVALIDWINDOW ) {
            // look ahead
            if( (ucPos + 1) < charBufferLimit )
            forwardUC = charBuffer[ucPos + 1];
            else
            forwardUC = INVALIDCHAR;
            
            // all three chars in same window, switch to that
            // window inDynamicWindow will return false for
            // INVALIDCHAR
            if(inDynamicWindow(nextUC, whichWindow) 
               && inDynamicWindow(forwardUC, whichWindow)) {
                                // make sure there is enough room to
                                // write both bytes if not, rewind the
                                // source stream and break out
            if( (bytePos + 1) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte)(SCHANGE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) 
                (curUC - fOffsets[whichWindow] 
                 + COMPRESSIONOFFSET);
            fTimeStamps [ whichWindow ] = ++fTimeStamp;
            fCurrentWindow = whichWindow;
            }
            
            // either only next char or neither in same
            // window, so quote
            else {
                                // make sure there is enough room to
                                // write both bytes if not, rewind the
                                // source stream and break out
            if((bytePos + 1) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte) (SQUOTE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) 
                (curUC - fOffsets[whichWindow] 
                 + COMPRESSIONOFFSET);
            }
        }

        // if a static window is defined, and the following
        // character is not in that static window, quote from
        // the static window Note: to quote from a static
        // window, don't add 0x80
        else if((whichWindow = findStaticWindow(curUC)) 
            != INVALIDWINDOW 
            && ! inStaticWindow(nextUC, whichWindow) ) {
            // make sure there is enough room to write both
            // bytes if not, rewind the source stream and
            // break out
            if((bytePos + 1) >= byteBufferLimit) 
            { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte) (SQUOTE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) 
            (curUC - sOffsets[whichWindow]);
        }
        
        // if a window is not defined, decide if we want to
        // define a new one or switch to unicode mode
        else {
            // determine index for current char (char is compressible)
            curIndex = makeIndex(curUC);
            fIndexCount[curIndex]++;

            // look ahead
            if((ucPos + 1) < charBufferLimit)
            forwardUC = charBuffer[ucPos + 1];
            else
            forwardUC = INVALIDCHAR;

            // if we have encountered this index at least once
            // before, define a new window
            // OR
            // three chars in a row with same index, define a
            // new window (makeIndex will return RESERVEDINDEX
            // for INVALIDCHAR)
            if((fIndexCount[curIndex] > 1) ||
               (curIndex == makeIndex(nextUC) 
            && curIndex == makeIndex(forwardUC))) {
            // make sure there is enough room to write all
            // three bytes if not, rewind the source
            // stream and break out
            if( (bytePos + 2) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            // get least recently defined window
            whichWindow = getLRDefinedWindow();

            byteBuffer[bytePos++] = (byte)(SDEFINE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) curIndex;
            byteBuffer[bytePos++] = (byte) 
                (curUC - sOffsetTable[curIndex] 
                 + COMPRESSIONOFFSET);

            fOffsets[whichWindow] = sOffsetTable[curIndex];
            fCurrentWindow = whichWindow;
            fTimeStamps [whichWindow] = ++fTimeStamp;
            }

            // only two chars in a row with same index, so
            // switch to unicode mode (makeIndex will return
            // RESERVEDINDEX for INVALIDCHAR)
            // OR
            // three chars have different indices, so switch
            // to unicode mode
            else {
            // make sure there is enough room to write all
            // four bytes if not, rewind the source stream
            // and break out
            if((bytePos + 3) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte) SCHANGEU;

            hiByte = curUC >>> 8;
            loByte = curUC & 0xFF;

            if(sUnicodeTagTable[hiByte])
                // add quote Unicode tag
                byteBuffer[bytePos++] = (byte) UQUOTEU; 

            byteBuffer[bytePos++] = (byte) hiByte;
            byteBuffer[bytePos++] = (byte) loByte;

            fMode = UNICODEMODE;
            break singleByteModeLoop;
            }
        }
        }
        break;

        case UNICODEMODE:
        // main unicode mode compression loop
        unicodeModeLoop:
        while(ucPos < charBufferLimit && bytePos < byteBufferLimit) {
        // get current char
        curUC = charBuffer[ucPos++];    

        // get next char
        if( ucPos < charBufferLimit )
            nextUC = charBuffer[ucPos];
        else
            nextUC = INVALIDCHAR;

        // if we have two uncompressible chars in a row,
        // put the current char's bytes in the stream
        if( ! isCompressible(curUC) 
            || (nextUC != INVALIDCHAR && ! isCompressible(nextUC))) {
            // make sure there is enough room to write all three bytes
            // if not, rewind the source stream and break out
            if( (bytePos + 2) >= byteBufferLimit) 
            { --ucPos; break mainLoop; }

            hiByte = curUC >>> 8;
            loByte = curUC & 0xFF;

            if(sUnicodeTagTable[ hiByte ])
            // add quote Unicode tag
            byteBuffer[bytePos++] = (byte) UQUOTEU;
                
            byteBuffer[bytePos++] = (byte) hiByte;
            byteBuffer[bytePos++] = (byte) loByte;
        }
        
        // bytes less than 0x80 can go straight in the stream,
        // but in single-byte mode
        else if(curUC < 0x0080) {
            loByte = curUC & 0xFF;

            // if two chars in a row below 0x80 and the
            // current char is not a single-byte mode tag,
            // switch to single-byte mode
            if(nextUC != INVALIDCHAR 
               && nextUC < 0x0080 && ! sSingleTagTable[ loByte ] ) {
                                // make sure there is enough room to
                                // write both bytes if not, rewind the
                                // source stream and break out
            if( (bytePos + 1) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            // use the last-active window
            whichWindow = fCurrentWindow;
            byteBuffer[bytePos++] = (byte)(UCHANGE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) loByte;

            //fCurrentWindow = 0;
            fTimeStamps [whichWindow] = ++fTimeStamp;
            fMode = SINGLEBYTEMODE;
            break unicodeModeLoop;
            }

            // otherwise, just write the bytes to the stream
            // (this will cover the case of only 1 char less than 0x80
            // and single-byte mode tags)
            else {
                                // make sure there is enough room to
                                // write both bytes if not, rewind the
                                // source stream and break out
            if((bytePos + 1) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            // since the character is less than 0x80, the
            // high byte is always 0x00 - no need for
            // (curUC >>> 8)
            byteBuffer[bytePos++] = (byte) 0x00;
            byteBuffer[bytePos++] = (byte) loByte;
            }
        }

        // figure out if the current char is in a defined window
        else if((whichWindow = findDynamicWindow(curUC)) 
            != INVALIDWINDOW ) {
            // if two chars in a row in the same window,
            // switch to that window and go to single-byte mode
            // inDynamicWindow will return false for INVALIDCHAR
            if(inDynamicWindow(nextUC, whichWindow)) {
                                // make sure there is enough room to
                                // write both bytes if not, rewind the
                                // source stream and break out
            if((bytePos + 1) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            byteBuffer[bytePos++] = (byte)(UCHANGE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) 
                (curUC - fOffsets[whichWindow] 
                 + COMPRESSIONOFFSET);

            fTimeStamps [ whichWindow ] = ++fTimeStamp;
            fCurrentWindow = whichWindow;
            fMode = SINGLEBYTEMODE;
            break unicodeModeLoop;
            }

            // otherwise, just quote the unicode for the char
            else {
                                // make sure there is enough room to
                                // write all three bytes if not,
                                // rewind the source stream and break
                                // out
            if((bytePos + 2) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            hiByte = curUC >>> 8;
            loByte = curUC & 0xFF;

            if(sUnicodeTagTable[ hiByte ])
                // add quote Unicode tag
                byteBuffer[bytePos++] = (byte) UQUOTEU;

            byteBuffer[bytePos++] = (byte) hiByte;
            byteBuffer[bytePos++] = (byte) loByte;
            }
        }
        
        // char is not in a defined window
        else {
            // determine index for current char (char is compressible)
            curIndex = makeIndex(curUC);
            fIndexCount[curIndex]++;
            
            // look ahead
            if( (ucPos + 1) < charBufferLimit )
            forwardUC = charBuffer[ucPos + 1];
            else
            forwardUC = INVALIDCHAR;
            
            // if we have encountered this index at least once
            // before, define a new window for it that hasn't
            // previously been redefined
            // OR
            // if three chars in a row with the same index,
            // define a new window (makeIndex will return
            // RESERVEDINDEX for INVALIDCHAR)
            if((fIndexCount[curIndex] > 1) ||
               (curIndex == makeIndex(nextUC) 
            && curIndex == makeIndex(forwardUC))) {
                                // make sure there is enough room to
                                // write all three bytes if not,
                                // rewind the source stream and break
                                // out
            if((bytePos + 2) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            // get least recently defined window
            whichWindow = getLRDefinedWindow();

            byteBuffer[bytePos++] = (byte)(UDEFINE0 + whichWindow);
            byteBuffer[bytePos++] = (byte) curIndex;
            byteBuffer[bytePos++] = (byte) 
                (curUC - sOffsetTable[curIndex] 
                 + COMPRESSIONOFFSET);
            
            fOffsets[whichWindow] = sOffsetTable[curIndex];
            fCurrentWindow = whichWindow;
            fTimeStamps [whichWindow] = ++fTimeStamp;
            fMode = SINGLEBYTEMODE;
            break unicodeModeLoop;
            }
            
            // otherwise just quote the unicode, and save our
            // windows for longer runs
            else {
                                // make sure there is enough room to
                                // write all three bytes if not,
                                // rewind the source stream and break
                                // out
            if((bytePos + 2) >= byteBufferLimit) 
                { --ucPos; break mainLoop; }

            hiByte = curUC >>> 8;
            loByte = curUC & 0xFF;

            if(sUnicodeTagTable[ hiByte ])
                // add quote Unicode tag
                byteBuffer[bytePos++] = (byte) UQUOTEU;  
            
            byteBuffer[bytePos++] = (byte) hiByte;
            byteBuffer[bytePos++] = (byte) loByte;
            }
        }
        }
        }  // end switch
    }
    
        // fill in output parameter
    if(charsRead != null)
        charsRead [0] = (ucPos - charBufferStart);
        
        // return # of bytes written
        return (bytePos - byteBufferStart);
    }

    /** 
     * Reset the compressor to its initial state.
     */
    public void reset()
    {
    int i;

        // reset dynamic windows
        fOffsets[0] = 0x0080;    // Latin-1
        fOffsets[1] = 0x00C0;    // Latin-1 Supplement + Latin Extended-A
        fOffsets[2] = 0x0400;    // Cyrillic
        fOffsets[3] = 0x0600;    // Arabic
        fOffsets[4] = 0x0900;    // Devanagari
        fOffsets[5] = 0x3040;    // Hiragana
        fOffsets[6] = 0x30A0;    // Katakana
        fOffsets[7] = 0xFF00;    // Fullwidth ASCII


        // reset time stamps
        for(i = 0; i < NUMWINDOWS; i++) {
            fTimeStamps[i]          = 0;
        }

        // reset count of seen indices
        for(i = 0; i <= MAXINDEX; i++ ) {
            fIndexCount[i] = 0;
        }

        fTimeStamp      = 0;                // Reset current time stamp
        fCurrentWindow  = 0;                // Make current window Latin-1
        fMode           = SINGLEBYTEMODE;   // Always start in single-byte mode
    }

    //==========================
    // Determine the index for a character
    //==========================

    /**
     * Create the index value for a character.
     * For more information on this function, refer to table X-3
     * <A HREF="http://www.unicode.org/unicode/reports/tr6">UTR6</A>.
     * @param c The character in question.
     * @return An index for c
     */
    private static int makeIndex(int c)
    {
        // check the predefined indices
        if(c >= 0x00C0 && c < 0x0140)
            return LATININDEX;
        else if(c >= 0x0250 && c < 0x02D0)
            return IPAEXTENSIONINDEX;
        else if(c >= 0x0370 && c < 0x03F0)
            return GREEKINDEX;
        else if(c >= 0x0530 && c < 0x0590)
            return ARMENIANINDEX;
        else if(c >= 0x3040 && c < 0x30A0)
            return HIRAGANAINDEX;
        else if(c >= 0x30A0 && c < 0x3120)
            return KATAKANAINDEX;
        else if(c >= 0xFF60 && c < 0xFF9F)
            return HALFWIDTHKATAKANAINDEX;

        // calculate index
        else if(c >= 0x0080 && c < 0x3400)
            return (c / 0x80) & 0xFF;
        else if(c >= 0xE000 && c <= 0xFFFF)
            return ((c - 0xAC00) / 0x80) & 0xFF;
            
        // should never happen
        else {
            return RESERVEDINDEX;
        }
    }

    //==========================
    // Check if a given character fits in a window
    //==========================

    /**
    * Determine if a character is in a dynamic window.
    * @param c The character to test
    * @param whichWindow The dynamic window the test
    * @return true if <TT>c</TT> will fit in <TT>whichWindow</TT>, 
    * false otherwise.
    */
    private boolean inDynamicWindow(int c, 
                    int whichWindow)
    {
        return (c >= fOffsets[whichWindow] 
        && c < (fOffsets[whichWindow] + 0x80));
    }

    /**
     * Determine if a character is in a static window.
    * @param c The character to test
    * @param whichWindow The static window the test
    * @return true if <TT>c</TT> will fit in <TT>whichWindow</TT>, 
    * false otherwise.
    */
    private static boolean inStaticWindow(int c, 
                      int whichWindow)
    {
        return (c >= sOffsets[whichWindow]
        && c < (sOffsets[whichWindow] + 0x80));
    }

    //==========================
    // Check if a given character is compressible
    //==========================

    /**
    * Determine if a character is compressible.
    * @param c The character to test.
    * @return true if the <TT>c</TT> is compressible, false otherwise.
    */
    private static boolean isCompressible(int c)
    {
        return (c < 0x3400 || c >= 0xE000);
    }

    //==========================
    // Check if a window is defined for a given character
    //==========================

    /**
     * Determine if a dynamic window for a certain character is defined
     * @param c The character in question
     * @return The dynamic window containing <TT>c</TT>, or 
     * INVALIDWINDOW if not defined.
     */
    private int findDynamicWindow(int c)
    {
    // supposedly faster to count down
        //for(int i = 0; i < NUMWINDOWS; i++) {
    for(int i = NUMWINDOWS - 1; i >= 0; --i) {
        if(inDynamicWindow(c, i)) {
        ++fTimeStamps[i];
                return i;
        }
    }
        
        return INVALIDWINDOW;
    }

    /**
     * Determine if a static window for a certain character is defined
     * @param c The character in question
     * @return The static window containing <TT>c</TT>, or 
     * INVALIDWINDOW if not defined.
     */
    private static int findStaticWindow(int c)
    {
    // supposedly faster to count down
        //for(int i = 0; i < NUMSTATICWINDOWS; i++) {
    for(int i = NUMSTATICWINDOWS - 1; i >= 0; --i) {
        if(inStaticWindow(c, i)) {
                return i;
        }
    }
    
        return INVALIDWINDOW;
    }
    
    //==========================
    // Find the least-recently used window
    //==========================

    /** Find the least-recently defined window */
    private int getLRDefinedWindow()
    {
        int leastRU         = Integer.MAX_VALUE;
        int whichWindow     = INVALIDWINDOW;

        // find least recently used window
        // supposedly faster to count down
        //for( int i = 0; i < NUMWINDOWS; i++ ) {
        for(int i = NUMWINDOWS - 1; i >= 0; --i ) {
            if( fTimeStamps[i] < leastRU ) {
                leastRU   = fTimeStamps[i];
                whichWindow  = i;
            }
        }

        return whichWindow;
    }
    
}
