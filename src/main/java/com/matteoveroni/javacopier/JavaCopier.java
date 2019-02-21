package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.filevisitors.CountFilesVisitor;
import com.matteoveroni.javacopier.pojo.CopyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Matteo Veroni
 */
public class JavaCopier {

    private static final Logger LOG = LoggerFactory.getLogger(JavaCopier.class);

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

        LOG.debug("calculating the number of files to copy...");
        Integer totalFilesToCopy = calculateFilesCount(src);
        LOG.debug("number of files to copy: " + totalFilesToCopy);

        CopyStatus finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.RUNNING, totalFilesToCopy, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), copyOptions);
        if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
            try {
                Files.copy(src, dest, copyOptions);
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.COMPLETED, totalFilesToCopy, Arrays.asList(src), new ArrayList<>(), Arrays.asList(src), copyOptions);
            } catch (IOException ex) {
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.COMPLETED_WITH_ERRORS, totalFilesToCopy, new ArrayList<>(), Arrays.asList(src), Arrays.asList(src), copyOptions);
            }
        } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
            try {
                Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.COMPLETED, totalFilesToCopy, Arrays.asList(src), new ArrayList<>(), Arrays.asList(src), copyOptions);
            } catch (IOException ex) {
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.COMPLETED_WITH_ERRORS, totalFilesToCopy, new ArrayList<>(), Arrays.asList(src), Arrays.asList(src), copyOptions);
            }
        } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
            CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFilesToCopy, (copyListener == null) ? Optional.empty() : Optional.of(copyListener), copyOptions);
            try {
                Files.walkFileTree(src, copyDirsFileVisitor);
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.COMPLETED, totalFilesToCopy, copyDirsFileVisitor.getFilesCopied(), copyDirsFileVisitor.getCopyErrors(), copyDirsFileVisitor.getCopyHistory(), copyOptions);
            } catch (IOException ex) {
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.State.COMPLETED_WITH_ERRORS, totalFilesToCopy, copyDirsFileVisitor.getFilesCopied(), copyDirsFileVisitor.getCopyErrors(), copyDirsFileVisitor.getCopyHistory(),  copyOptions);
            }
        } else {
            throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
        }
        if (copyListener != null) {
            copyListener.onCopyCompleted(finalCopyStatus);
        }
    }

    private static Integer calculateFilesCount(Path src) throws IOException {
        CountFilesVisitor fileCounter = new CountFilesVisitor();
        Files.walkFileTree(src, fileCounter);
        return fileCounter.getFileCount();
    }
}
