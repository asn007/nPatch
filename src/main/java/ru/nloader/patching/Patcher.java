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

package ru.nloader.patching;

import com.eclipsesource.json.JsonObject;
import ru.nloader.patching.exception.CorruptPatchException;
import ru.nloader.patching.exception.PatchApplyException;
import ru.nloader.patching.io.CompressedFileNotFoundException;
import ru.nloader.patching.io.adapter.IOAdapter;

import java.io.*;

/**
 * Created by asn007 on 11.06.2015.
 */
public class Patcher {

    private final Patch patch;
    private final File patchDirectory;
    private final IOAdapter ioAdapter;

    public Patcher(Patch patch, File patchDirectory) throws CorruptPatchException, IOException {
        this.patch = patch;
        this.patchDirectory = patchDirectory;
        this.ioAdapter = patch.getIOAdapter();
    }

    public boolean isPatchable() {
        return false;
    }

    public void patch() throws PatchApplyException {

    }

    public Patch getPatch() {
        return patch;
    }

    public File getPatchDirectory() {
        return patchDirectory;
    }

}
