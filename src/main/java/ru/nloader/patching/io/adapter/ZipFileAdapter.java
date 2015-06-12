/*
 * Copyright (C) 2015 asn007 aka Andrey Sinitsyn <andrey.sin98@gmail.com>
 *
 *  This file (ZipFileAdapter.java) is part of nPatch.
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

package ru.nloader.patching.io.adapter;

import ru.nloader.patching.exception.CorruptPatchException;
import ru.nloader.patching.io.CompressedFileNotFoundException;
import ru.nloader.patching.io.StreamHelper;
import ru.nloader.patching.io.adapter.entry.CompressedEntry;


import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import ru.nloader.patching.io.adapter.entry.ZipEntry;

/**
 * Created by asn007 on 11.06.2015.
 */
public class ZipFileAdapter extends IOAdapter {

    public ZipFileAdapter(File patchFile) throws IOException {
        super(patchFile);
        this.patchContents = new HashMap<String, CompressedEntry>();
        ZipFile zf = new ZipFile(patchFile);
        int i = 0;
        for(Enumeration e = zf.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry)e.nextElement();
            String entryName = entry.getName();
            byte[] fileData;
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                StreamHelper.copy(zf.getInputStream(entry), baos);
                fileData = baos.toByteArray();
            }
            ru.nloader.patching.io.adapter.entry.ZipEntry futureOne = new ru.nloader.patching.io.adapter.entry.ZipEntry(entryName, fileData.length, fileData);
            patchContents.put(entryName, futureOne);

        }
//        try(FileInputStream fis = new FileInputStream(patchFile)) {
//
//        } catch (IOException e) {
//            throw e;
//        }
    }

    @Override
    public CompressedEntry getFile(String identifier) throws CompressedFileNotFoundException {
        return patchContents.get(identifier);
    }

    @Override
    public HashMap<String, ru.nloader.patching.io.adapter.entry.CompressedEntry> getContents() throws CorruptPatchException {
        return patchContents;
    }
}
