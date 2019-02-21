package com.matteoveroni.javacopier;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    public void onCopyProgress(int totalFilesToCopy, List<Path> filesCopied, List<Path> copyErrors);
}
