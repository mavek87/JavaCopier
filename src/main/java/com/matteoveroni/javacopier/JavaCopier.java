package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.copyhistory.CopyHistory;
import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.filevisitors.CountFilesVisitor;
import com.matteoveroni.javacopier.copyhistory.CopyHistoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * @author Matteo Veroni
 */
public class JavaCopier {

    public static final CopyOption[] STANDARD_COPY_OPTIONS = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
    static final String ERROR_MSG_SRC_OR_DEST_NULL = "src and dest cannot be null";
    static final String ERROR_MSG_SRC_MUST_EXIST = "src must exist";
    static final String ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE = "cannot copy a directory into a file";
    private static final Logger LOG = LoggerFactory.getLogger(JavaCopier.class);

    public static CopyStatusReport copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException, IOException {
        return copy(src, dest, null, copyOptions);
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

        copyOptions = (copyOptions != null) ? copyOptions : STANDARD_COPY_OPTIONS;

        return executeCopy(src, dest, copyListener, totalFiles, copyOptions);
    }

    private static CopyStatusReport executeCopy(Path src, Path dest, CopyListener copyListener, Integer totalFiles, CopyOption[] copyOptions) {
        LOG.debug("Copy from src: " + src + " to dest: " + dest + " started");
        CopyHistory copyHistory = new CopyHistory();
        CopyStatusReport copyStatus;
        boolean isCopyMultiple = false;
        try {
            if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
                Files.copy(src, dest, copyOptions);
            } else if (src.toFile().isFile() && dest.toFile().isDirectory()) {
                Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
            } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
                isCopyMultiple = true;
                CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFiles, copyHistory, copyListener, copyOptions);
                Files.walkFileTree(src, copyDirsFileVisitor);
            }
            if (!isCopyMultiple) {
                registerSingleCopySuccessToHistory(src, dest, copyHistory);
            }
        } catch (IOException ex) {
            LOG.debug("Exception: " + ex.toString());
            if (!isCopyMultiple) {
                registerSingleCopyFailureToHistory(src, dest, ex, copyHistory);
            }
        }
        copyStatus = new CopyStatusReport(src, dest, CopyStatusReport.CopyState.DONE, totalFiles, copyHistory, copyOptions);
        notifyCopyStatusToListener(copyStatus, copyListener);

        // TODO: log copyStatus to a file. Now the code is hardcoded. Pass an outputstream to copy from outside
        try (PrintWriter p = new PrintWriter(new FileOutputStream("/home/mavek/java-copier-log.txt", true))) {
            p.println(copyStatus.toPrettyString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return copyStatus;
    }

    private static Integer calculateFilesCount(Path src) throws IOException {
        CountFilesVisitor fileCounter = new CountFilesVisitor();
        Files.walkFileTree(src, fileCounter);
        return fileCounter.getFilesCount();
    }

    private static void registerSingleCopySuccessToHistory(Path src, Path dest, CopyHistory copyHistory) {
        copyHistory.addHistoryEvent(
                new CopyHistoryEvent.Builder(src, dest)
                        .setSuccessful()
                        .build()
        );
    }

    private static void registerSingleCopyFailureToHistory(Path src, Path dest, IOException ex, CopyHistory copyHistory) {
        copyHistory.addHistoryEvent(
                new CopyHistoryEvent.Builder(src, dest)
                        .setFailed(ex)
                        .build()
        );
    }

    private static void notifyCopyStatusToListener(CopyStatusReport copyStatus, CopyListener copyListener) {
        if (copyListener != null) {
            copyListener.onCopyComplete(copyStatus);
        }
    }
}
