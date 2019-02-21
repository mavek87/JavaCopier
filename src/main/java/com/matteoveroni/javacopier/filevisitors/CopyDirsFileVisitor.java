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
import com.matteoveroni.javacopier.pojo.CopyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matteo Veroni
 */
public class CopyDirsFileVisitor implements FileVisitor<Path> {

    private final static Logger LOG = LoggerFactory.getLogger(CopyDirsFileVisitor.class);

    private final Path dest;
    private final Path src;
    private final CopyOption[] copyOptions;

    private final Optional<CopyListener> copyListener;

    private final int totalFilesToCopy;
    private final List<Path> copyHistory = new ArrayList<>();
    private final List<Path> filesCopied = new ArrayList<>();
    private final List<Path> copyErrors = new ArrayList<>();

    public CopyDirsFileVisitor(Path src, Path dest, int totalFilesToCopy, Optional<CopyListener> copyListener, CopyOption[] copyOptions) {
        this.src = src;
        this.dest = dest;
        this.totalFilesToCopy = totalFilesToCopy;
        this.copyOptions = copyOptions;
        this.copyListener = copyListener;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path srcDir, BasicFileAttributes attrs) {
        LOG.debug("+++ | pre visit srcDir: " + srcDir);
        copyHistory.add(srcDir);
        Path destDir = calculateDestPath(srcDir);
        if (Files.exists(destDir)) {
            LOG.info("destDir " + destDir + " already exists. No copy needed.");
            filesCopied.add(srcDir);
        } else {
            try {
                Path createdDirectory = Files.createDirectory(destDir);
                LOG.info("srcDir: " + srcDir + " visited, destDir: " + createdDirectory + " created");
                filesCopied.add(srcDir);
            } catch (IOException ex) {
                LOG.warn("Unable to create directory: " + dest + ", ex: " + ex.toString());
                copyErrors.add(srcDir);
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        if (copyListener.isPresent()) {
            copyListener.get().onCopyProgress(new CopyStatus(src, dest, CopyStatus.State.RUNNING, totalFilesToCopy, filesCopied, copyErrors, copyHistory, copyOptions));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) {
        LOG.debug("*** | visit srcFile: " + srcFile);
        copyHistory.add(srcFile);
        Path destFile = calculateDestPath(srcFile);
        try {
            Path createdNewFile = Files.copy(srcFile, destFile, copyOptions);
            filesCopied.add(srcFile);
            LOG.info("srcFile " + srcFile + " visited and copied to destFile: " + destFile);
        } catch (IOException ex) {
            copyErrors.add(srcFile);
            LOG.warn("Unable to copy: " + srcFile + ", ex: " + ex.toString());
        }
        if (copyListener.isPresent()) {
            copyListener.get().onCopyProgress(new CopyStatus(src, dest, CopyStatus.State.RUNNING, totalFilesToCopy, filesCopied, copyErrors, copyHistory, copyOptions));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcFile, IOException ex) {
        LOG.debug("xxx | visit srcFile: " + srcFile + " failed");
        copyHistory.add(srcFile);
        if (!copyErrors.contains(srcFile)) {
            copyErrors.add(srcFile);
        }
        if (copyListener.isPresent()) {
            copyListener.get().onCopyProgress(new CopyStatus(src, dest, CopyStatus.State.RUNNING, totalFilesToCopy, filesCopied, copyErrors, copyHistory, copyOptions));
        }
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

    public List<Path> getCopyHistory() {
        return this.copyHistory;
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
        return dest.resolve(src.relativize(srcPath));
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
