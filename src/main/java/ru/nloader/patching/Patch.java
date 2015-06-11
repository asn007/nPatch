/*
 * Copyright (C) 2015 asn007 aka Andrey Sinitsyn <andrey.sin98@gmail.com>
 *
 *  This file (Patch.java) is part of nPatch.
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
import ru.nloader.patching.io.CompressedFileNotFoundException;
import ru.nloader.patching.io.adapter.IOAdapter;

import java.io.*;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by asn007 on 11.06.2015.
 */
public class Patch {
    private final IOAdapter ioAdapter;
    private final File patchFile;
    private Date releaseDate;
    private PatchNote patchNote;



    public Patch(File patchFile, IOAdapter adapter) throws Exception {
        this.patchFile = patchFile;
        this.ioAdapter = adapter;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ioAdapter.getFile(Constants.PATCH_DESCRIPTOR_FILE).getData())))) {
            JsonObject patchDescriptorObject = JsonObject.readFrom(reader);
            JsonObject patchNoteObject = patchDescriptorObject.get("patchnote").asObject();
            patchNote = new PatchNote(patchNoteObject.getString("title", ""), patchNoteObject.getString("text", ""));
            releaseDate = Constants.DEFAULT_DATE_FORMAT.parse(patchDescriptorObject.getString("releaseDate", "01.07.12 12:00"));

        }
    }

    public IOAdapter getIOAdapter() { return this.ioAdapter; }

    public PatchNote getPatchNote() {
        return this.patchNote;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public File getPatchFile() {
        return patchFile;
    }

}
