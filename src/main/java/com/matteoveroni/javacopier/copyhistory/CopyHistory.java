package com.matteoveroni.javacopier.copyhistory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public class CopyHistory {

    private final List<CopyHistoryEvent> history = new ArrayList<>();
    private final List<Path> copiedFiles = new ArrayList<>();
    private final List<Path> copiesFailed = new ArrayList<>();
    private String lastCopyHistoryEventMessage;
    private int analyzedFiles = 0;

    public final void registerCopyFailEventInHistory(Path srcPath, Path destPath, IOException ex) {
        lastCopyHistoryEventMessage = "src: " + srcPath + " copy to dest: " + destPath + " failed, ex: " + ex.toString();
        registerHistoryEvent(
                new CopyHistoryEvent.Builder(srcPath, destPath)
                        .setFailed(ex)
                        .build()
        );
    }

    public final void registerCopySuccessEventInHistory(Path srcPath, Path destPath) {
        lastCopyHistoryEventMessage = "src: " + srcPath + " copy to dest: " + destPath + " successful";
        registerHistoryEvent(
                new CopyHistoryEvent.Builder(srcPath, destPath)
                        .setSuccessful()
                        .build()
        );
    }

    public List<CopyHistoryEvent> getHistory() {
        return history;
    }

    public String getLastCopyHistoryEventMessage() {
        return lastCopyHistoryEventMessage;
    }

    public List<Path> getCopiedFiles() {
        return copiedFiles;
    }

    public List<Path> getCopiesFailed() {
        return copiesFailed;
    }

    public int getAnalyzedFiles() {
        return analyzedFiles;
    }

    private final void registerHistoryEvent(CopyHistoryEvent event) {
        history.add(event);
        Path src = event.getSrc();
        if (event.isCopySuccessful()) {
            copiedFiles.add(src);
        } else {
            copiesFailed.add(src);
        }
        analyzedFiles++;
    }

}
