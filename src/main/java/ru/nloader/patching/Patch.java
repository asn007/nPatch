package ru.nloader.patching;

import java.io.File;
import java.util.Date;

/**
 * Created by asn007 on 11.06.2015.
 */
public class Patch {
    private File patchFile;
    private Date releaseDate;

    public Patch(File patchFile) {
        this.patchFile = patchFile;
    }

}
