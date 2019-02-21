package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.filevisitors.CountFileVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

/**
 * @author Matteo Veroni
 */
public class JavaCopier {

    public static final CopyOption[] STANDARD_COPY_OPTIONS = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
    static final String ERROR_MSG_SRC_OR_DEST_NULL = "src and dest cannot be null";
    static final String ERROR_MSG_SRC_MUST_EXIST = "src must exist";
    static final String ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE = "cannot copy a directory into a file";
//    private final static Logger LOG = LoggerFactory.getLogger(JavaCopier.class);

    public static void copy(File src, File dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), null, copyOptions);
    }

    public static void copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        copy(src, dest, null, copyOptions);
    }

    public static void copy(File src, File dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), copyListener, copyOptions);
    }

    public static void copy(Path src, Path dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        }
        if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        }
        copyOptions = (copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;

        Integer totalFilesToCopy = calculateFilesToCopy(src);
//        LOG.debug("totalFilesToCopy: " + totalFilesToCopy);

        if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
            Files.copy(src, dest, copyOptions);
        } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
            Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
        } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
            Files.walkFileTree(src, new CopyDirsFileVisitor(src, dest, totalFilesToCopy, (copyListener == null) ? Optional.empty() : Optional.of(copyListener), copyOptions));
        } else {
            throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
        }
    }

    private static Integer calculateFilesToCopy(Path src) throws IOException {
        CountFileVisitor fileCounter = new CountFileVisitor();
        Files.walkFileTree(src, fileCounter);
        return fileCounter.getFileCount();
    }
}
