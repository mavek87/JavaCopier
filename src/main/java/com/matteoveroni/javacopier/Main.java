package com.matteoveroni.javacopier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matteo Veroni
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String SRC_LINUX = "/home/mavek/src/";
    private static final String DEST_LINUX = "/home/mavek/dest/";
    private static final String SRC_WIN = "C:\\users\\veroni\\vertx";
    private static final String DEST_WIN = "C:\\users\\veroni\\dest\\";

    private enum OS {WINDOWS, LINUX}

    private static OS ENVIRONMENT = OS.WINDOWS;

    public static void main(String[] args) throws IOException {
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

        JavaCopier jc = new JavaCopier();
        jc.copy(srcPath, destPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
//        jc.copy(srcPath, destPath);
    }
}
