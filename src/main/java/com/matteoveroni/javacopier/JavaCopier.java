package com.matteoveroni.javacopier;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author Matteo Veroni
 */
public class JavaCopier {

    public static final CopyOption[] STANDARD_COPY_OPTIONS = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};

    public void copy(File src, File dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        this.copy(src.toPath(), dest.toPath(), copyOptions);
    }

    public void copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        if (Files.notExists(src) || Files.notExists(dest)) {
            throw new IllegalArgumentException("src and dest must exist");
        }
        copyOptions = (copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;
        if (src.toFile().isFile() && dest.toFile().isFile()) {
            Files.copy(src, dest, copyOptions);
        } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
            Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
        } else if (src.toFile().isDirectory() && dest.toFile().isDirectory()) {
            Files.walkFileTree(src, new CopyDirsFileVisitor(src, dest, copyOptions));
        } else {
            throw new IllegalArgumentException("cannot copy a directory into a file");
        }
    }
}
