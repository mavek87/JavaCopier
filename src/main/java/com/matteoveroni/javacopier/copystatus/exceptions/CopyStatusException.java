package com.matteoveroni.javacopier.copystatus.exceptions;

import com.matteoveroni.javacopier.copystatus.CopyStatusReport;

public class CopyStatusException extends Exception {

    private CopyStatusReport copyStatus;

    public CopyStatusException(String message, CopyStatusReport copyStatus) {
        super(message);
        this.copyStatus = copyStatus;
    }

    public CopyStatusReport getCopyStatus() {
        return copyStatus;
    }
}
