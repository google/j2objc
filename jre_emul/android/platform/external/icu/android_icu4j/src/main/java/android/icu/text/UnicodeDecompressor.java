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
* A decompression engine implementing the Standard Compression Scheme
* for Unicode (SCSU) as outlined in <A
* HREF="http://www.unicode.org/unicode/reports/tr6">Unicode Technical
* Report #6</A>.
*
* <P><STRONG>USAGE</STRONG></P>
*
* <P>The static methods on <TT>UnicodeDecompressor</TT> may be used in a
* straightforward manner to decompress simple strings:</P>
*
* <PRE>
*  byte [] compressed = ... ; // get compressed bytes from somewhere
*  String result = UnicodeDecompressor.decompress(compressed);
* </PRE>
*
* <P>The static methods have a fairly large memory footprint.
* For finer-grained control over memory usage, 
* <TT>UnicodeDecompressor</TT> offers more powerful APIs allowing
* iterative decompression:</P>
*
* <PRE>
*  // Decompress an array "bytes" of length "len" using a buffer of 512 chars
*  // to the Writer "out"
*
*  UnicodeDecompressor myDecompressor         = new UnicodeDecompressor();
*  final static int    BUFSIZE                = 512;
*  char []             charBuffer             = new char [ BUFSIZE ];
*  int                 charsWritten           = 0;
*  int []              bytesRead              = new int [1];
*  int                 totalBytesDecompressed = 0;
*  int                 totalCharsWritten      = 0;
*
*  do {
*    // do the decompression
*    charsWritten = myDecompressor.decompress(bytes, totalBytesDecompressed, 
*                                             len, bytesRead,
*                                             charBuffer, 0, BUFSIZE);
*
*    // do something with the current set of chars
*    out.write(charBuffer, 0, charsWritten);
*
*    // update the no. of bytes decompressed
*    totalBytesDecompressed += bytesRead[0];
*
*    // update the no. of chars written
*    totalCharsWritten += charsWritten;
*
*  } while(totalBytesDecompressed &lt; len);
*
*  myDecompressor.reset(); // reuse decompressor
* </PRE>
*
* <P>Decompression is performed according to the standard set forth in 
* <A HREF="http://www.unicode.org/unicode/reports/tr6">Unicode Technical 
* Report #6</A></P>
*
* @see UnicodeCompressor
*
* @author Stephen F. Booth
* @hide Only a subset of ICU is exposed in Android
*/
public final class UnicodeDecompressor implements SCSU
{
    //==========================
    // Instance variables
    //==========================
    
    /** Alias to current dynamic window */
    private int       fCurrentWindow   = 0;

    /** Dynamic compression window offsets */
    private int []    fOffsets         = new int [ NUMWINDOWS ];

    /** Current compression mode */
    private int       fMode            = SINGLEBYTEMODE;

    /** Size of our internal buffer */
    private final static int BUFSIZE   = 3;

    /** Internal buffer for saving state */
    private byte []   fBuffer          = new byte [BUFSIZE];

    /** Number of characters in our internal buffer */
    private int       fBufferLength    = 0;
    

    /**
     * Create a UnicodeDecompressor.
     * Sets all windows to their default values.
     * @see #reset
     */
    public UnicodeDecompressor(){
        reset();              // initialize to defaults
    }

    /**
     * Decompress a byte array into a String.
     * @param buffer The byte array to decompress.
     * @return A String containing the decompressed characters.
     * @see #decompress(byte [], int, int)
     */
    public static String decompress(byte [] buffer){
        char [] buf = decompress(buffer, 0, buffer.length);
        return new String(buf);
    }

    /**
     * Decompress a byte array into a Unicode character array.
     * @param buffer The byte array to decompress.
     * @param start The start of the byte run to decompress.
     * @param limit The limit of the byte run to decompress.
     * @return A character array containing the decompressed bytes.
     * @see #decompress(byte [])
     */
    public static char [] decompress(byte [] buffer, int start, int limit) {
        UnicodeDecompressor comp = new UnicodeDecompressor();
    
        // use a buffer we know will never overflow
        // in the worst case, each byte will decompress
        // to a surrogate pair (buffer must be at least 2 chars)
        int len = Math.max(2, 2 * (limit - start));
        char [] temp = new char [len];
    
        int charCount = comp.decompress(buffer, start, limit, null, 
                        temp, 0, len);
    
        char [] result = new char [charCount];
        System.arraycopy(temp, 0, result, 0, charCount);
        return result;
    }
    
    /**
     * Decompress a byte array into a Unicode character array.
     *
     * This function will either completely fill the output buffer, 
     * or consume the entire input.  
     *
     * @param byteBuffer The byte buffer to decompress.
     * @param byteBufferStart The start of the byte run to decompress.
     * @param byteBufferLimit The limit of the byte run to decompress.
     * @param bytesRead A one-element array.  If not null, on return
     * the number of bytes read from byteBuffer.
     * @param charBuffer A buffer to receive the decompressed data. 
     * This buffer must be at minimum two characters in size.
     * @param charBufferStart The starting offset to which to write 
     * decompressed data.
     * @param charBufferLimit The limiting offset for writing 
     * decompressed data.
     * @return The number of Unicode characters written to charBuffer.
     */
    public int decompress(byte []    byteBuffer,
              int        byteBufferStart,
              int        byteBufferLimit,
              int []     bytesRead,
              char []    charBuffer,
              int        charBufferStart,
              int        charBufferLimit)
    {
    // the current position in the source byte buffer
    int bytePos      = byteBufferStart;
    
    // the current position in the target char buffer
    int ucPos        = charBufferStart;
        
        // the current byte from the source buffer
    int aByte        = 0x00;


    // charBuffer must be at least 2 chars in size
    if(charBuffer.length < 2 || (charBufferLimit - charBufferStart) < 2)
        throw new IllegalArgumentException("charBuffer.length < 2");
    
    // if our internal buffer isn't empty, flush its contents
    // to the output buffer before doing any more decompression
    if(fBufferLength > 0) {

        int newBytes = 0;

        // fill the buffer completely, to guarantee one full character
        if(fBufferLength != BUFSIZE) {
        newBytes = fBuffer.length - fBufferLength;

        // verify there are newBytes bytes in byteBuffer
        if(byteBufferLimit - byteBufferStart < newBytes)
            newBytes = byteBufferLimit - byteBufferStart;

        System.arraycopy(byteBuffer, byteBufferStart, 
                 fBuffer, fBufferLength, newBytes);
        }

        // reset buffer length to 0 before recursive call
        fBufferLength = 0;

        // call self recursively to decompress the buffer
        int count = decompress(fBuffer, 0, fBuffer.length, null,
                   charBuffer, charBufferStart, 
                   charBufferLimit);

        // update the positions into the arrays
        ucPos += count;
        bytePos += newBytes;
    }

        // the main decompression loop
    mainLoop:
    while(bytePos < byteBufferLimit && ucPos < charBufferLimit) {
        switch(fMode) {  
        case SINGLEBYTEMODE:
        // single-byte mode decompression loop
        singleByteModeLoop:
        while(bytePos < byteBufferLimit && ucPos < charBufferLimit) {
        aByte = byteBuffer[bytePos++] & 0xFF;
        switch(aByte) {
            // All bytes from 0x80 through 0xFF are remapped
            // to chars or surrogate pairs according to the
            // currently active window
        case 0x80: case 0x81: case 0x82: case 0x83: case 0x84: 
        case 0x85: case 0x86: case 0x87: case 0x88: case 0x89:
        case 0x8A: case 0x8B: case 0x8C: case 0x8D: case 0x8E:
        case 0x8F: case 0x90: case 0x91: case 0x92: case 0x93:
        case 0x94: case 0x95: case 0x96: case 0x97: case 0x98:
        case 0x99: case 0x9A: case 0x9B: case 0x9C: case 0x9D:
        case 0x9E: case 0x9F: case 0xA0: case 0xA1: case 0xA2:
        case 0xA3: case 0xA4: case 0xA5: case 0xA6: case 0xA7:
        case 0xA8: case 0xA9: case 0xAA: case 0xAB: case 0xAC:
        case 0xAD: case 0xAE: case 0xAF: case 0xB0: case 0xB1:
        case 0xB2: case 0xB3: case 0xB4: case 0xB5: case 0xB6:
        case 0xB7: case 0xB8: case 0xB9: case 0xBA: case 0xBB:
        case 0xBC: case 0xBD: case 0xBE: case 0xBF: case 0xC0:
        case 0xC1: case 0xC2: case 0xC3: case 0xC4: case 0xC5:
        case 0xC6: case 0xC7: case 0xC8: case 0xC9: case 0xCA:
        case 0xCB: case 0xCC: case 0xCD: case 0xCE: case 0xCF:
        case 0xD0: case 0xD1: case 0xD2: case 0xD3: case 0xD4:
        case 0xD5: case 0xD6: case 0xD7: case 0xD8: case 0xD9:
        case 0xDA: case 0xDB: case 0xDC: case 0xDD: case 0xDE:
        case 0xDF: case 0xE0: case 0xE1: case 0xE2: case 0xE3:
        case 0xE4: case 0xE5: case 0xE6: case 0xE7: case 0xE8:
        case 0xE9: case 0xEA: case 0xEB: case 0xEC: case 0xED:
        case 0xEE: case 0xEF: case 0xF0: case 0xF1: case 0xF2:
        case 0xF3: case 0xF4: case 0xF5: case 0xF6: case 0xF7:
        case 0xF8: case 0xF9: case 0xFA: case 0xFB: case 0xFC:
        case 0xFD: case 0xFE: case 0xFF: 
            // For offsets <= 0xFFFF, convert to a single char
            // by adding the window's offset and subtracting
            // the generic compression offset
            if(fOffsets[ fCurrentWindow ] <= 0xFFFF) {
            charBuffer[ucPos++] = (char) 
                (aByte + fOffsets[ fCurrentWindow ] 
                 - COMPRESSIONOFFSET);
            }
            // For offsets > 0x10000, convert to a surrogate pair by 
            // normBase = window's offset - 0x10000
            // high surr. = 0xD800 + (normBase >> 10)
            // low  surr. = 0xDC00 + (normBase & 0x3FF) + (byte & 0x7F)
            else {
            // make sure there is enough room to write
            // both characters 
            // if not, save state and break out
            if((ucPos + 1) >= charBufferLimit) {
                --bytePos;
                System.arraycopy(byteBuffer, bytePos,
                         fBuffer, 0, 
                         byteBufferLimit - bytePos);
                fBufferLength = byteBufferLimit - bytePos;
                bytePos += fBufferLength;
                break mainLoop; 
            }
            
            int normalizedBase = fOffsets[ fCurrentWindow ] 
                - 0x10000;
            charBuffer[ucPos++] = (char) 
                (0xD800 + (normalizedBase >> 10));
            charBuffer[ucPos++] = (char) 
                (0xDC00 + (normalizedBase & 0x3FF)+(aByte & 0x7F));
            }
            break;

            // bytes from 0x20 through 0x7F are treated as ASCII and
            // are remapped to chars by padding the high byte
            // (this is the same as quoting from static window 0)
            // NUL (0x00), HT (0x09), CR (0x0A), LF (0x0D) 
            // are treated as ASCII as well
        case 0x00: case 0x09: case 0x0A: case 0x0D:
        case 0x20: case 0x21: case 0x22: case 0x23: case 0x24:
        case 0x25: case 0x26: case 0x27: case 0x28: case 0x29:
        case 0x2A: case 0x2B: case 0x2C: case 0x2D: case 0x2E:
        case 0x2F: case 0x30: case 0x31: case 0x32: case 0x33:
        case 0x34: case 0x35: case 0x36: case 0x37: case 0x38:
        case 0x39: case 0x3A: case 0x3B: case 0x3C: case 0x3D:
        case 0x3E: case 0x3F: case 0x40: case 0x41: case 0x42:
        case 0x43: case 0x44: case 0x45: case 0x46: case 0x47:
        case 0x48: case 0x49: case 0x4A: case 0x4B: case 0x4C:
        case 0x4D: case 0x4E: case 0x4F: case 0x50: case 0x51:
        case 0x52: case 0x53: case 0x54: case 0x55: case 0x56:
        case 0x57: case 0x58: case 0x59: case 0x5A: case 0x5B:
        case 0x5C: case 0x5D: case 0x5E: case 0x5F: case 0x60:
        case 0x61: case 0x62: case 0x63: case 0x64: case 0x65:
        case 0x66: case 0x67: case 0x68: case 0x69: case 0x6A:
        case 0x6B: case 0x6C: case 0x6D: case 0x6E: case 0x6F:
        case 0x70: case 0x71: case 0x72: case 0x73: case 0x74:
        case 0x75: case 0x76: case 0x77: case 0x78: case 0x79:
        case 0x7A: case 0x7B: case 0x7C: case 0x7D: case 0x7E:
        case 0x7F: 
            charBuffer[ucPos++] = (char) aByte;
            break;

            // quote unicode
        case SQUOTEU:
            // verify we have two bytes following tag
            // if not, save state and break out
            if( (bytePos + 1) >= byteBufferLimit ) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }
                
            aByte = byteBuffer[bytePos++];
            charBuffer[ucPos++] = (char)
            (aByte << 8 | (byteBuffer[bytePos++] & 0xFF));
            break;

            // switch to Unicode mode
        case SCHANGEU:
            fMode = UNICODEMODE;
            break singleByteModeLoop;
            //break;

            // handle all quote tags
        case SQUOTE0: case SQUOTE1: case SQUOTE2: case SQUOTE3:
        case SQUOTE4: case SQUOTE5: case SQUOTE6: case SQUOTE7:
            // verify there is a byte following the tag
            // if not, save state and break out
            if(bytePos >= byteBufferLimit) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }
                
            // if the byte is in the range 0x00 - 0x7F, use
            // static window n otherwise, use dynamic window n
            int dByte = byteBuffer[bytePos++] & 0xFF;
            charBuffer[ucPos++] = (char) 
            (dByte+ (dByte >= 0x00 && dByte < 0x80 
                 ? sOffsets[aByte - SQUOTE0] 
                 : (fOffsets[aByte - SQUOTE0] 
                    - COMPRESSIONOFFSET))); 
            break;

            // handle all change tags
        case SCHANGE0: case SCHANGE1: case SCHANGE2: case SCHANGE3:
        case SCHANGE4: case SCHANGE5: case SCHANGE6: case SCHANGE7:
            fCurrentWindow = aByte - SCHANGE0;
            break;

            // handle all define tags
        case SDEFINE0: case SDEFINE1: case SDEFINE2: case SDEFINE3:
        case SDEFINE4: case SDEFINE5: case SDEFINE6: case SDEFINE7:
            // verify there is a byte following the tag
            // if not, save state and break out
            if(bytePos >= byteBufferLimit) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }

            fCurrentWindow = aByte - SDEFINE0;
            fOffsets[fCurrentWindow] = 
            sOffsetTable[byteBuffer[bytePos++] & 0xFF];
            break;

            // handle define extended tag
        case SDEFINEX:
            // verify we have two bytes following tag
            // if not, save state and break out
            if((bytePos + 1) >= byteBufferLimit ) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }
                
            aByte = byteBuffer[bytePos++] & 0xFF;
            fCurrentWindow = (aByte & 0xE0) >> 5;
            fOffsets[fCurrentWindow] = 0x10000 + 
            (0x80 * (((aByte & 0x1F) << 8) 
                 | (byteBuffer[bytePos++] & 0xFF)));
            break;
                            
            // reserved, shouldn't happen
        case SRESERVED:
            break;

        } // end switch
        } // end while
        break;

        case UNICODEMODE:
        // unicode mode decompression loop
        unicodeModeLoop:
        while(bytePos < byteBufferLimit && ucPos < charBufferLimit) {
        aByte = byteBuffer[bytePos++] & 0xFF;
        switch(aByte) {
            // handle all define tags
        case UDEFINE0: case UDEFINE1: case UDEFINE2: case UDEFINE3:
        case UDEFINE4: case UDEFINE5: case UDEFINE6: case UDEFINE7:
            // verify there is a byte following tag
            // if not, save state and break out
            if(bytePos >= byteBufferLimit ) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }
                
            fCurrentWindow = aByte - UDEFINE0;
            fOffsets[fCurrentWindow] = 
            sOffsetTable[byteBuffer[bytePos++] & 0xFF];
            fMode = SINGLEBYTEMODE;
            break unicodeModeLoop;
            //break;

            // handle define extended tag
        case UDEFINEX:
            // verify we have two bytes following tag
            // if not, save state and break out
            if((bytePos + 1) >= byteBufferLimit ) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }
            
            aByte = byteBuffer[bytePos++] & 0xFF;
            fCurrentWindow = (aByte & 0xE0) >> 5;
            fOffsets[fCurrentWindow] = 0x10000 + 
            (0x80 * (((aByte & 0x1F) << 8) 
                 | (byteBuffer[bytePos++] & 0xFF)));
            fMode = SINGLEBYTEMODE;
            break unicodeModeLoop;
            //break;

            // handle all change tags
        case UCHANGE0: case UCHANGE1: case UCHANGE2: case UCHANGE3:
        case UCHANGE4: case UCHANGE5: case UCHANGE6: case UCHANGE7:
            fCurrentWindow = aByte - UCHANGE0;
            fMode = SINGLEBYTEMODE;
            break unicodeModeLoop;
            //break;

            // quote unicode
        case UQUOTEU:
            // verify we have two bytes following tag
            // if not, save state and break out
            if(bytePos >= byteBufferLimit  - 1) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }
                
            aByte = byteBuffer[bytePos++];
            charBuffer[ucPos++] = (char) 
            (aByte << 8 | (byteBuffer[bytePos++] & 0xFF));
            break;

        default:
            // verify there is a byte following tag
            // if not, save state and break out
            if(bytePos >= byteBufferLimit ) {
            --bytePos;
            System.arraycopy(byteBuffer, bytePos,
                     fBuffer, 0, 
                     byteBufferLimit - bytePos);
            fBufferLength = byteBufferLimit - bytePos;
            bytePos += fBufferLength;
            break mainLoop; 
            }

            charBuffer[ucPos++] = (char) 
            (aByte << 8 | (byteBuffer[bytePos++] & 0xFF));
            break;

        } // end switch
        } // end while
        break;
        
        } // end switch( fMode )
    } // end while

        // fill in output parameter
    if(bytesRead != null)
        bytesRead [0] = (bytePos - byteBufferStart);

        // return # of chars written
    return (ucPos - charBufferStart);
    }

    /** 
     * Reset the decompressor to its initial state. 
     */
    public void reset()
    {
        // reset dynamic windows
        fOffsets[0] = 0x0080;    // Latin-1
        fOffsets[1] = 0x00C0;    // Latin-1 Supplement + Latin Extended-A
        fOffsets[2] = 0x0400;    // Cyrillic
        fOffsets[3] = 0x0600;    // Arabic
        fOffsets[4] = 0x0900;    // Devanagari
        fOffsets[5] = 0x3040;    // Hiragana
        fOffsets[6] = 0x30A0;    // Katakana
        fOffsets[7] = 0xFF00;    // Fullwidth ASCII


        fCurrentWindow  = 0;                // Make current window Latin-1
        fMode           = SINGLEBYTEMODE;   // Always start in single-byte mode
        fBufferLength   = 0;                // Empty buffer
    }
}
