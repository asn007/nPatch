nPatch
======

Simple patching library for use with nLoader

The patch file is just a simple ZIP archive, which contains the following:

* patch.descriptor
* patch/
* new/
The first one is a patch descriptor, as you can get by the name. It contains the list of modified files alongside with their old hashes, so it can verify whether it can patch the file
It also contains an array of deleted files and patch notes.

The second one is where the patch is stored. The directory structure inside the patch resembles the original directory structure. The filenames are kept the same, except for they have additional extension appended.
Therefore, /mc/game/game.jar becomes /mc/game/game.jar.patch

The last one, the "new" directory contains new files that has been added to the folder in the patch.