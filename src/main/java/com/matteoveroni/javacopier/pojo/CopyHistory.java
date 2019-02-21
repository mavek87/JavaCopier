package com.matteoveroni.javacopier.pojo;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class CopyHistory {

    private final Map<Path, Path> copyHistory = new LinkedHashMap<>();

    public void addCopyEvent(Path src, Path dest) {
        copyHistory.put(src, dest);
    }

    public Path getCopyEventBySrc(Path src) {
        return copyHistory.get(src);
    }
    
    public Map<Path, Path> getCopyHistory() {
        return copyHistory;
    }
}
