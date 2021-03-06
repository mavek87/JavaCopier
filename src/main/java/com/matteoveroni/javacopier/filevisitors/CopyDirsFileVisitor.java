package com.matteoveroni.javacopier.filevisitors;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import com.matteoveroni.javacopier.CopyListener;
import com.matteoveroni.javacopier.copyhistory.CopyHistoryEvent;
import com.matteoveroni.javacopier.CopyStatusReport;
import com.matteoveroni.javacopier.copyhistory.CopyHistory;

import java.nio.file.FileAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matteo Veroni
 */
public class CopyDirsFileVisitor implements FileVisitor<Path> {

    private final static Logger LOG = LoggerFactory.getLogger(CopyDirsFileVisitor.class);
    private final Path rootSrc;
    private final Path rootDest;
    private final int totalFiles;
    private final CopyHistory copyHistory;
    private final CopyListener copyListener;
    private final CopyOption[] copyOptions;

    public CopyDirsFileVisitor(Path rootSrc, Path destSrc, int totalFiles, CopyHistory copyHistory, CopyListener copyListener, CopyOption[] copyOptions) {
        this.rootSrc = rootSrc;
        this.rootDest = destSrc;
        this.totalFiles = totalFiles;
        this.copyHistory = copyHistory;
        this.copyOptions = copyOptions;
        this.copyListener = copyListener;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path srcDir, BasicFileAttributes attrs) {
        LOG.debug("+++ | pre visit srcDir: " + srcDir);
        Path destDir = calculateDestPath(srcDir);
        try {
            Files.createDirectory(destDir);
            LOG.debug("destDir: " + destDir + " created");
            copyHistory.registerCopySuccessEventInHistory(srcDir, destDir);
            notifyCopyStatusProgressEventToListener();
            return FileVisitResult.CONTINUE;
        } catch (FileAlreadyExistsException ex) {
            LOG.warn("Unable to create destDir: " + destDir + ", ex: " + ex.toString());
            copyHistory.registerCopySuccessEventInHistory(srcDir, destDir);
            notifyCopyStatusProgressEventToListener();
            return FileVisitResult.CONTINUE;
        } catch (IOException ioe) {
            LOG.error("Unable to create destDir: " + destDir + ", ex: " + ioe.toString());
            copyHistory.registerCopyFailEventInHistory(srcDir, destDir, ioe);
            notifyCopyStatusProgressEventToListener();
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) {
        LOG.debug("*** | visit srcFile: " + srcFile);
        Path destFile = calculateDestPath(srcFile);
        try {
            Files.copy(srcFile, destFile, copyOptions);
            LOG.debug("srcFile " + srcFile + " visited and copied to destFile: " + destFile);
            copyHistory.registerCopySuccessEventInHistory(srcFile, destFile);
        } catch (IOException ioe) {
            LOG.error("Unable to copy: " + srcFile + ", ex: " + ioe.toString());
            copyHistory.registerCopyFailEventInHistory(srcFile, destFile, ioe);
        }
        notifyCopyStatusProgressEventToListener();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcPath, IOException ex) {
        LOG.debug("xxx | visit srcFile: " + srcPath + " failed");
        Path destPath = calculateDestPath(srcPath);
        if (ex instanceof FileSystemLoopException) {
            LOG.warn("Cycle detected: " + srcPath);
        } else {
            LOG.warn("Unable to access: " + srcPath + ", ex: " + ex.toString());
        }
        copyHistory.registerCopyFailEventInHistory(srcPath, destPath, ex);
        notifyCopyStatusProgressEventToListener();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path srcDir, IOException exc) {
        LOG.debug("--- | post visit srcDir: " + srcDir);
        if (exc == null && containsCopyOption(StandardCopyOption.COPY_ATTRIBUTES)) {
            copyAllAttributesFromSrcToDestDirIfNeeded(srcDir);
        }
        return FileVisitResult.CONTINUE;
    }

    private void notifyCopyStatusProgressEventToListener() {
        if (copyListener != null) {
            copyListener.onCopyProgress(
                    new CopyStatusReport(rootSrc, rootDest, CopyStatusReport.CopyState.RUNNING, totalFiles, copyHistory, copyOptions)
            );
        }
    }

    private void copyAllAttributesFromSrcToDestDirIfNeeded(Path srcDir) {
        Path destDir = calculateDestPath(srcDir);
        try {
            FileTime time = Files.getLastModifiedTime(srcDir);
            Files.setLastModifiedTime(destDir, time);
            LOG.debug("Dest dir " + destDir + " attributes copied from srcDir " + srcDir);
        } catch (IOException ex) {
            LOG.warn("Unable to copy all attributes to: " + destDir + ", ex: " + ex.toString());
        }
    }

    private Path calculateDestPath(Path srcPath) {
        return rootDest.resolve(rootSrc.relativize(srcPath));
    }

    private boolean containsCopyOption(StandardCopyOption searchedCopyOption) {
        for (CopyOption copyOption : copyOptions) {
            if (copyOption.equals(searchedCopyOption)) {
                return true;
            }
        }
        return false;
    }

}
