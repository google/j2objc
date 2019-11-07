package com.strobel.decompiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import com.strobel.decompiler.languages.LineNumberPosition;

/**
 * A <code>LineNumberFormatter</code> is used to rewrite an existing .java file, introducing
 * line number information.  It can handle either, or both, of the following jobs:
 * 
 * <ul>
 *   <li>Introduce line numbers as leading comments.
 *   <li>Stretch the file so that the line number comments match the physical lines.
 * </ul>
 */
public class LineNumberFormatter {
    private final List<LineNumberPosition> _positions;
    private final File _file;
    private final EnumSet<LineNumberOption> _options;
    
    public enum LineNumberOption
    {
        LEADING_COMMENTS,
        STRETCHED,
    }
    
    /**
     * Constructs an instance.
     * 
     * @param file the file whose line numbers should be fixed
     * @param lineNumberPositions a recipe for how to fix the line numbers in 'file'.
     * @param options controls how 'this' represents line numbers in the resulting file
     */
    public LineNumberFormatter(File file,
            List<LineNumberPosition> lineNumberPositions,
            EnumSet<LineNumberOption> options) {
        _file = file;
        _positions = lineNumberPositions;
        _options = (options == null ? EnumSet.noneOf( LineNumberOption.class) : options);
    }

    /**
     * Rewrites the file passed to 'this' constructor so that the actual line numbers match
     * the recipe passed to 'this' constructor.
     */
    public void reformatFile() throws IOException {
        List<LineNumberPosition> lineBrokenPositions = new ArrayList<LineNumberPosition>();
        List<String> brokenLines = breakLines( lineBrokenPositions);
        emitFormatted( brokenLines, lineBrokenPositions);
    }
    
    /**
     * Processes {@link #_file}, breaking apart any lines on which multiple line-number markers
     * appear in different columns.
     * 
     * @return the list of broken lines
     */
    private List<String> breakLines( List<LineNumberPosition> o_LineBrokenPositions) throws IOException {
        int numLinesRead = 0;
        int lineOffset = 0;
        List<String> brokenLines = new ArrayList<>();

        try( BufferedReader r = new BufferedReader( new FileReader( _file))) {
            for ( int posIndex=0; posIndex<_positions.size(); posIndex++) {
                LineNumberPosition pos = _positions.get( posIndex);
                o_LineBrokenPositions.add( new LineNumberPosition(
                        pos.getOriginalLine(), pos.getEmittedLine()+lineOffset, pos.getEmittedColumn()));
                
                // Copy the input file up to but not including the emitted line # in "pos".
                while ( numLinesRead < pos.getEmittedLine()-1) {
                    brokenLines.add( r.readLine());
                    numLinesRead++;
                }
                
                // Read the line that contains the next line number annotations, but don't write it yet.
                String line = r.readLine();
                numLinesRead++;
                
                // See if there are two original line annotations on the same emitted line.
                LineNumberPosition nextPos;
                int prevPartLen = 0;
                char[] indent = {};
                do {
                    nextPos = (posIndex < _positions.size()-1) ? _positions.get( posIndex+1) : null;
                    if ( nextPos != null
                        && nextPos.getEmittedLine() == pos.getEmittedLine()
                        && nextPos.getOriginalLine() > pos.getOriginalLine()) {
                        // Two different source line numbers on the same emitted line!
                        posIndex++;
                        lineOffset++;
                        String firstPart = line.substring( 0, nextPos.getEmittedColumn() - prevPartLen - 1);
                        brokenLines.add( new String(indent) + firstPart);
                        prevPartLen += firstPart.length();
                        indent = new char[prevPartLen];
                        Arrays.fill( indent, ' ');
                        line = line.substring( firstPart.length(), line.length());
                        
                        // Alter the position while adding it.
                        o_LineBrokenPositions.add( new LineNumberPosition(
                                nextPos.getOriginalLine(), nextPos.getEmittedLine()+lineOffset, nextPos.getEmittedColumn()));
                    } else {
                        nextPos = null;
                    }
                } while ( nextPos != null);
                
                // Nothing special here-- just emit the line.
                brokenLines.add( new String(indent) + line);
            }
            
            // Copy out the remainder of the file.
            String line;
            while ( (line = r.readLine()) != null) {
                brokenLines.add( line);
            }
        }
        return brokenLines;
    }
    
    private void emitFormatted( List<String> brokenLines, List<LineNumberPosition> lineBrokenPositions) throws IOException {
        File tempFile = new File( _file.getAbsolutePath() + ".fixed");
        int globalOffset = 0;
        int numLinesRead = 0;
        Iterator<String> lines = brokenLines.iterator();
        
        int maxLineNo = LineNumberPosition.computeMaxLineNumber( lineBrokenPositions);
        try( LineNumberPrintWriter w = new LineNumberPrintWriter( 
                maxLineNo, new BufferedWriter( new FileWriter( tempFile)))) {
            
            // Suppress all line numbers if we weren't asked to show them.
            if ( ! _options.contains( LineNumberOption.LEADING_COMMENTS)) {
                w.suppressLineNumbers();
            }
            
            // Suppress stretching if we weren't asked to do it.
            boolean doStretching = (_options.contains( LineNumberOption.STRETCHED));
            
            for ( LineNumberPosition pos : lineBrokenPositions) {
                int nextTarget = pos.getOriginalLine();
                int nextActual = pos.getEmittedLine();
                int requiredAdjustment = (nextTarget - nextActual - globalOffset);
                
                if (doStretching && requiredAdjustment < 0) {
                    // We currently need to remove newlines to squeeze things together.
                    // prefer to remove empty lines, 
                    // 1. read all lines before nextActual and remove empty lines as needed
                    List<String> stripped = new ArrayList<>();
                    while( numLinesRead < nextActual - 1) {
                        String line = lines.next();
                        numLinesRead++;
                        if ((requiredAdjustment < 0) && line.trim().isEmpty()) {
                            requiredAdjustment++;
                            globalOffset--;
                        } else {
                            stripped.add(line);
                        }
                    }
                    // 2. print non empty lines while stripping further as needed
                    int lineNoToPrint = (stripped.size() + requiredAdjustment <= 0) 
                        ? nextTarget : LineNumberPrintWriter.NO_LINE_NUMBER;
                    for (String line : stripped) {
                        if (requiredAdjustment < 0) {
                            w.print( lineNoToPrint, line);
                            w.print( "  ");
                            requiredAdjustment++;
                            globalOffset--;
                        } else {
                            w.println( lineNoToPrint, line);
                        }
                    }
                    // 3. read and print next actual
                    String line = lines.next();
                    numLinesRead++;
                    if (requiredAdjustment < 0) {
                        w.print( nextTarget, line);
                        w.print( "  ");
                        globalOffset--;
                    } else {
                        w.println( nextTarget, line);
                    }

                } else {
                    while( numLinesRead < nextActual) {
                        String line = lines.next();
                        numLinesRead++;
                        boolean isLast = (numLinesRead >= nextActual);
                        int lineNoToPrint = isLast ? nextTarget : LineNumberPrintWriter.NO_LINE_NUMBER;
                        
                        if ( requiredAdjustment > 0 && doStretching) {
                            // We currently need to inject newlines to space things out.
                            do {
                                w.println( "");
                                requiredAdjustment--;
                                globalOffset++;
                            } while ( isLast && requiredAdjustment > 0);
                            w.println( lineNoToPrint, line);
                        } else {
                            // No tweaks needed-- we are on the ball.
                            w.println( lineNoToPrint, line);
                        }
                    }
                }
            }
            
            // Finish out the file.
            String line;
            while ( lines.hasNext()) {
                line = lines.next();
                w.println( line);
            }
        }
        
        // Delete the original file and rename the formatted temp file over the original.
        _file.delete();
        tempFile.renameTo( _file);
    }

}
