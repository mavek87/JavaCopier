package com.matteoveroni.javacopier.copyhistory;

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
    private int analyzedFiles = 0;

    public final void addHistoryEvent(CopyHistoryEvent event) {
        history.add(event);
        Path src = event.getSrc();
        if(event.isCopySuccessful()) {
            copiedFiles.add(src);
        } else {
            copiesFailed.add(src);
        }
        analyzedFiles++;
    }
    
    public List<CopyHistoryEvent> getHistory() {
        return history;
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
    
}
