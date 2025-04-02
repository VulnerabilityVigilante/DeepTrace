package edu.utsa.cs3443.deepTrace.models;

import java.io.File;
import java.util.List;

public class FileRemover {

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