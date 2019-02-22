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

    private static final Logger LOG = LoggerFactory.getLogger(JavaCopier.class);

    public static final CopyOption[] STANDARD_COPY_OPTIONS = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
    static final String ERROR_MSG_SRC_OR_DEST_NULL = "src and dest cannot be null";
    static final String ERROR_MSG_SRC_MUST_EXIST = "src must exist";
    static final String ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE = "cannot copy a directory into a file";

    public static CopyStatusReport copy(File src, File dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), null, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy(src, dest, null, copyOptions);
    }

    public static CopyStatusReport copy(File src, File dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        return copy((src == null) ? null : src.toPath(), (dest == null) ? null : dest.toPath(), copyListener, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        }
        if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        }

        copyOptions = (copyOptions == null || copyOptions.length == 0) ? STANDARD_COPY_OPTIONS : copyOptions;

        LOG.debug("calculating the number of files to copy...");
        Integer totalFiles = 0;
        try {
            totalFiles = calculateFilesCount(src);
        } catch (IOException ex) {
            return buildCopyFailStatusReport(src, dest, totalFiles, ex, copyOptions);
        }
        LOG.debug("number of files to copy: " + totalFiles);

        CopyStatusReport copyStatus = new CopyStatusReport(src, dest, CopyStatusReport.CopyState.RUNNING, totalFiles, new CopyHistory(), copyOptions);
//        try {
            if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
                try {
                    Files.copy(src, dest, copyOptions);
                    copyStatus = buildCopySuccessStatusReport(src, dest, totalFiles, copyOptions);
                } catch (IOException ex) {
                    copyStatus = buildCopyFailStatusReport(src, dest, totalFiles, ex, copyOptions);
                }
            } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
                try {
                    Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
                    copyStatus = buildCopySuccessStatusReport(src, dest, totalFiles, copyOptions);
                } catch (IOException ex) {
                    copyStatus = buildCopyFailStatusReport(src, dest, totalFiles, ex, copyOptions);
                }
            } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
                try {
                    CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFiles, (copyListener == null) ? Optional.empty() : Optional.of(copyListener), copyOptions);
                    Files.walkFileTree(src, copyDirsFileVisitor);
                    copyStatus = buildCopySuccessStatusReport(src, dest, totalFiles, copyOptions);
                } catch (IOException ex) {
                    LOG.debug("Severe error it should not happen --->> " + ex.toString());
                    copyStatus = buildCopyFailStatusReport(src, dest, totalFiles, ex, copyOptions);
                }
            } else {
                throw new IllegalArgumentException(ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);
            }
//        } catch (IOException ex) {
        
//        }
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
        copyHistory.addHistoryEvent(new CopyHistoryEvent(src, dest, true, null));
        return new CopyStatusReport(src, dest, CopyStatusReport.CopyState.DONE, totalFiles, copyHistory, copyOptions);
    }

    private static CopyStatusReport buildCopyFailStatusReport(Path src, Path dest, Integer totalFiles, IOException ex, CopyOption[] copyOptions) {
        CopyHistory copyHistory = new CopyHistory();
        copyHistory.addHistoryEvent(new CopyHistoryEvent(src, dest, false, ex));
        return new CopyStatusReport(src, dest, CopyStatusReport.CopyState.DONE, totalFiles, copyHistory, copyOptions);
    }

    private static Integer calculateFilesCount(Path src) throws IOException {
        CountFilesVisitor fileCounter = new CountFilesVisitor();
        Files.walkFileTree(src, fileCounter);
        return fileCounter.getFilesCount();
    }
}
