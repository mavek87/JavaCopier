package com.matteoveroni.javacopier.copyhistory;

import java.nio.file.Path;

/**
 * @author Matteo Veroni
 */
public class CopyHistoryEvent {

    private static Long CURRENT_ID = 1L;

    private Long id;
    private Path src;
    private Path dest;
    private boolean successful;
    private String exceptionMessage = "";

    private CopyHistoryEvent(Path src, Path dest, boolean successful, String exceptionMessage) {
        this.id = CURRENT_ID;
        CURRENT_ID++;
        this.src = src;
        this.dest = dest;
        this.successful = successful;
        this.exceptionMessage = exceptionMessage;
    }

    public static class Builder {

        private final Path src;
        private final Path dest;
        private boolean successful;
        private String exceptionMessage = "";

        public Builder(Path src, Path dest) {
            this.src = src;
            this.dest = dest;
        }

        public Builder setSuccessful() {
            this.successful = true;
            return this;
        }

        public Builder setFailed(Exception ex) {
            this.successful = false;
            this.exceptionMessage = ex.getMessage();
            return this;
        }

        public CopyHistoryEvent build() {
            return new CopyHistoryEvent(src, dest, successful, exceptionMessage);
        }
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
