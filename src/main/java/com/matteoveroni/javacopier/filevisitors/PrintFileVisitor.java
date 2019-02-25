package com.matteoveroni.javacopier.filevisitors;

import com.matteoveroni.javacopier.CopyListener;
import com.matteoveroni.javacopier.CopyStatusReport;
import com.matteoveroni.javacopier.copyhistory.CopyHistory;
import com.matteoveroni.javacopier.copyhistory.CopyHistoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class PrintFileVisitor implements FileVisitor<Path> {

    private final static Logger LOG = LoggerFactory.getLogger(PrintFileVisitor.class);
    private final Path rootSrc;
    private final Path rootDest;
    private final PrintWriter printWriter;
    private final int totalFiles;
    private final CopyHistory copyHistory;
    private final CopyListener copyListener;
    private final CopyOption[] copyOptions;

    public PrintFileVisitor(Path rootSrc, Path destSrc, OutputStream outputStream, int totalFiles, CopyHistory copyHistory, CopyListener copyListener, CopyOption[] copyOptions) {
        this.rootSrc = rootSrc;
        this.rootDest = destSrc;
        this.printWriter = new PrintWriter(outputStream, true);
        this.totalFiles = totalFiles;
        this.copyHistory = copyHistory;
        this.copyOptions = copyOptions;
        this.copyListener = copyListener;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path srcDir, BasicFileAttributes attrs) throws IOException {
        logPathToOutputStream(srcDir);
        copyHistory.registerCopySuccessEventInHistory(srcDir, rootDest);
        notifyCopyStatusProgressEventToListener();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) throws IOException {
        logPathToOutputStream(srcFile);
        copyHistory.registerCopySuccessEventInHistory(srcFile, rootDest);
        notifyCopyStatusProgressEventToListener();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcFile, IOException ex) throws IOException {
        if (ex != null) {
            LOG.error("Visit file failed exception: " + ex.toString());
            copyHistory.registerCopyFailEventInHistory(srcFile, rootDest, ex);
            notifyCopyStatusProgressEventToListener();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path srcDir, IOException ex) throws IOException {
        if (ex != null) {
            LOG.error("Post visit directory exception: " + ex.toString());
            copyHistory.registerCopyFailEventInHistory(srcDir, rootDest, ex);
            notifyCopyStatusProgressEventToListener();
        }
        return FileVisitResult.CONTINUE;
    }

    public void closeOutputStream() {
        printWriter.close();
    }

    private void logPathToOutputStream(Path path) {
        LOG.debug(path.toString());
        printWriter.println(path.toString());
    }

    private void notifyCopyStatusProgressEventToListener() {
        if (copyListener != null) {
            copyListener.onCopyProgress(
                    new CopyStatusReport(rootSrc, rootDest, CopyStatusReport.CopyState.RUNNING, totalFiles, copyHistory, copyOptions)
            );
        }
    }
}
