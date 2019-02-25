package com.matteoveroni.javacopier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matteoveroni.javacopier.copyhistory.CopyHistory;
import com.matteoveroni.javacopier.gsonconverters.PathToGsonConverter;

import java.nio.file.CopyOption;
import java.nio.file.Path;

/**
 * @author Matteo Veroni
 */
public class CopyStatusReport {

    private transient final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Path.class, new PathToGsonConverter())
            .create();

    private transient final Gson prettygson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Path.class, new PathToGsonConverter())
            .setPrettyPrinting()
            .create();

    public enum CopyState {
        RUNNING, DONE;
    }

    public enum FinalResult {
        NOT_ELABORATED, COPY_SUCCESSFUL, COPY_FAILED, COPY_PARTIAL
    }

    private final Path src;
    private final Path dest;
    private final int totalFiles;
    private final int numberOfCopiedFiles;
    private final int numberOfCopiesFailed;
    private final CopyState copyState;
    private final double copyPercentage;
    private final FinalResult result;
    private final CopyOption[] copyOptions;
    private final CopyHistory copyHistory;

    public CopyStatusReport(Path src, Path dest, CopyState copyState, int totalFiles, CopyHistory copyHistory, CopyOption... copyOptions) {
        this.src = src;
        this.dest = dest;
        this.copyState = copyState;
        this.totalFiles = totalFiles;
        this.copyHistory = copyHistory;
        this.numberOfCopiedFiles = copyHistory.getCopiedFiles().size();
        this.numberOfCopiesFailed = copyHistory.getCopiesFailed().size();
        this.copyOptions = copyOptions;
        switch (copyState) {

            case DONE:
                copyPercentage = 100.0;
                if (copyHistory == null || copyHistory.getCopiesFailed() == null) {
                    result = FinalResult.COPY_FAILED;
                    break;
                }
                if (copyHistory.getCopiesFailed().size() >= totalFiles) {
                    result = FinalResult.COPY_FAILED;
                } else if (copyHistory.getCopiesFailed().isEmpty()) {
                    result = FinalResult.COPY_SUCCESSFUL;
                } else {
                    result = FinalResult.COPY_PARTIAL;
                }
                break;

            case RUNNING:
            default:
                copyPercentage = calculateCopyPercentage();
                result = FinalResult.NOT_ELABORATED;
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

    public int getNumberOfCopiedFiles() {
        return numberOfCopiedFiles;
    }

    public int getNumberOfCopiesFailed() {
        return numberOfCopiesFailed;
    }

    public CopyHistory getCopyHistory() {
        return copyHistory;
    }

    public CopyState getCopyState() {
        return copyState;
    }

    public FinalResult getFinalResult() {
        return result;
    }

    public CopyOption[] getCopyOptions() {
        return copyOptions;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public String toPrettyString() {
        return prettygson.toJson(this);
    }

    private double calculateCopyPercentage() {
        if (copyHistory == null) {
            return 0.0;
        } else {
            return ((double) (copyHistory.getAnalyzedFiles()) / totalFiles) * 100;
        }
    }
}
