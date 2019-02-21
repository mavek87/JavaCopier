package com.matteoveroni.javacopier.pojo;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public class CopyStatus {

    public enum State {
        COMPLETED, RUNNING, COMPLETED_WITH_ERRORS;
    }

    private final Path src;
    private final Path dest;
    private final CopyOption[] copyOptions;
    private final State copyState;
    private final double copyPercentage;
    private final int totalFileToCopy;
    private final List<Path> filesCopied;
    private final List<Path> copyErrors;

    public CopyStatus(Path src, Path dest, State copyState, int totalFileToCopy, List<Path> filesCopied, List<Path> copyErrors, CopyOption... copyOptions) {
        this.src = src;
        this.dest = dest;
        this.copyState = copyState;
        this.totalFileToCopy = totalFileToCopy;
        this.filesCopied = filesCopied;
        this.copyErrors = copyErrors;
        this.copyOptions = copyOptions;
        this.copyPercentage = calculateCopyPercentage();
    }

    private double calculateCopyPercentage() {
        int numberOfAnalyzedFiles = (filesCopied.size() + copyErrors.size());
        return ((double) (numberOfAnalyzedFiles) / totalFileToCopy) * 100;
    }

    public Path getSrc() {
        return src;
    }

    public Path getDest() {
        return dest;
    }

    public CopyOption[] getCopyOptions() {
        return copyOptions;
    }

    public State getCopyState() {
        return copyState;
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

    @Override
    public String toString() {
        return "CopyStatus{" +
                "src=" + src +
                ", dest=" + dest +
                ", copyOptions=" + Arrays.toString(copyOptions) +
                ", copyState=" + copyState +
                ", copyPercentage=" + copyPercentage +
                ", totalFileToCopy=" + totalFileToCopy +
                ", filesCopied=" + filesCopied +
                ", copyErrors=" + copyErrors +
                '}';
    }
}