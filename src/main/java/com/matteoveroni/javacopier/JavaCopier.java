package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.filevisitors.CountFilesVisitor;
import com.matteoveroni.javacopier.pojo.CopyHistory;
import com.matteoveroni.javacopier.pojo.CopyHistoryEvent;
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

    public static void copy(File src, File dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), copyListener, copyOptions);
    }

    public static void copy(Path src, Path dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        }
        if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        }
        copyOptions = (copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;

        CopyHistory copyHistory = new CopyHistory();

        LOG.debug("calculating the number of files to copy...");
        Integer totalFilesToCopy = calculateFilesCount(src);
        LOG.debug("number of files to copy: " + totalFilesToCopy);

        CopyStatus finalCopyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.RUNNING, totalFilesToCopy, new ArrayList<>(), new ArrayList<>(), copyHistory, copyOptions);
        if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
            try {
                Files.copy(src, dest, copyOptions);
                copyHistory.addEvent(new CopyHistoryEvent(src, dest, true, null));
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, Arrays.asList(src), new ArrayList<>(), copyHistory, copyOptions);
            } catch (IOException ex) {
                copyHistory.addEvent(new CopyHistoryEvent(src, dest, false, ex));
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, new ArrayList<>(), Arrays.asList(src), copyHistory, copyOptions);
            }
        } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
            try {
                Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
                copyHistory.addEvent(new CopyHistoryEvent(src, dest, true, null));
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, Arrays.asList(src), new ArrayList<>(), copyHistory, copyOptions);
            } catch (IOException ex) {
                copyHistory.addEvent(new CopyHistoryEvent(src, dest, false, ex));
                finalCopyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, new ArrayList<>(), Arrays.asList(src), copyHistory, copyOptions);
            }
        } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
            CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFilesToCopy, (copyListener == null) ? Optional.empty() : Optional.of(copyListener), copyOptions);
            try {
                Files.walkFileTree(src, copyDirsFileVisitor);
            } catch (IOException ex) {
                LOG.debug(ex.toString());
            }
            finalCopyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, copyDirsFileVisitor.getFilesCopied(), copyDirsFileVisitor.getCopyErrors(), copyDirsFileVisitor.getCopyHistory(), copyOptions);
        } else {
            throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
        }
        if (copyListener != null) {
            copyListener.onCopyCompleted(finalCopyStatus);
        }
    }

    // TODO: ugly. Change this code
    private static Integer calculateFilesCount(Path src) {
        CountFilesVisitor fileCounter = new CountFilesVisitor();
        try {
            Files.walkFileTree(src, fileCounter);
        } catch (IOException ex) {
            LOG.error(ex.toString());
            return 0;
        }
        return fileCounter.getFileCount();
    }
}
