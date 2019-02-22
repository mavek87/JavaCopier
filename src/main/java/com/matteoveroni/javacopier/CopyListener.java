package com.matteoveroni.javacopier;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    void onCopyProgress(CopyStatus copyStatus);

    void onCopyCompleted(CopyStatus finalCopyStatus);
}
