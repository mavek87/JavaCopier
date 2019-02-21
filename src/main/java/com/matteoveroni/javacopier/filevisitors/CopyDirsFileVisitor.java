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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.matteoveroni.javacopier.CopyListener;
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
        Path destDir = calculateDestPath(srcDir);
        CopyHistoryEvent copyHistoryEvent = new CopyHistoryEvent(srcDir, destDir);
        try {
            Files.createDirectory(destDir);
            LOG.info("srcDir: " + srcDir + " visited, destDir: " + destDir + " created");
            registerCopySuccessEventInHistory(srcDir, copyHistoryEvent);
        } catch (FileAlreadyExistsException ex1) {
            LOG.warn("srcDir: " + srcDir + " visited, destDir " + destDir + " already exists. No creation needed.");
            registerCopySuccessEventInHistory(srcDir, copyHistoryEvent);
        } catch (IOException ex2) {
            LOG.error("Unable to create directory: " + destDir + ", ex: " + ex2.toString());
            registerCopyFailEventInHistory(srcDir, copyHistoryEvent, ex2);
            return FileVisitResult.SKIP_SUBTREE;
        }
        sendCopyEventToListener(copyHistoryEvent);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) {
        LOG.debug("*** | visit srcFile: " + srcFile);
        Path destFile = calculateDestPath(srcFile);
        CopyHistoryEvent copyHistoryEvent = new CopyHistoryEvent(srcFile, destFile);
        try {
            Files.copy(srcFile, destFile, copyOptions);
            LOG.info("srcFile " + srcFile + " visited and copied to destFile: " + destFile);
            registerCopySuccessEventInHistory(srcFile, copyHistoryEvent);
        } catch (FileAlreadyExistsException ex) {
            LOG.warn("srcFile " + srcFile + " visited but not copied, destFile: " + destFile + " exists already");
            registerCopySuccessEventInHistory(srcFile, copyHistoryEvent);
        } catch (IOException ioe) {
            LOG.error("Unable to copy: " + srcFile + ", ex: " + ioe.toString());
            registerCopyFailEventInHistory(srcFile, copyHistoryEvent, ioe);
        }
        sendCopyEventToListener(copyHistoryEvent);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcFile, IOException ex) {
        LOG.debug("xxx | visit srcFile: " + srcFile + " failed");
        Path destPath = calculateDestPath(srcFile);
        if (!copyErrors.contains(srcFile)) {
            copyErrors.add(srcFile);
        }
        sendCopyEventToListener(new CopyHistoryEvent(srcFile, destPath, false, ex));
        if (ex instanceof FileSystemLoopException) {
            LOG.warn("Cycle detected: " + srcFile);
        } else {
            LOG.warn("Unable to access: " + srcFile + ", ex: " + ex.toString());
        }
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

    private void sendCopyEventToListener(CopyHistoryEvent copyHistoryEvent) {
        if (copyListener.isPresent()) {
            copyHistory.addEvent(copyHistoryEvent);
            copyListener.get().onCopyProgress(new CopyStatus(rootSrc, rootDest, CopyStatus.CopyState.RUNNING, totalFiles, filesCopied, copyErrors, copyHistory, copyOptions));
        }
    }

    private void registerCopySuccessEventInHistory(Path srcDir, CopyHistoryEvent copyHistoryEvent) {
        filesCopied.add(srcDir);
        copyHistoryEvent.setSuccesfull(true);
    }

    private void registerCopyFailEventInHistory(Path srcFile, CopyHistoryEvent copyHistoryEvent, IOException ex2) {
        copyErrors.add(srcFile);
        copyHistoryEvent.setSuccesfull(false);
        copyHistoryEvent.setException(ex2);
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
