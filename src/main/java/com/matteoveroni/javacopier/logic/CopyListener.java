package com.matteoveroni.javacopier.logic;

import com.matteoveroni.javacopier.pojo.CopyStatus;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    void onCopyProgress(CopyStatus copyStatus);

    void onCopyCompleted(CopyStatus finalCopyStatus);
}
