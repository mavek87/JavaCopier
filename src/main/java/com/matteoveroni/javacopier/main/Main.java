package com.matteoveroni.javacopier.main;

import com.matteoveroni.javacopier.CopyListener;
import com.matteoveroni.javacopier.JavaCopier;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.matteoveroni.javacopier.CopyStatusReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matteo Veroni
 */
public class Main implements CopyListener {
    private enum OS {
        WINDOWS, LINUX
    }

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String SRC_LINUX = "/home/mavek/Scaricati";
    private static final String DEST_LINUX = "/home/mavek/dest/Scaricati";
    private static final String SRC_WIN = "C:\\users\\veroni\\vertx";
    private static final String DEST_WIN = "C:\\users\\veroni\\dest2\\";
    private static final OS ENVIRONMENT = OS.LINUX;

    public static void main(String[] args) throws IOException {
        new Main().start();
    }

    public void start() throws IOException {
        LOG.debug("MAIN");
        Path srcPath;
        Path destPath;
        switch (ENVIRONMENT) {
            case LINUX:
                srcPath = Paths.get(SRC_LINUX);
                destPath = Paths.get(DEST_LINUX);
                break;
            case WINDOWS:
                srcPath = Paths.get(SRC_WIN);
                destPath = Paths.get(DEST_WIN);
                break;
            default:
                throw new RuntimeException("Unknown OS");
        }
        FileOutputStream f = new FileOutputStream("/home/mavek/output");
//        JavaCopier.copy(srcPath, destPath, this, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        CopyStatusReport copyStatusReport = JavaCopier.copy(srcPath, destPath, this);
    }

    @Override
    public void onCopyProgress(CopyStatusReport copyStatus) {
        LOG.debug("file analyzed: " + copyStatus.getCopyHistory().getHistory().size());
        LOG.debug("copy percentage: " + copyStatus.getCopyPercentageInString());
    }

    @Override
    public void onCopyComplete(CopyStatusReport finalCopyStatusReport) {
        LOG.debug("copy completed: " + finalCopyStatusReport.toPrettyString());
    }
}
