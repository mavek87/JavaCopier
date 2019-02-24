package com.matteoveroni.javacopier.main;

import com.matteoveroni.javacopier.CopyListener;
import com.matteoveroni.javacopier.JavaCopier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.matteoveroni.javacopier.copystatus.CopyStatusReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matteo Veroni
 */
public class Main implements CopyListener {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String SRC_LINUX = "/home/mavek/src/";
    private static final String DEST_LINUX = "/home/mavek/dest/";
    private static final String SRC_WIN = "C:\\users\\veroni\\vertx";
    private static final String DEST_WIN = "C:\\users\\veroni\\dest2\\";

    private enum OS {
        WINDOWS, LINUX
    }

    private static final OS ENVIRONMENT = OS.LINUX;

    public static void main(String[] args) throws IOException {
        new Main().startTest();
    }

    public void startTest() throws IOException {
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

//        JavaCopier.copy(srcPath, destPath, this, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        CopyStatusReport copy = JavaCopier.copy(srcPath, destPath, this);
    }

    @Override
    public void onCopyProgress(CopyStatusReport copyStatus) {
        LOG.debug("file analyzed: " + copyStatus.getCopyHistory().getHistory().size());
        LOG.debug("copy percentage: " + copyStatus.getCopyPercentageInString());
    }

    @Override
    public void onCopyCompleted(CopyStatusReport finalCopyStatus) {
        LOG.debug("copy completed " + finalCopyStatus);
    }
}
