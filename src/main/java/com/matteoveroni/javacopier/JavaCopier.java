package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.copyhistory.CopyHistory;
import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.filevisitors.CountFilesVisitor;
import com.matteoveroni.javacopier.copyhistory.CopyHistoryEvent;
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
    protected static final String ERROR_MSG_SRC_OR_DEST_NULL = "src and dest cannot be null";
    protected static final String ERROR_MSG_SRC_MUST_EXIST = "src must exist";
    protected static final String ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE = "cannot copy a directory into a file";
    private static final Logger LOG = LoggerFactory.getLogger(JavaCopier.class);

    public static CopyStatusReport copy(File src, File dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), null, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy(src, dest, null, copyOptions);
    }

    public static CopyStatusReport copy(File src, File dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), copyListener, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        } else if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        } else if (src.toFile().isDirectory() && (Files.exists(dest) && dest.toFile().isFile())) {
            throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
        }

        LOG.debug("calculating the number of files to copy...");
        Integer totalFiles = calculateFilesCount(src);
        LOG.debug("number of files to copy: " + totalFiles);

        copyOptions = (copyOptions == null || copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;

        CopyStatusReport copyStatus;
        try {
            if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
                Files.copy(src, dest, copyOptions);
            } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
                Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
            } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
                CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFiles, (copyListener == null) ? Optional.empty() : Optional.of(copyListener), copyOptions);
                Files.walkFileTree(src, copyDirsFileVisitor);
            }
            copyStatus = buildCopySuccessStatusReport(src, dest, totalFiles, copyOptions);
        } catch (IOException ex) {
            LOG.debug("Exception: " + ex.toString());
            copyStatus = buildCopyFailStatusReport(src, dest, totalFiles, ex, copyOptions);
        }
        notifyCopyStateToListener(copyListener, copyStatus);
        return copyStatus;
    }

    private static void notifyCopyStateToListener(CopyListener copyListener, CopyStatusReport copyStatus) {
        if (copyListener != null) {
            copyListener.onCopyCompleted(copyStatus);
        }
    }

    private static CopyStatusReport buildCopySuccessStatusReport(Path src, Path dest, Integer totalFiles, CopyOption[] copyOptions) {
        CopyHistory copyHistory = new CopyHistory();
        copyHistory.addHistoryEvent(
            new CopyHistoryEvent.Builder(src, dest)
            .setSuccessful()
            .build()
        );
        return new CopyStatusReport(src, dest, CopyStatusReport.CopyState.DONE, totalFiles, copyHistory, copyOptions);
    }

    private static CopyStatusReport buildCopyFailStatusReport(Path src, Path dest, Integer totalFiles, IOException ex, CopyOption[] copyOptions) {
        CopyHistory copyHistory = new CopyHistory();
        copyHistory.addHistoryEvent(
            new CopyHistoryEvent.Builder(src, dest)
                .setFailed(ex)
                .build()
        );
        return new CopyStatusReport(src, dest, CopyStatusReport.CopyState.DONE, totalFiles, copyHistory, copyOptions);
    }

    private static Integer calculateFilesCount(Path src) throws IOException {
        CountFilesVisitor fileCounter = new CountFilesVisitor();
        Files.walkFileTree(src, fileCounter);
        return fileCounter.getFilesCount();
    }
}
