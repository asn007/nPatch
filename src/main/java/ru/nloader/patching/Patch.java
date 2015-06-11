/*
 * Copyright (C) 2015 asn007 aka Andrey Sinitsyn <andrey.sin98@gmail.com>
 *
 *  This file (Patch.java) is part of nPatch.
 *
 *      ${PROJECT_NAME} is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      ${PROJECT_NAME} is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with ${PROJECT_NAME}.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.nloader.patching;

import java.io.File;
import java.util.Date;

/**
 * Created by asn007 on 11.06.2015.
 */
public class Patch {
    private File patchFile;
    private Date releaseDate;
    private PatchNote patchNote;
    public Patch(File patchFile) {
        this.patchFile = patchFile;
    }

}
