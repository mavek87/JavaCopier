package com.matteoveroni.javacopier.logic.filevisitors;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.matteoveroni.javacopier.logic.CopyListener;
import com.matteoveroni.javacopier.pojo.CopyHistory;
import com.matteoveroni.javacopier.pojo.CopyHistoryEvent;
import com.matteoveroni.javacopier.pojo.CopyStatus;

import java.nio.file.FileAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matteo Veroni
 */
public class CopyDirsFileVisitor implements FileVisitor<Path> {

    private final static Logger LOG = LoggerFactory.getLogger(CopyDirsFileVisitor.class);

    private final Path rootDest;
    private final Path rootSrc;
    private final int totalFiles;
    private final CopyHistory copyHistory = new CopyHistory();
    private final List<Path> filesCopied = new ArrayList<>();
    private final List<Path> copyErrors = new ArrayList<>();
    private final Optional<CopyListener> copyListener;
    private final CopyOption[] copyOptions;

    public CopyDirsFileVisitor(Path rootSrc, Path destSrc, int totalFiles, Optional<CopyListener> copyListener, CopyOption[] copyOptions) {
        this.rootSrc = rootSrc;
        this.rootDest = destSrc;
        this.totalFiles = totalFiles;
        this.copyOptions = copyOptions;
        this.copyListener = copyListener;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path srcDir, BasicFileAttributes attrs) {
        LOG.debug("+++ | pre visit srcDir: " + srcDir);
        final Path destDir = calculateDestPath(srcDir);
        try {
            Files.createDirectory(destDir);
            LOG.info("srcDir: " + srcDir + " visited, destDir: " + destDir + " created");
            registerCopySuccessEventInHistory(srcDir, destDir);
        } catch (FileAlreadyExistsException ex1) {
            LOG.warn("srcDir: " + srcDir + " visited, destDir " + destDir + " already exists. No creation needed.");
            registerCopySuccessEventInHistory(srcDir, destDir);
        } catch (IOException ex2) {
            LOG.error("Unable to create directory: " + destDir + ", ex: " + ex2.toString());
            registerCopyFailEventInHistory(srcDir, destDir, ex2);
            return FileVisitResult.SKIP_SUBTREE;
        }
        sendCopyStatusProgressEventToListener();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) {
        LOG.debug("*** | visit srcFile: " + srcFile);
        final Path destFile = calculateDestPath(srcFile);
        try {
            Files.copy(srcFile, destFile, copyOptions);
            LOG.info("srcFile " + srcFile + " visited and copied to destFile: " + destFile);
            registerCopySuccessEventInHistory(srcFile, destFile);
        } catch (FileAlreadyExistsException ex) {
            LOG.warn("srcFile " + srcFile + " visited but not copied, destFile: " + destFile + " exists already");
            registerCopySuccessEventInHistory(srcFile, destFile);
        } catch (IOException ioe) {
            LOG.error("Unable to copy: " + srcFile + ", ex: " + ioe.toString());
            registerCopyFailEventInHistory(srcFile, destFile, ioe);
        }
        sendCopyStatusProgressEventToListener();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcPath, IOException ex) {
        LOG.debug("xxx | visit srcFile: " + srcPath + " failed");
        final Path destPath = calculateDestPath(srcPath);
        if (ex instanceof FileSystemLoopException) {
            LOG.warn("Cycle detected: " + srcPath);
        } else {
            LOG.warn("Unable to access: " + srcPath + ", ex: " + ex.toString());
        }
        registerCopyFailEventInHistory(srcPath, destPath, ex);
        sendCopyStatusProgressEventToListener();
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

    public List<Path> getFilesCopied() {
        return filesCopied;
    }

    public List<Path> getCopyErrors() {
        return copyErrors;
    }

    public CopyHistory getCopyHistory() {
        return this.copyHistory;
    }

    private void sendCopyStatusProgressEventToListener() {
        CopyStatus runningCopyStatus = new CopyStatus(rootSrc, rootDest, CopyStatus.CopyState.RUNNING, totalFiles, filesCopied, copyErrors, copyHistory, copyOptions);
        copyListener.ifPresent(listener -> listener.onCopyProgress(runningCopyStatus));
    }

    private void registerCopySuccessEventInHistory(Path srcPath, Path destPath) {
        CopyHistoryEvent copyHistoryEvent = new CopyHistoryEvent(srcPath, destPath);
        copyHistoryEvent.setSuccesfull(true);
        copyHistory.addEvent(copyHistoryEvent);
        filesCopied.add(srcPath);
    }

    private void registerCopyFailEventInHistory(Path srcPath, Path destPath, IOException ex2) {
        CopyHistoryEvent copyHistoryEvent = new CopyHistoryEvent(srcPath, destPath);
        copyHistoryEvent.setSuccesfull(false);
        copyHistoryEvent.setException(ex2);
        copyErrors.add(srcPath);
    }

    private void copyAllAttributesFromSrcToDestDirIfNeeded(Path srcDir) {
        Path destDir = calculateDestPath(srcDir);
        try {
            FileTime time = Files.getLastModifiedTime(srcDir);
            Files.setLastModifiedTime(destDir, time);
            LOG.info("Dest dir " + destDir + " attributes copied from srcDir " + srcDir);
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
