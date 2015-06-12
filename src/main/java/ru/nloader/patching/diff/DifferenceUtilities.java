/*
 * Copyright (C) 2015 asn007 aka Andrey Sinitsyn <andrey.sin98@gmail.com>
 *
 *  This file (Patcher.java) is part of nPatch.
 *
 *      nPatch is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      nPatch is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with nPatch.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.nloader.patching.diff;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

/**
 * Created by asn007 on 09.08.14.
 */
public class DifferenceUtilities {

    public static final String MAGIC = "nldrdiff";

    /* This is a java binary patcher based on bsdiff from Colin Percival and JBDiff from Joe Desbonnet */

    public static byte[] patch(byte[] oldByteArray, byte[] patch) throws IOException {
        int oldpos = 0, newpos = 0;
        try(DataInputStream dis = new DataInputStream(new ByteArrayInputStream(patch))) {
            byte[] magicStringArr = new byte[8]; // magic "nldrdiff"
            dis.read(magicStringArr);
            if(!new String(magicStringArr, "US-ASCII").equals(MAGIC)) throw new IOException("Cannot apply patch as magic is not 'nldrdiff'");
            long controlBlockLength = dis.readLong(); // control block length after gzip compression, offset 8, len 8 bytes
            long diffBlockLength = dis.readLong(); // diff block length after gzip compression, offset 16, len 8 bytes
            int newFileSize = (int)dis.readLong(); // new file size, offset 16, len 8 bytes

            int[] control = new int[3];

            byte[] patchedByteArray = new byte[newFileSize + 1];
            while(newpos < newFileSize) {
                for (int i = 0; i <= 2; i++) {
                    control[i] = dis.readInt();
                }

                if(newpos + control[0] > newFileSize)
                    throw new IOException("Corrupt patch file!");
                // TODO: read from diff block stream

                for (int i = 0; i < control[0]; i++) {
                    if ((oldpos + i >= 0) && (oldpos + i < oldByteArray.length))
                        patchedByteArray[newpos + i] += oldByteArray[oldpos + i];
                }
                newpos += control[0];
                oldpos += control[0];

                if(newpos + control[1] > newFileSize)
                    throw new IOException("Corrupt patch");

                // TODO: read from extra block stream

                newpos += control[1];
                oldpos += control[2];
            }
            return patchedByteArray;
        }
    }

    private static  int min(int x, int y) {
        return x < y ? x : y;
    }

    private static void split(int[] I, int[] V, int start, int len, int h) {

        int i, j, k, x, tmp, jj, kk;

        if (len < 16) {
            for (k = start; k < start + len; k += j) {
                j = 1;
                x = V[I[k] + h];
                for (i = 1; k + i < start + len; i++) {
                    if (V[I[k + i] + h] < x) {
                        x = V[I[k + i] + h];
                        j = 0;
                    }
                    if (V[I[k + i] + h] == x) {
                        tmp = I[k + j];
                        I[k + j] = I[k + i];
                        I[k + i] = tmp;
                        j++;
                    }
                }
                for (i = 0; i < j; i++)
                    V[I[k + i]] = k + j - 1;
                if (j == 1)
                    I[k] = -1;
            }
            return;
        }

        x = V[I[start + len / 2] + h];
        jj = 0;
        kk = 0;
        for (i = start; i < start + len; i++) {
            if (V[I[i] + h] < x)
                jj++;
            if (V[I[i] + h] == x)
                kk++;
        }

        jj += start;
        kk += jj;

        i = start;
        j = 0;
        k = 0;
        while (i < jj) {
            if (V[I[i] + h] < x)
                i++;
            else if (V[I[i] + h] == x) {
                tmp = I[i];
                I[i] = I[jj + j];
                I[jj + j] = tmp;
                j++;
            } else {
                tmp = I[i];
                I[i] = I[kk + k];
                I[kk + k] = tmp;
                k++;
            }
        }

        while (jj + j < kk) {
            if (V[I[jj + j] + h] == x)
                j++;
            else {
                tmp = I[jj + j];
                I[jj + j] = I[kk + k];
                I[kk + k] = tmp;
                k++;
            }
        }

        if (jj > start)
            split(I, V, start, jj - start, h);
        for (i = 0; i < kk - jj; i++)
            V[I[jj + i]] = kk - 1;
        if (jj == kk - 1)
            I[jj] = -1;
        if (start + len > kk)
            split(I, V, kk, start + len - kk, h);
    }

    private static int matchlen(byte[] oldBuf, int oldOffset, byte[] newBuf, int newOffset) {
        int end = min(oldBuf.length - oldOffset, newBuf.length - newOffset);
        int i;
        for (i = 0; i < end; i++) {
            if (oldBuf[oldOffset+i] != newBuf[newOffset+i]) {
                break;
            }
        }
        return i;
    }


    private static int search(int[] I, byte[] oldBuf, byte[] newBuf,
                                    int newBufOffset, int start, int end, IntByRef pos) {
        int x, y;

        if (end - start < 2) {
            x = matchlen(oldBuf, I[start], newBuf, newBufOffset);
            y = matchlen(oldBuf, I[end], newBuf, newBufOffset);

            if (x > y) {
                pos.value = I[start];
                return x;
            } else {
                pos.value = I[end];
                return y;
            }
        }


        x = start + (end - start) / 2;
        if (memcmp(oldBuf, I[x], newBuf, newBufOffset) < 0) {
            return search(I, oldBuf, newBuf, newBufOffset, x, end, pos);
        } else {
            return search(I, oldBuf, newBuf, newBufOffset, start, x, pos);
        }

    }

    private static int memcmp(byte[] s1, int s1offset, byte[] s2, int s2offset) {

        int n = s1.length - s1offset;

        if (n > (s2.length-s2offset)) {
            n = s2.length-s2offset;
        }
        for (int i = 0; i < n; i++) {
            if (s1[i + s1offset] != s2[i + s2offset]) {
                return s1[i + s1offset] < s2[i + s2offset] ? -1 : 1;
            }
        }

        return 0;
    }

    private static void qsufsort(int[] I, int[] V, byte[] oldBuf) {

        int oldsize = oldBuf.length;

        int[] buckets = new int[256];
        int i, h, len;

        for (i = 0; i < 256; i++)
            buckets[i] = 0;

        for (i = 0; i < oldsize; i++)
            buckets[(int)oldBuf[i] &0xff]++;

        for (i = 1; i < 256; i++)
            buckets[i] += buckets[i - 1];

        for (i = 255; i > 0; i--)
            buckets[i] = buckets[i - 1];

        buckets[0] = 0;

        for (i = 0; i < oldsize; i++)
            I[++buckets[(int)oldBuf[i] & 0xff]] = i;

        I[0] = oldsize;
        for (i = 0; i < oldsize; i++)
            V[i] = buckets[(int)oldBuf[i] &0xff];

        V[oldsize] = 0;
        for (i = 1; i < 256; i++) {
            if (buckets[i] == buckets[i - 1] + 1) {
                I[buckets[i]] = -1;
            }
        }
        I[0] = -1;
        for (h = 1; I[0] != -(oldsize + 1); h += h) {
            len = 0;
            for (i = 0; i < oldsize + 1;) {
                if (I[i] < 0) {
                    len -= I[i];
                    i -= I[i];
                } else {
                    //if(len) I[i-len]=-len;
                    if (len != 0)
                        I[i - len] = -len;
                    len = V[I[i]] + 1 - i;
                    split(I, V, i, len, h);
                    i += len;
                    len = 0;
                }
            }
            if (len != 0)
                I[i - len] = -len;
        }

        for (i = 0; i < oldsize + 1; i++)
            I[V[i]] = i;
    }

    private static class IntByRef {
        public int value;
    }

    public static void diff(File oldFile, File newFile, File diffFile) {

    }

    public static byte[] diff(byte[] oldArray, byte[] newArray) {
        int oldsize = oldArray.length;
        int[] I = new int[oldsize+1];
        int[] V = new int[oldsize+1];

        qsufsort(I, V, oldArray);
        V = null;

        int newsize = newArray.length;

        // diffblock
        int dblen = 0;
        byte[] db = new byte[newsize];

        // extra block
        int eblen = 0;
        byte[] eb = new byte[newsize];

		/*
		 * Diff file is composed as follows:
		 *
		 * Header (32 bytes)
		 * Data (from offset 32 to end of file)
		 *
		 * Header:
		 * Offset 0, length 8 bytes: file magic "nldrdiff"
		 * Offset 8, length 8 bytes: length of ctrl block
		 * Offset 16, length 8 bytes: length of compressed diff block
		 * Offset 24, length 8 bytes: length of new file
		 *
		 * Data:
		 * 32  (length ctrlBlockLen): ctrlBlock
		 * 32+ctrlBlockLen (length diffBlockLen): diffBlock (gziped)
		 * 32+ctrlBlockLen+diffBlockLen (to end of file): extraBlock (gziped)
		 *
		 * ctrlBlock comprises a set of records, each record 12 bytes. A record
		 * comprises 3 x 32 bit integers. The ctrlBlock is not compressed.
		 */

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream diffStream = new DataOutputStream(baos)) {

            int oldscore, scsc;

            int overlap, Ss, lens;
            int i;
            int scan = 0;
            int len = 0;
            int lastscan = 0;
            int lastpos = 0;
            int lastoffset = 0;

            IntByRef pos = new IntByRef();
            int ctrlBlockLen = 0;

            while (scan < newsize) {

                oldscore = 0;

                for (scsc = scan += len; scan < newsize; scan++) {
                    len = search(I, oldArray, newArray, scan, 0, oldsize, pos);
                    for (; scsc < scan + len; scsc++) {
                        if ((scsc + lastoffset < oldsize) && (oldArray[scsc + lastoffset] == newArray[scsc]))
                            oldscore++;
                    }

                    if (((len == oldscore) && (len != 0)) || (len > oldscore + 8))  {
                        break;
                    }

                    if ((scan + lastoffset < oldsize) && (oldArray[scan + lastoffset] == newArray[scan]))
                        oldscore--;
                }


                if ((len != oldscore) || (scan == newsize)) {
                    int s = 0;
                    int Sf = 0;
                    int lenf = 0;
                    for (i = 0; (lastscan + i < scan) && (lastpos + i < oldsize);) {
                        if (oldArray[lastpos + i] == newArray[lastscan + i])
                            s++;
                        i++;
                        if (s * 2 - i > Sf * 2 - lenf) {
                            Sf = s;
                            lenf = i;
                        }
                    }

                    int lenb = 0;
                    if (scan < newsize) {
                        s = 0;
                        int Sb = 0;
                        for (i = 1; (scan >= lastscan + i) && (pos.value >= i); i++) {
                            if (oldArray[pos.value - i] == newArray[scan - i])
                                s++;
                            if (s * 2 - i > Sb * 2 - lenb) {
                                Sb = s;
                                lenb = i;
                            }
                        }
                    }

                    if (lastscan + lenf > scan - lenb) {
                        overlap = (lastscan + lenf) - (scan - lenb);
                        s = 0;
                        Ss = 0;
                        lens = 0;
                        for (i = 0; i < overlap; i++) {
                            if (newArray[lastscan + lenf - overlap + i] == oldArray[lastpos + lenf - overlap + i])
                                s++;
                            if (newArray[scan - lenb + i] == oldArray[pos.value - lenb + i])
                                s--;
                            if (s > Ss) {
                                Ss = s;
                                lens = i + 1;
                            }
                        }
                        lenf += lens - overlap;
                        lenb -= lens;
                    }


                    // ? byte casting introduced here -- might affect things
                    for (i = 0; i < lenf; i++)
                        db[dblen + i] = (byte) (newArray[lastscan + i] - oldArray[lastpos + i]);

                    for (i = 0; i < (scan - lenb) - (lastscan + lenf); i++)
                        eb[eblen + i] = newArray[lastscan + lenf + i];

                    dblen += lenf;
                    eblen += (scan - lenb) - (lastscan + lenf);


				/*
				 * Write control block entry (3 x int)
				 */
                    diffStream.writeInt(lenf);
                    diffStream.writeInt ( (scan - lenb) - (lastscan + lenf) );
                    diffStream.writeInt ( (pos.value - lenb) - (lastpos + lenf) );
                    ctrlBlockLen += 12;

                    lastscan = scan - lenb;
                    lastpos = pos.value - lenb;
                    lastoffset = pos.value - scan;
                }
            }
            GZIPOutputStream gzOut;
            gzOut = new GZIPOutputStream(diffStream);
            gzOut.write(db, 0, dblen);
            gzOut.finish();

            int diffBlockLen = diffStream.size() - ctrlBlockLen; // no need for -32 since we do not write header info here

            gzOut = new GZIPOutputStream(diffStream);
            gzOut.write(eb, 0, eblen);
            gzOut.finish();

            long extraBlockLen = diffStream.size() - diffBlockLen - ctrlBlockLen; // no need for -32 since we do not write header info here

            try(ByteArrayOutputStream resultBuffer =  new ByteArrayOutputStream();
                DataOutputStream disResult = new DataOutputStream(resultBuffer)) {
                disResult.write(MAGIC.getBytes("US-ASCII"));
                disResult.writeLong(ctrlBlockLen); // placeholder for control block length
                diffStream.writeLong(diffBlockLen); // placeholder for compressed diff length
                diffStream.writeLong(newsize);
                diffStream.write(baos.toByteArray());

                return resultBuffer.toByteArray();
            }


        } catch(IOException ioe) {

        }
        return null;
    }

    public static void patch(File oldFile, File newFile, byte[] patch) throws IOException {
        Files.write(newFile.toPath(), patch(Files.readAllBytes(oldFile.toPath()), patch));
    }

    public static void patch(File oldFile, File newFile, File patch) throws IOException {
        patch(oldFile, newFile, Files.readAllBytes(patch.toPath()));
    }


}
