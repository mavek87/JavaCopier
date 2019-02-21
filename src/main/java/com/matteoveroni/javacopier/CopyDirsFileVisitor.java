package com.matteoveroni.javacopier;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Veroni
 */
public class CopyDirsFileVisitor implements FileVisitor<Path> {

    private final static Logger LOG = LoggerFactory.getLogger(CopyDirsFileVisitor.class);

    private final Path dest;
    private final Path src;
    private final CopyOption[] copyOptions;

    public CopyDirsFileVisitor(Path src, Path dest, CopyOption[] copyOptions) {
        this.src = src;
        this.dest = dest;
        this.copyOptions = copyOptions;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path srcDir, BasicFileAttributes attrs) {
        LOG.debug("+++ | pre visit srcDir: " + srcDir);
        Path destDir = calculateDestPath(srcDir);
        if (Files.notExists(destDir)) {
            try {
                Path createdDirectory = Files.createDirectory(destDir);
                LOG.info("Dest dir " + createdDirectory + " created");
            } catch (IOException ex) {
                LOG.warn("Unable to create directory: " + dest + ", ex: " + ex);
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) {
        LOG.debug("*** | visit srcFile: " + srcFile);
        Path destFile = calculateDestPath(srcFile);
        try {
            Path createdNewFile = Files.copy(srcFile, destFile, copyOptions);
            LOG.info("Dest file " + createdNewFile + " created");
        } catch (IOException ex) {
            LOG.warn("Unable to copy: " + srcFile + ", ex: " + ex.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcFile, IOException ex) {
        LOG.debug("xxx | visit srcFile: " + srcFile + " failed");
        if (ex instanceof FileSystemLoopException) {
            LOG.warn("Cycle detected: " + srcFile);
        } else {
            LOG.warn("Unable to access: " + srcFile + ", ex: " + ex.getMessage());
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

    private void copyAllAttributesFromSrcToDestDirIfNeeded(Path srcDir) {
        Path destDir = calculateDestPath(srcDir);
        try {
            FileTime time = Files.getLastModifiedTime(srcDir);
            Files.setLastModifiedTime(destDir, time);
            LOG.info("Dest dir " + destDir + " attributes copied from srcDir" + srcDir);
        } catch (IOException ex) {
            LOG.warn("Unable to copy all attributes to: " + destDir + ", ex: " + ex.getMessage());
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
