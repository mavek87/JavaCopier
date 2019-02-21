package com.matteoveroni.javacopier.pojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matteoveroni.javacopier.gsonconverters.PathToGsonConverter;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public class CopyStatus {

    private transient final Gson gson = new GsonBuilder()
        //            .registerTypeAdapter(Path.class, new PathToGsonConverter())
        .registerTypeHierarchyAdapter(Path.class, new PathToGsonConverter())
        .create();

    public enum State {
        COMPLETED, RUNNING, COMPLETED_WITH_ERRORS;
    }

    private final Path src;
    private final Path dest;
    private final CopyOption[] copyOptions;
    private final double copyPercentage;
    private final int totalFileToCopy;
    private final List<Path> filesCopied;
    private final List<Path> copyErrors;
    private final List<Path> copyHistory;
    private final State copyState;

    public CopyStatus(Path src, Path dest, State copyState, int totalFileToCopy, List<Path> filesCopied, List<Path> copyErrors, List<Path> copyHistory, CopyOption... copyOptions) {
        this.src = src;
        this.dest = dest;
        this.copyState = copyState;
        this.totalFileToCopy = totalFileToCopy;
        this.filesCopied = filesCopied;
        this.copyErrors = copyErrors;
        this.copyHistory = copyHistory;
        this.copyOptions = copyOptions;
        switch (copyState) {
            case COMPLETED:
            case COMPLETED_WITH_ERRORS:
                this.copyPercentage = 100;
                break;
            case RUNNING:
            default:
                this.copyPercentage = calculateCopyPercentage();
                break;
        }
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

    public List<Path> getCopyHistory() {
        return copyHistory;
    }

    public CopyOption[] getCopyOptions() {
        return copyOptions;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
