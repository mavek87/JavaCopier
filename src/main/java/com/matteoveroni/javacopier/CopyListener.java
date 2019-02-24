package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.copystatus.CopyStatusReport;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    void onCopyProgress(CopyStatusReport copyStatus);

    void onCopyCompleted(CopyStatusReport finalCopyStatus);
}
