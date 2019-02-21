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

/**
 *
 * @author Matteo Veroni
 */
public class CopyDirsFileVisitor implements FileVisitor<Path> {

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
        System.out.println("preVisitDirectory " + srcDir);
        Path destDir = calculateDestPath(srcDir);
        if (Files.notExists(destDir)) {
            try {
                Path createdDirectory = Files.createDirectory(destDir);
            } catch (IOException ex) {
                System.err.format("preVisitDirectory - Unable to create directory: %s: %n", dest, ex);
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) {
        System.out.println("visitFile " + srcFile);
        Path destFile = calculateDestPath(srcFile);
        try {
            Path createdNewFile = Files.copy(srcFile, destFile, copyOptions);
        } catch (IOException ex) {
            System.err.format("visitFile - Unable to copy: %s: %n", srcFile, ex);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path srcFile, IOException ex) {
        System.out.println("visitFileFailed " + srcFile);
        if (ex instanceof FileSystemLoopException) {
            System.err.println("Cycle detected: " + srcFile);
        } else {
            System.err.format("visitFileFailed - Unable to access: %s: %n", srcFile, ex);
        }
        ex.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path srcDir, IOException exc) throws IOException {
        System.out.println("postVisitDirectory " + srcDir);
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
        } catch (IOException ex) {
            System.err.format("Unable to copy all attributes to: %s: %n", destDir, ex);
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
