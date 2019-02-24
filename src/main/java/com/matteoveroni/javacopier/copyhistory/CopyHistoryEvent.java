package com.matteoveroni.javacopier.copyhistory;

import java.nio.file.Path;

/**
 * @author Matteo Veroni
 */
public class CopyHistoryEvent {

    private static Long CURRENT_ID = 1L;

    private final Long id;
    private final Path src;
    private final Path dest;
    private final boolean successful;
    private final String exceptionMessage;

    private CopyHistoryEvent(Path src, Path dest, boolean successful, String exceptionMessage) {
        this.id = CURRENT_ID;
        this.src = src;
        this.dest = dest;
        this.successful = successful;
        this.exceptionMessage = exceptionMessage;
        CURRENT_ID++;
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

    public Path getDest() {
        return dest;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

}
