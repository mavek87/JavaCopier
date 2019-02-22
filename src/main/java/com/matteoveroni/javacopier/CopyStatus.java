package com.matteoveroni.javacopier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matteoveroni.javacopier.CopyHistory;
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

    public enum CopyState {
        RUNNING, DONE;
    }

    public enum CopyResult {
        SUCCESFULL, FAILED, PARTIAL
    }

    private final Path src;
    private final Path dest;
    private final int totalFiles;
    private final List<Path> filesCopied;
    private final List<Path> copyErrors;
    private final CopyHistory copyHistory;
    private double copyPercentage = 0.0;
    private CopyResult copyResult = null;
    private CopyState copyState = null;
    private final CopyOption[] copyOptions;

    public CopyStatus(Path src, Path dest, CopyState copyState, int totalFiles, List<Path> filesCopied, List<Path> copyErrors, CopyHistory copyHistory, CopyOption... copyOptions) {
        this.src = src;
        this.dest = dest;
        this.copyState = copyState;
        this.totalFiles = totalFiles;
        this.filesCopied = filesCopied;
        this.copyErrors = copyErrors;
        this.copyHistory = copyHistory;
        this.copyOptions = copyOptions;
        switch (copyState) {
            case DONE:
                this.copyPercentage = 100;
                if(copyHistory.getCopyErrors() != null && copyHistory.getCopyErrors().isEmpty()) {
                    copyResult = CopyResult.SUCCESFULL;
                } else {
                    copyResult = CopyResult.FAILED;
                }
                break;
            case RUNNING:
            default:
                this.copyPercentage = calculateCopyPercentage();
                break;
        }
    }

    public Path getSrc() {
        return src;
    }

    public Path getDest() {
        return dest;
    }

    public double getCopyPercentage() {
        return copyPercentage;
    }

    public String getCopyPercentageInString() {
        return String.format("%.0f", copyPercentage) + "%";
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public List<Path> getFilesCopied() {
        return filesCopied;
    }

    public List<Path> getCopyErrors() {
        return copyErrors;
    }

    public CopyHistory getCopyHistory() {
        return copyHistory;
    }

    public CopyState getCopyState() {
        return copyState;
    }

    public CopyResult getCopyResult() {
        return copyResult;
    }

    public CopyOption[] getCopyOptions() {
        return copyOptions;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    private double calculateCopyPercentage() {
        int numberOfAnalyzedFiles = copyHistory.getHistory().size();
        return ((double) (numberOfAnalyzedFiles) / totalFiles) * 100;
    }
}
