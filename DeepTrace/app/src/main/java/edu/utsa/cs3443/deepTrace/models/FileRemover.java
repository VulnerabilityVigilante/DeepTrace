package edu.utsa.cs3443.deepTrace.models;

import java.io.File;
import java.util.List;

/**
 * Deletes files from storage.
 */
public class FileRemover {

    /**
     * Attempts to delete all files in the provided list.
     * @param files A list of {@link File} objects to be deleted.
     * @return {@code true} if all files were successfully deleted;
     * {@code false} if at least one file failed to delete.
     */
    public boolean removeFiles(List<File> files) {
        boolean allDeleted = true;
        for (File file : files) {
            if (!file.delete()) {
                allDeleted = false;
            }
        }
        return allDeleted;
    }
}