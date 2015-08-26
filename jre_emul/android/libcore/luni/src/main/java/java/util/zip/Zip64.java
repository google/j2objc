/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package java.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.util.zip.ZipOutputStream.writeIntAsUint16;
import static java.util.zip.ZipOutputStream.writeLongAsUint32;
import static java.util.zip.ZipOutputStream.writeLongAsUint64;

/**
 * @hide
 */
public class Zip64 {

    /* Non instantiable */
    private Zip64() {}

    /**
     * The maximum supported entry / archive size for standard (non zip64) entries and archives.
     */
    static final long MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE = 0x00000000ffffffffL;

    /**
     * The header ID of the zip64 extended info header. This value is used to identify
     * zip64 data in the "extra" field in the file headers.
     */
    private static final short ZIP64_EXTENDED_INFO_HEADER_ID = 0x0001;

    /**
     * The minimum size of the zip64 extended info header. This excludes the 2 byte header ID
     * and the 2 byte size.
     */
    private static final int ZIP64_EXTENDED_INFO_MIN_SIZE = 28;

    /*
     * Size (in bytes) of the zip64 end of central directory locator. This will be located
     * immediately before the end of central directory record if a given zipfile is in the
     * zip64 format.
     */
    private static final int ZIP64_LOCATOR_SIZE = 20;

    /**
     * The zip64 end of central directory locator signature (4 bytes wide).
     */
    private static final int ZIP64_LOCATOR_SIGNATURE = 0x07064b50;

    /**
     * The zip64 end of central directory record singature (4 bytes wide).
     */
    private static final int ZIP64_EOCD_RECORD_SIGNATURE = 0x06064b50;

    /**
     * The "effective" size of the zip64 eocd record. This excludes the fields that
     * are proprietary, signature, or fields we aren't interested in. We include the
     * following (contiguous) fields in this calculation :
     * - disk number (4 bytes)
     * - disk with start of central directory (4 bytes)
     * - number of central directory entries on this disk (8 bytes)
     * - total number of central directory entries (8 bytes)
     * - size of the central directory (8 bytes)
     * - offset of the start of the central directory (8 bytes)
     */
    private static final int ZIP64_EOCD_RECORD_EFFECTIVE_SIZE = 40;

    /**
     * Parses the zip64 end of central directory record locator. The locator
     * must be placed immediately before the end of central directory (eocd) record
     * starting at {@code eocdOffset}.
     *
     * The position of the file cursor for {@code raf} after a call to this method
     * is undefined an callers must reposition it after each call to this method.
     */
    public static long parseZip64EocdRecordLocator(RandomAccessFile raf, long eocdOffset)
            throws IOException {
        // The spec stays curiously silent about whether a zip file with an EOCD record,
        // a zip64 locator and a zip64 eocd record is considered "empty". In our implementation,
        // we parse all records and read the counts from them instead of drawing any size or
        // layout based information.
        if (eocdOffset > ZIP64_LOCATOR_SIZE) {
            raf.seek(eocdOffset - ZIP64_LOCATOR_SIZE);
            if (Integer.reverseBytes(raf.readInt()) == ZIP64_LOCATOR_SIGNATURE) {
                byte[] zip64EocdLocator = new byte[ZIP64_LOCATOR_SIZE  - 4];
                raf.readFully(zip64EocdLocator);
                ByteBuffer buf = ByteBuffer.wrap(zip64EocdLocator).order(ByteOrder.LITTLE_ENDIAN);

                final int diskWithCentralDir = buf.getInt();
                final long zip64EocdRecordOffset = buf.getLong();
                final int numDisks = buf.getInt();

                if (numDisks != 1 || diskWithCentralDir != 0) {
                    throw new ZipException("Spanned archives not supported");
                }

                return zip64EocdRecordOffset;
            }
        }

        return -1;
    }

    public static ZipFile.EocdRecord parseZip64EocdRecord(RandomAccessFile raf,
            long eocdRecordOffset, int commentLength) throws IOException {
        raf.seek(eocdRecordOffset);
        final int signature = Integer.reverseBytes(raf.readInt());
        if (signature != ZIP64_EOCD_RECORD_SIGNATURE) {
            throw new ZipException("Invalid zip64 eocd record offset, sig="
                    + Integer.toHexString(signature) + " offset=" + eocdRecordOffset);
        }

        // The zip64 eocd record specifies its own size as an 8 byte integral type. It is variable
        // length because of the "zip64 extensible data sector" but that field is reserved for
        // pkware's proprietary use. We therefore disregard it altogether and treat the end of
        // central directory structure as fixed length.
        //
        // We also skip "version made by" (2 bytes) and "version needed to extract" (2 bytes)
        // fields. We perform additional validation at the ZipEntry level, where applicable.
        //
        // That's a total of 12 bytes to skip
        raf.skipBytes(12);

        byte[] zip64Eocd = new byte[ZIP64_EOCD_RECORD_EFFECTIVE_SIZE];
        raf.readFully(zip64Eocd);

        ByteBuffer buf = ByteBuffer.wrap(zip64Eocd).order(ByteOrder.LITTLE_ENDIAN);
        try {
            int diskNumber = buf.getInt();
            int diskWithCentralDirStart = buf.getInt();
            long numEntries = buf.getLong();
            long totalNumEntries = buf.getLong();
            buf.getLong(); // Ignore the size of the central directory
            long centralDirOffset = buf.getLong();

            if (numEntries != totalNumEntries || diskNumber != 0 || diskWithCentralDirStart != 0) {
                throw new ZipException("Spanned archives not supported :" +
                        " numEntries=" + numEntries + ", totalNumEntries=" + totalNumEntries +
                        ", diskNumber=" + diskNumber + ", diskWithCentralDirStart=" +
                        diskWithCentralDirStart);
            }

            return new ZipFile.EocdRecord(numEntries, centralDirOffset, commentLength);
        } catch (BufferUnderflowException bue) {
            ZipException zipException = new ZipException("Error parsing zip64 eocd record.");
            zipException.initCause(bue);
            throw zipException;
        }
    }

    /**
     * Parse the zip64 extended info record from the extras present in {@code ze}.
     *
     * If {@code fromCentralDirectory} is true, we assume we're parsing a central directory
     * record. We assume a local file header otherwise. The difference between the two is that
     * a central directory entry is required to be complete, whereas a local file header isn't.
     * This is due to the presence of an optional data descriptor after the file content.
     *
     * @return {@code} true iff. a zip64 extended info record was found.
     */
    public static boolean parseZip64ExtendedInfo(ZipEntry ze, boolean fromCentralDirectory)
            throws ZipException {
        int extendedInfoSize = -1;
        int extendedInfoStart = -1;
        // If this file contains a zip64 central directory locator, entries might
        // optionally contain a zip64 extended information extra entry.
        if (ze.extra != null && ze.extra.length > 0) {
            // Extensible data fields are of the form header1+data1 + header2+data2 and so
            // on, where each header consists of a 2 byte header ID followed by a 2 byte size.
            // We need to iterate through the entire list of headers to find the header ID
            // for the zip64 extended information extra field (0x0001).
            final ByteBuffer buf = ByteBuffer.wrap(ze.extra).order(ByteOrder.LITTLE_ENDIAN);
            extendedInfoSize = getZip64ExtendedInfoSize(buf);
            if (extendedInfoSize != -1) {
                extendedInfoStart = buf.position();
                try {
                    if (extendedInfoSize < ZIP64_EXTENDED_INFO_MIN_SIZE) {
                        throw new ZipException("Invalid zip64 extended info size: " + extendedInfoSize);
                    }

                    // The size & compressed size only make sense in the central directory *or* if
                    // we know them beforehand. If we don't know them beforehand, they're stored in
                    // the data descriptor and should be read from there.
                    if (fromCentralDirectory || (ze.getMethod() == ZipEntry.STORED)) {
                        final long zip64Size = buf.getLong();
                        if (ze.size == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
                            ze.size = zip64Size;
                        }

                        final long zip64CompressedSize = buf.getLong();
                        if (ze.compressedSize == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
                            ze.compressedSize = zip64CompressedSize;
                        }
                    }

                    // The local header offset is significant only in the central directory. It makes no
                    // sense within the local header itself.
                    if (fromCentralDirectory) {
                        final long zip64LocalHeaderRelOffset = buf.getLong();
                        if (ze.localHeaderRelOffset == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
                            ze.localHeaderRelOffset = zip64LocalHeaderRelOffset;
                        }
                    }
                } catch (BufferUnderflowException bue) {
                    ZipException zipException = new ZipException("Error parsing extendend info ");
                    zipException.initCause(bue);
                    throw zipException;
                }
            }
        }

        // This entry doesn't contain a zip64 extended information data entry header.
        // We have to check that the compressedSize / size / localHeaderRelOffset values
        // are valid and don't require the presence of the extended header.
        if (extendedInfoSize == -1) {
            if (ze.compressedSize == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE ||
                    ze.size == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE ||
                    ze.localHeaderRelOffset == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
                throw new ZipException("File contains no zip64 extended information: "
                        + "name=" + ze.name + "compressedSize=" + ze.compressedSize + ", size="
                        + ze.size + ", localHeader=" + ze.localHeaderRelOffset);
            }

            return false;
        } else {
            // If we're parsed the zip64 extended info header, we remove it from the extras
            // so that applications that set their own extras will see the data they set.

            // This is an unfortunate workaround needed due to a gap in the spec. The spec demands
            // that extras are present in the "extensible" format, which means that each extra field
            // must be prefixed with a header ID and a length. However, earlier versions of the spec
            // made no mention of this, nor did any existing API enforce it. This means users could
            // set "free form" extras without caring very much whether the implementation wanted to
            // extend or add to them.

            // The start of the extended info header.
            final int extendedInfoHeaderStart = extendedInfoStart - 4;
            // The total size of the extended info, including the header.
            final int extendedInfoTotalSize = extendedInfoSize + 4;

            final int extrasLen = ze.extra.length - extendedInfoTotalSize;
            byte[] extrasWithoutZip64 = new byte[extrasLen];

            System.arraycopy(ze.extra, 0, extrasWithoutZip64, 0, extendedInfoHeaderStart);
            System.arraycopy(ze.extra, extendedInfoHeaderStart + extendedInfoTotalSize,
                    extrasWithoutZip64, extendedInfoHeaderStart, (extrasLen - extendedInfoHeaderStart));

            ze.extra = extrasWithoutZip64;
            return true;
        }
    }

    /**
     * Appends a zip64 extended info record to the extras contained in {@code ze}. If {@code ze}
     * contains no extras, a new extras array is created.
     */
    public static void insertZip64ExtendedInfoToExtras(ZipEntry ze) throws ZipException {
        final byte[] output;
        // We add 4 to ZIP64_EXTENDED_INFO_MIN_SIZE to account for the 2 byte header and length.
        final int extendedInfoSize = ZIP64_EXTENDED_INFO_MIN_SIZE + 4;
        if (ze.extra == null) {
            output = new byte[extendedInfoSize];
        } else {
            // If the existing extras are already too big, we have no choice but to throw
            // an error.
            if (ze.extra.length + extendedInfoSize > 65535) {
                throw new ZipException("No space in extras for zip64 extended entry info");
            }

            // We copy existing extras over and put the zip64 extended info at the beginning. This
            // is to avoid breakages in the presence of "old style" extras which don't contain
            // headers and lengths. The spec is again silent about these inconsistencies.
            //
            // This means that people that for ZipOutputStream users, the value ZipEntry.getExtra
            // after an entry is written will be different from before. This shouldn't be an issue
            // in practice.
            output = new byte[ze.extra.length + ZIP64_EXTENDED_INFO_MIN_SIZE + 4];
            System.arraycopy(ze.extra, 0, output,  ZIP64_EXTENDED_INFO_MIN_SIZE + 4, ze.extra.length);
        }

        ByteBuffer bb = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(ZIP64_EXTENDED_INFO_HEADER_ID);
        bb.putShort((short) ZIP64_EXTENDED_INFO_MIN_SIZE);

        if (ze.getMethod() == ZipEntry.STORED) {
            bb.putLong(ze.size);
            bb.putLong(ze.compressedSize);
        } else {
            // Store these fields in the data descriptor instead.
            bb.putLong(0); // size.
            bb.putLong(0); // compressed size.
        }

        // The offset is only relevant in the central directory entry, but we write it out here
        // anyway, since we know what it is.
        bb.putLong(ze.localHeaderRelOffset);
        bb.putInt(0);  //  disk number

        ze.extra = output;
    }

    /**
     * Returns the size of the extended info record if {@code extras} contains a zip64 extended info
     * record, {@code -1} otherwise. The buffer will be positioned at the start of the extended info
     * record.
     */
    private static int getZip64ExtendedInfoSize(ByteBuffer extras) {
        try {
            while (extras.hasRemaining()) {
                final int headerId = extras.getShort() & 0xffff;
                final int length = extras.getShort() & 0xffff;
                if (headerId == ZIP64_EXTENDED_INFO_HEADER_ID) {
                    if (extras.remaining() >= length) {
                        return length;
                    } else {
                        return -1;
                    }
                } else {
                    extras.position(extras.position() + length);
                }
            }

            return -1;
        } catch (BufferUnderflowException bue) {
            // We'll underflow if we have an incomplete header in our extras.
            return -1;
        } catch (IllegalArgumentException iae) {
            // ByteBuffer.position() will throw if we have a truncated extra or
            // an invalid length in the header.
            return -1;
        }
    }

    /**
     * Copy the size, compressed size and local header offset fields from {@code ze} to
     * inside {@code ze}'s extended info record. This is additional step is necessary when
     * we could calculate the correct sizes only after writing out the entry. In this case,
     * the local file header would not contain real sizes, and they would be present in the
     * data descriptor and the central directory only.
     */
    public static void refreshZip64ExtendedInfo(ZipEntry ze) {
        if (ze.extra == null || ze.extra.length < ZIP64_EXTENDED_INFO_MIN_SIZE) {
            throw new IllegalStateException("Zip64 entry has no available extras: " + ze);
        }


        ByteBuffer buf = ByteBuffer.wrap(ze.extra).order(ByteOrder.LITTLE_ENDIAN);
        if (getZip64ExtendedInfoSize(buf) == -1) {
            throw new IllegalStateException(
                    "Zip64 entry extras has no zip64 extended info record: " + ze);
        }

        buf.putLong(ze.size);
        buf.putLong(ze.compressedSize);
        buf.putLong(ze.localHeaderRelOffset);
        buf.putInt(0); // disk number.
    }

    public static void writeZip64EocdRecordAndLocator(ByteArrayOutputStream baos,
            long numEntries, long offset, long cDirSize) throws IOException {
        // Step 1: Write out the zip64 EOCD record.
        writeLongAsUint32(baos, ZIP64_EOCD_RECORD_SIGNATURE);
        // The size of the zip64 eocd record. This is the effective size + the
        // size of the "version made by" (2 bytes) and the "version needed to extract" (2 bytes)
        // fields.
        writeLongAsUint64(baos, ZIP64_EOCD_RECORD_EFFECTIVE_SIZE + 4);
        // TODO: What values should we put here ? The pre-zip64 values we've chosen don't
        // seem to make much sense either.
        writeIntAsUint16(baos, 20);
        writeIntAsUint16(baos, 20);
        writeLongAsUint32(baos, 0L); // number of disk
        writeLongAsUint32(baos, 0L); // number of disk with start of central dir.
        writeLongAsUint64(baos, numEntries); // number of entries in this disk.
        writeLongAsUint64(baos, numEntries); // number of entries in total.
        writeLongAsUint64(baos, cDirSize); // size of the central directory.
        writeLongAsUint64(baos, offset); // offset of the central directory wrt. this file.

        // Step 2: Write out the zip64 EOCD record locator.
        writeLongAsUint32(baos, ZIP64_LOCATOR_SIGNATURE);
        writeLongAsUint32(baos, 0); // number of disk with start of central dir.
        writeLongAsUint64(baos, offset + cDirSize); // offset of the eocd record wrt. this file.
        writeLongAsUint32(baos, 1); // total number of disks.
    }
}
