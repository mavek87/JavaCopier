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
public class CopyFileVisitor implements FileVisitor<Path> {

    private final Path dest;
    private final Path src;
    private final boolean preserve;

    public CopyFileVisitor(Path src, Path dest, boolean preserve) {
        this.src = src;
        this.dest = dest;
        this.preserve = preserve;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.println("preVisitDirectory " + dir);

//        CopyOption[] copyOptions = (preserve)
//            ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES} 
//            : new CopyOption[0];
//        try {
        Path newDir = dest.resolve(src.relativize(dir));
        Path createdNewDir = Files.createDirectories(newDir);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return FileVisitResult.SKIP_SUBTREE;
//        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        System.out.println("visitFile " + file);

        CopyOption[] copyOptions = (preserve)
            ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING}
            : new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};

        try {
            Path newFile = dest.resolve(src.relativize(file));
            Path createdNewFile = Files.copy(file, newFile, copyOptions);
        } catch (IOException ex) {
            System.err.format("visitFile - Unable to copy: %s: %n", file, ex);
            ex.printStackTrace();
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
        if (Files.isDirectory(file)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("postVisitDirectory " + dir);

        if (exc == null && preserve) {
            Path newDir = dest.resolve(src.relativize(dir));
            try {
                FileTime time = Files.getLastModifiedTime(dir);
                Files.setLastModifiedTime(dest.resolve(src.relativize(dir)), time);
            } catch (IOException ex) {
                System.err.format("Unable to copy all attributes to: %s: %n", newDir, ex);
            }
        }
        return FileVisitResult.CONTINUE;
    }

}
