package com.matteoveroni.javacopier;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    void onCopyProgress(CopyStatusReport copyStatus);

    void onCopyCompleted(CopyStatusReport finalCopyStatus);
}
