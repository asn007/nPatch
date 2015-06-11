/*
 * Copyright (C) 2015 asn007 aka Andrey Sinitsyn <andrey.sin98@gmail.com>
 *
 *  This file (IOAdapter.java) is part of nPatch.
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
import ru.nloader.patching.io.adapter.entry.CompressedEntry;
import ru.nloader.patching.io.CompressedFileNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by asn007 on 11.06.2015.
 */
public abstract class IOAdapter {
    protected final File patchFile;
    protected HashMap<String, CompressedEntry> patchContents;
    public IOAdapter(File patchFile) throws IOException {
        this.patchFile = patchFile;
    }

    public File getFile() {
        return patchFile;
    }

    public abstract CompressedEntry getFile(String identifier) throws CompressedFileNotFoundException;

    public abstract HashMap<String, ? extends CompressedEntry> getContents() throws CorruptPatchException;

}
