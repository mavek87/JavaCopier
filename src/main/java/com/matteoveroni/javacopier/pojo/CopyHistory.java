package com.matteoveroni.javacopier.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public class CopyHistory {
    
    private final List<CopyHistoryEvent> history = new ArrayList<>();
    
    public void addEvent(CopyHistoryEvent historyEvent) {
        history.add(historyEvent);
    }

    public List<CopyHistoryEvent> getHistory() {
        return history;
    }
}
