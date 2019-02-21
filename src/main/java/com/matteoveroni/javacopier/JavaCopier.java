package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Matteo Veroni
 */
public class JavaCopier {

    public static final CopyOption[] STANDARD_COPY_OPTIONS = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};

    static final String ERROR_MSG_SRC_OR_DEST_NULL = "src and dest cannot be null";
    static final String ERROR_MSG_SRC_MUST_EXIST = "src must exist";
    static final String ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE = "cannot copy a directory into a file";

    public void copy(File src, File dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        this.copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), copyOptions);
    }

    public void copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        }
        if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        }
        copyOptions = (copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;
        if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
            Files.copy(src, dest, copyOptions);
        } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
            Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
        } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
            Files.walkFileTree(src, new CopyDirsFileVisitor(src, dest, copyOptions));
        } else {
            throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
        }
    }
}
