package com.matteoveroni.javacopier;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    void onCopyProgress(CopyStatusReport copyStatusReport);

    void onCopyComplete(CopyStatusReport finalCopyStatusReport);
}
