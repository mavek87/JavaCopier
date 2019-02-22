package com.matteoveroni.javacopier;

import com.matteoveroni.javacopier.logic.JavaCopier;
import com.matteoveroni.javacopier.logic.CopyListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.matteoveroni.javacopier.pojo.CopyStatus;
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
        CopyStatus copy = JavaCopier.copy(srcPath, destPath, this);
    }

    @Override
    public void onCopyProgress(CopyStatus copyStatus) {
        LOG.debug("file copied: " + copyStatus.getCopyHistory().getHistory().size());
        LOG.debug("copy percentage: " + copyStatus.getCopyPercentageInString());
    }

    @Override
    public void onCopyCompleted(CopyStatus finalCopyStatus) {
        LOG.debug("copy completed " + finalCopyStatus);
    }
}
