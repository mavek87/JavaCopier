package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.pojo.CopyStatus;

/**
 * @author Matteo Veroni
 */
public interface CopyListener {
    public void onCopyProgress(CopyStatus copyStatus);
}
