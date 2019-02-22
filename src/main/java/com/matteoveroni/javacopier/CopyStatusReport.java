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
        //            .registerTypeAdapter(Path.class, new PathToGsonConverter())
        .registerTypeHierarchyAdapter(Path.class, new PathToGsonConverter())
        .create();

    public enum CopyState {
        READY, RUNNING, DONE;
    }

    public enum FinalResult {
        NOT_ELABORATED, COPY_SUCCESFULL, COPY_FAILED, COPY_PARTIAL
    }

    private final Path src;
    private final Path dest;
    private final int totalFiles;
    private final CopyState copyState;
    private final double copyPercentage;
    private final CopyHistory copyHistory;
    private final FinalResult result;
    private final CopyOption[] copyOptions;

    public CopyStatusReport(Path src, Path dest, CopyState copyState, int totalFiles, CopyHistory copyHistory, CopyOption... copyOptions) {
        this.src = src;
        this.dest = dest;
        this.copyState = copyState;
        this.totalFiles = totalFiles;
        this.copyHistory = copyHistory;
        this.copyOptions = copyOptions;
        switch (copyState) {

            case READY:
                copyPercentage = 0.0;
                result = FinalResult.NOT_ELABORATED;
                break;

            case DONE:
                copyPercentage = 100.0;
                if (copyHistory == null || copyHistory.getCopyErrors() == null) {
                    result = FinalResult.COPY_FAILED;
                    break;
                }
                if (copyHistory.getCopyErrors().size() >= totalFiles) {
                    result = FinalResult.COPY_FAILED;
                } else if (copyHistory.getCopyErrors().isEmpty()) {
                    result = FinalResult.COPY_SUCCESFULL;
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

    private double calculateCopyPercentage() {
        if (copyHistory == null) {
            return 0.0;
        } else {
            return ((double) (copyHistory.getAnalyzedFiles()) / totalFiles) * 100;
        }
    }
}
