package com.matteoveroni.javacopier.pojo;

import java.nio.file.Path;
import java.util.List;

@Deprecated
// TODO: think about the right return pojo for the copy
public class CopyReport {

    private final Path src;
    private final Path dest;
    private final List<Path> copiedFile;
    private final List<Path> copyErrors;
    private final int totalSrcFiles;
    private final int totalDestFiles;

    public CopyReport(Path src, Path dest, List<Path> copiedFile, List<Path> copyErrors, int totalSrcFiles, int totalDestFiles) {
        this.src = src;
        this.dest = dest;
        this.copiedFile = copiedFile;
        this.copyErrors = copyErrors;
        this.totalSrcFiles = totalSrcFiles;
        this.totalDestFiles = totalDestFiles;
    }

    public Path getSrc() {
        return src;
    }

    public Path getDest() {
        return dest;
    }

    public List<Path> getCopiedFile() {
        return copiedFile;
    }

    public List<Path> getCopyErrors() {
        return copyErrors;
    }

    public int getTotalSrcFiles() {
        return totalSrcFiles;
    }

    public int getTotalDestFiles() {
        return totalDestFiles;
    }
}
