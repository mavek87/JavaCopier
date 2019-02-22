package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.copyhistory.CopyHistoryEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public class CopyHistory {

    private final List<CopyHistoryEvent> history = new ArrayList<>();
    private final List<Path> filesCopied = new ArrayList<>();
    private final List<Path> filesSkipped = new ArrayList<>();
    private final List<Path> copyErrors = new ArrayList<>();

    public void addHistoryEvent(CopyHistoryEvent event) {
        history.add(event);
        Path src = event.getSrc();
        if(event.isSuccessful()) {
            filesCopied.add(src);
        } else {
            copyErrors.add(src);
        }
    }

    public List<CopyHistoryEvent> getHistory() {
        return history;
    }

    public List<Path> getFilesCopied() {
        return filesCopied;
    }

    public List<Path> getCopyErrors() {
        return copyErrors;
    }
    
}
