/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.FileChannel.MapMode;

import junit.framework.TestCase;

public class MappedByteBufferTest extends TestCase {

    File tmpFile, emptyFile;
    
    /**
     * A regression test for failing to correctly set capacity of underlying
     * wrapped buffer from a mapped byte buffer.
     */
    public void testasIntBuffer() throws IOException {
        // Map file
        FileInputStream fis = new FileInputStream(tmpFile);
        FileChannel fc = fis.getChannel();
        MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc
                .size());
        int len = mmb.capacity();
        assertEquals("Got wrong number of bytes", 46, len); //$NON-NLS-1$

        // Read in our 26 bytes
        for (int i = 0; i < 26; i++) {
            byte b = mmb.get();
            assertEquals("Got wrong byte value", (byte) 'A' + i, b); //$NON-NLS-1$
        }

        // Now convert to an IntBuffer to read our ints
        IntBuffer ibuffer = mmb.asIntBuffer();
        for (int i = 0; i < 5; i++) {
            int val = ibuffer.get();
            assertEquals("Got wrong int value", i + 1, val); //$NON-NLS-1$
        }
        fc.close();
    }
    
    /**
     * Regression for HARMONY-6315 - FileChannel.map throws IOException
     * when called with size 0
     * 
     * @throws IOException
     */
    public void testEmptyBuffer() throws IOException {
    	// Map empty file
        FileInputStream fis = new FileInputStream(emptyFile);
        FileChannel fc = fis.getChannel();
        MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        
        // check non-null
        assertNotNull("MappedByteBuffer created from empty file should not be null", 
        		mmb);
        
        // check capacity is 0
        int len = mmb.capacity();
        assertEquals("MappedByteBuffer created from empty file should have 0 capacity", 
        		0, len);
        
        assertFalse("MappedByteBuffer from empty file shouldn't be backed by an array ", 
        		mmb.hasArray());
        
        try
        {
        	byte b = mmb.get();
        	fail("Calling MappedByteBuffer.get() on empty buffer should throw a BufferUnderflowException");
        }
        catch (BufferUnderflowException e)
        {
        	// expected behaviour
        }
        
        // test expected exceptions thrown
        try 
        {
        	mmb = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size());
        	fail("Expected NonWritableChannelException to be thrown");
        }
        catch (NonWritableChannelException e)
        {
        	// expected behaviour
        }
        try
        {
        	mmb = fc.map(FileChannel.MapMode.PRIVATE, 0, fc.size());
        	fail("Expected NonWritableChannelException to be thrown");
        }
        catch (NonWritableChannelException e)
        {
        	// expected behaviour
        }
        fc.close();
    }
    
    /**
     * @tests {@link java.nio.MappedByteBuffer#force()}
     */
    public void test_force() throws IOException {
        // buffer was not mapped in read/write mode
        FileInputStream fileInputStream = new FileInputStream(tmpFile);
        FileChannel fileChannelRead = fileInputStream.getChannel();
        MappedByteBuffer mmbRead = fileChannelRead.map(MapMode.READ_ONLY, 0,
                fileChannelRead.size());

        mmbRead.force();

        FileInputStream inputStream = new FileInputStream(tmpFile);
        FileChannel fileChannelR = inputStream.getChannel();
        MappedByteBuffer resultRead = fileChannelR.map(MapMode.READ_ONLY, 0,
                fileChannelR.size());

        //If this buffer was not mapped in read/write mode, then invoking this method has no effect.
        assertEquals(
                "Invoking force() should have no effect when this buffer was not mapped in read/write mode",
                mmbRead, resultRead);

        // Buffer was mapped in read/write mode
        RandomAccessFile randomFile = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannelReadWrite = randomFile.getChannel();
        MappedByteBuffer mmbReadWrite = fileChannelReadWrite.map(
                FileChannel.MapMode.READ_WRITE, 0, fileChannelReadWrite.size());

        mmbReadWrite.put((byte) 'o');
        mmbReadWrite.force();

        RandomAccessFile random = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannelRW = random.getChannel();
        MappedByteBuffer resultReadWrite = fileChannelRW.map(
                FileChannel.MapMode.READ_WRITE, 0, fileChannelRW.size());

        // Invoking force() will change the buffer
        assertFalse(mmbReadWrite.equals(resultReadWrite));
        
        fileChannelRead.close();
        fileChannelR.close();
        fileChannelReadWrite.close();
        fileChannelRW.close();
    }

    /**
     * @tests {@link java.nio.MappedByteBuffer#load()}
     */
    public void test_load() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(tmpFile);
        FileChannel fileChannelRead = fileInputStream.getChannel();
        MappedByteBuffer mmbRead = fileChannelRead.map(MapMode.READ_ONLY, 0,
                fileChannelRead.size());
        
        assertEquals(mmbRead, mmbRead.load());

        RandomAccessFile randomFile = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannelReadWrite = randomFile.getChannel();
        MappedByteBuffer mmbReadWrite = fileChannelReadWrite.map(
                FileChannel.MapMode.READ_WRITE, 0, fileChannelReadWrite.size());

        assertEquals(mmbReadWrite, mmbReadWrite.load());
        
        fileChannelRead.close();
        fileChannelReadWrite.close();
    }

    protected void setUp() throws IOException {
        // Create temp file with 26 bytes and 5 ints
        tmpFile = File.createTempFile("harmony", "test");  //$NON-NLS-1$//$NON-NLS-2$
        tmpFile.deleteOnExit();
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        FileChannel fileChannel = fileOutputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(26 + 20);
        for (int i = 0; i < 26; i++) {
            byteBuffer.put((byte) ('A' + i));
        }
        for (int i = 0; i < 5; i++) {
            byteBuffer.putInt(i + 1);
        }
        byteBuffer.rewind();
        fileChannel.write(byteBuffer);
        fileChannel.close();
        fileOutputStream.close();
        
        emptyFile = File.createTempFile("harmony", "test");  //$NON-NLS-1$//$NON-NLS-2$
        emptyFile.deleteOnExit();
    }

    public void test_position() throws IOException {
        File tmp = File.createTempFile("hmy", "tmp");
        tmp.deleteOnExit();
        RandomAccessFile f = new RandomAccessFile(tmp, "rw");
        FileChannel ch = f.getChannel();
        MappedByteBuffer mbb = ch.map(MapMode.READ_WRITE, 0L, 100L);
        ch.close();

        mbb.putInt(1, 1);
        mbb.position(50);
        mbb.putInt(50);

        mbb.flip();
        mbb.get();
        assertEquals(1, mbb.getInt());

        mbb.position(50);
        assertEquals(50, mbb.getInt());
    }
}
