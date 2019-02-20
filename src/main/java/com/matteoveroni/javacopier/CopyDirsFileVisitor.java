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

        Path destDir = dest.resolve(src.relativize(srcDir));
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
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        System.out.println("visitFile " + file);
        Path newFile = dest.resolve(src.relativize(file));
        try {
            Path createdNewFile = Files.copy(file, newFile, copyOptions);
        } catch (IOException ex) {
            System.err.format("visitFile - Unable to copy: %s: %n", file, ex);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ex) {
        System.out.println("visitFileFailed " + file);
        if (ex instanceof FileSystemLoopException) {
            System.err.println("Cycle detected: " + file);
        } else {
            System.err.format("visitFileFailed - Unable to access: %s: %n", file, ex);
        }
        ex.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path srcDir, IOException exc) throws IOException {
        System.out.println("postVisitDirectory " + srcDir);

        if (exc == null && containsCopyOption(StandardCopyOption.COPY_ATTRIBUTES)) {
            Path destDir = dest.resolve(src.relativize(srcDir));
            try {
                FileTime time = Files.getLastModifiedTime(srcDir);
                Files.setLastModifiedTime(dest.resolve(src.relativize(srcDir)), time);
            } catch (IOException ex) {
                System.err.format("Unable to copy all attributes to: %s: %n", destDir, ex);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean containsCopyOption(StandardCopyOption copyOption) {
        for (int i = 0; i < copyOptions.length; i++) {
            if (copyOptions[i].equals(copyOption)) {
                return true;
            }
        }
        return false;
    }

}
