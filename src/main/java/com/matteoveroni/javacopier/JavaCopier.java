package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.copyhistory.CopyHistory;
import com.matteoveroni.javacopier.filevisitors.CopyDirsFileVisitor;
import com.matteoveroni.javacopier.filevisitors.CountFileVisitor;
import com.matteoveroni.javacopier.copyhistory.CopyHistoryEvent;
import com.matteoveroni.javacopier.filevisitors.PrintFileVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * @author Matteo Veroni
 */
public class JavaCopier {

    static final String ERROR_MSG_SRC_OR_DEST_NULL = "src and dest cannot be null";
    static final String ERROR_MSG_SRC_MUST_EXIST = "src must exists";

    private static final CopyOption[] DEFAULT_COPY_OPTIONS = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING};
    private static final Logger LOG = LoggerFactory.getLogger(JavaCopier.class);

    private JavaCopier() {
        //  PRIVATE CONSTRUCTOR
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyOption... copyOptions) throws IllegalArgumentException {
        return copy(src, dest, null, null, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyListener copyListener, CopyOption... copyOptions) throws IllegalArgumentException {
        return copy(src, dest, copyListener, null, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, OutputStream logReportOutputStream, CopyOption... copyOptions) throws IOException {
        return copy(src, dest, null, logReportOutputStream, copyOptions);
    }

    public static CopyStatusReport copy(Path src, Path dest, CopyListener copyListener, OutputStream logReportOutputStream, CopyOption... copyOptions) throws IllegalArgumentException {
        if (src == null || dest == null) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_OR_DEST_NULL);
        } else if (Files.notExists(src)) {
            throw new IllegalArgumentException(ERROR_MSG_SRC_MUST_EXIST);
        }

        LOG.debug("calculating the number of files to copy...");
        Integer totalFiles = calculateFilesCount(src);
        LOG.debug("number of files to copy: " + totalFiles);

        copyOptions = (copyOptions != null) ? copyOptions : DEFAULT_COPY_OPTIONS;
        return executeCopy(src, dest, copyListener, totalFiles, logReportOutputStream, copyOptions);
    }

    private static CopyStatusReport executeCopy(Path src, Path dest, CopyListener copyListener, Integer totalFiles, OutputStream logReportOutputStream, CopyOption[] copyOptions) {
        src = src.toAbsolutePath();
        dest = dest.toAbsolutePath();
        LOG.debug("Copy from src: " + src + " to dest: " + dest + " started");
        CopyHistory copyHistory = new CopyHistory();
        CopyStatusReport copyStatus;
        boolean isCopyMultiple = false;
        try {
            if (src.toFile().isFile() && (Files.notExists(dest) || dest.toFile().isFile())) {
                Files.copy(src, dest, copyOptions);
            } else if (src.toFile().isFile() && (Files.exists(dest) && dest.toFile().isDirectory())) {
                Files.copy(src, Paths.get(dest + File.separator + src.toFile().getName()), copyOptions);
            } else if (src.toFile().isDirectory() && (Files.notExists(dest) || dest.toFile().isDirectory())) {
                isCopyMultiple = true;
                CopyDirsFileVisitor copyDirsFileVisitor = new CopyDirsFileVisitor(src, dest, totalFiles, copyHistory, copyListener, copyOptions);
                Files.walkFileTree(src, copyDirsFileVisitor);
            } else if (src.toFile().isDirectory() && (Files.exists(dest) && dest.toFile().isFile())) {
                isCopyMultiple = true;
                PrintFileVisitor printFileVisitor = null;
                try {
                    printFileVisitor = new PrintFileVisitor(src, dest, new FileOutputStream(dest.toFile()), totalFiles, copyHistory, copyListener, copyOptions);
                    Files.walkFileTree(src, printFileVisitor);
                } finally {
                    if (printFileVisitor != null) {
                        printFileVisitor.closeOutputStream();
                    }
                }
            }
            if (!isCopyMultiple) {
                copyHistory.registerCopySuccessEventInHistory(src, dest);
            }
        } catch (IOException ex) {
            LOG.debug("Exception: " + ex.toString());
            if (!isCopyMultiple) {
                copyHistory.registerCopyFailEventInHistory(src, dest, ex);
            }
        }
        copyStatus = new CopyStatusReport(src, dest, CopyStatusReport.CopyState.DONE, totalFiles, copyHistory, copyOptions);
        notifyCopyStatusToListener(copyStatus, copyListener);
        logCopyReportStatusToOutputStream(logReportOutputStream, copyStatus);
        return copyStatus;
    }

    private static Integer calculateFilesCount(Path src) {
        int filesCount;
        try {
            CountFileVisitor fileCounter = new CountFileVisitor();
            Files.walkFileTree(src, fileCounter);
            filesCount = fileCounter.getFilesCount();
        } catch (IOException ex) {
            LOG.debug("Error during files count. This should not happen because fileCounterVisitor doesnt throw ioexceptions. ex: " + ex);
            filesCount = 0;
        }
        return filesCount;
    }

    private static void notifyCopyStatusToListener(CopyStatusReport copyStatus, CopyListener copyListener) {
        if (copyListener != null) {
            copyListener.onCopyComplete(copyStatus);
        }
    }

    private static void logCopyReportStatusToOutputStream(OutputStream logOutputStream, CopyStatusReport copyStatus) {
        if (logOutputStream != null) {
            try (PrintWriter printWriter = new PrintWriter(logOutputStream, true)) {
                printWriter.println(copyStatus.toPrettyString());
            } catch (Exception ex) {
                LOG.error("Error trying to log copy status report to outputstream. ex: ", ex);
            }
        }
    }
}
