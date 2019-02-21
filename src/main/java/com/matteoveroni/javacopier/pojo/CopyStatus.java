package com.matteoveroni.javacopier.pojo;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public class CopyStatus {

    private final double copyPercentage;
    private final int totalFileToCopy;
    private final List<Path> filesCopied;
    private final List<Path> copyErrors;

    public CopyStatus(int totalFileToCopy, List<Path> filesCopied, List<Path> copyErrors) {
        this.totalFileToCopy = totalFileToCopy;
        this.filesCopied = filesCopied;
        this.copyErrors = copyErrors;
        this.copyPercentage = calculateCopyPercentage();
    }

    private double calculateCopyPercentage() {
        int numberOfAnalyzedFiles = (filesCopied.size() + copyErrors.size());
        return ((double) (numberOfAnalyzedFiles) / totalFileToCopy) * 100;
    }

    public double getCopyPercentage() {
        return copyPercentage;
    }

    public String getCopyPercentageInString() {
        return String.format("%.0f", copyPercentage) + "%";
    }

    public int getTotalFileToCopy() {
        return totalFileToCopy;
    }

    public List<Path> getFilesCopied() {
        return filesCopied;
    }

    public List<Path> getCopyErrors() {
        return copyErrors;
    }
}
