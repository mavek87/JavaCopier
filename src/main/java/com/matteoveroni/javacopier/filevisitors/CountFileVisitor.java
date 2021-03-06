package com.matteoveroni.javacopier.filevisitors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Matteo Veroni
 */
public class CountFileVisitor implements FileVisitor<Path> {

    private final AtomicInteger fileCounter = new AtomicInteger(0);

    public Integer getFilesCount() {
        return fileCounter.get();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        fileCounter.getAndIncrement();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        fileCounter.getAndIncrement();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ex) {
        fileCounter.getAndIncrement();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException ex)  {
        return FileVisitResult.CONTINUE;
    }
}
