package com.matteoveroni.javacopier.copyhistory;

import java.nio.file.Path;

/**
 * @author Matteo Veroni
 */
public class CopyHistoryEvent {

    private static Long NEXT_ID_GLOBAL = 1L;

    private Long id;
    private Path src;
    private Path dest;
    private boolean successful;
    private String exceptionMessage = "";

    public CopyHistoryEvent(Path src, Path dest) {
        this.src = src;
        this.dest = dest;
        id = NEXT_ID_GLOBAL;
        NEXT_ID_GLOBAL++;
    }

    public CopyHistoryEvent(Path src, Path dest, boolean successful, Exception ex) {
        this(src, dest);
        this.successful = successful;
        this.exceptionMessage = ex.toString();
    }

    public Long getId() {
        return id;
    }

    public Path getSrc() {
        return src;
    }

    public void setSrc(Path src) {
        this.src = src;
    }

    public Path getDest() {
        return dest;
    }

    public void setDest(Path dest) {
        this.dest = dest;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccesfull(boolean successful) {
        this.successful = successful;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setException(Exception exception) {
        this.exceptionMessage = exception.toString();
    }
}
