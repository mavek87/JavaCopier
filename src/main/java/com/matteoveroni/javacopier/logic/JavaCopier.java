package com.matteoveroni.javacopier.logic;

import com.matteoveroni.javacopier.logic.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.logic.filevisitors.CountFilesVisitor;
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

    public static CopyStatus copy(File src, File dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), null, copyOptions);
    }

    public static CopyStatus copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy(src, dest, null, copyOptions);
    }

    public static CopyStatus copy(File src, File dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        return copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), copyListener, copyOptions);
    }

    public static CopyStatus copy(Path src, Path dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        }
        if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        }

        copyOptions = (copyOptions == null || copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;

        LOG.debug("calculating the number of files to copy...");
        Integer totalFilesToCopy = calculateFilesCount(src);
        LOG.debug("number of files to copy: " + totalFilesToCopy);

        CopyStatus copyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.RUNNING, totalFilesToCopy, new ArrayList<>(), new ArrayList<>(), new CopyHistory(), copyOptions);
        if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
            try {
                Files.copy(src, dest, copyOptions);
                copyStatus = getSingleCopySuccessStatus(src, dest, totalFilesToCopy, copyOptions);
            } catch (IOException ex) {
                copyStatus = getSingleCopyFailStatus(src, dest, totalFilesToCopy, ex, copyOptions);
            }
        } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
            try {
                Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
                copyStatus = getSingleCopySuccessStatus(src, dest, totalFilesToCopy, copyOptions);
            } catch (IOException ex) {
                copyStatus = getSingleCopyFailStatus(src, dest, totalFilesToCopy, ex, copyOptions);
            }
        } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
            CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFilesToCopy, (copyListener == null) ? Optional.empty() : Optional.of(copyListener), copyOptions);
            try {
                Files.walkFileTree(src, copyDirsFileVisitor);
            } catch (IOException ex) {
                LOG.debug(ex.toString());
            }
            copyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, copyDirsFileVisitor.getFilesCopied(), copyDirsFileVisitor.getCopyErrors(), copyDirsFileVisitor.getCopyHistory(), copyOptions);
        } else {
            throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
        }
        if (copyListener != null) {
            copyListener.onCopyCompleted(copyStatus);
        }
        return copyStatus;
    }

    private static CopyStatus getSingleCopySuccessStatus(Path src, Path dest, Integer totalFilesToCopy, CopyOption[] copyOptions) {
        CopyStatus copyStatus;
        CopyHistory copyHistory = new CopyHistory();
        copyHistory.addEvent(new CopyHistoryEvent(src, dest, true, null));
        copyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, Arrays.asList(src), new ArrayList<>(), copyHistory, copyOptions);
        return copyStatus;
    }

    private static CopyStatus getSingleCopyFailStatus(Path src, Path dest, Integer totalFilesToCopy, IOException ex, CopyOption[] copyOptions) {
        CopyStatus copyStatus;
        CopyHistory copyHistory = new CopyHistory();
        copyHistory.addEvent(new CopyHistoryEvent(src, dest, false, ex));
        copyStatus = new CopyStatus(src, dest, CopyStatus.CopyState.DONE, totalFilesToCopy, new ArrayList<>(), Arrays.asList(src), copyHistory, copyOptions);
        return copyStatus;
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
