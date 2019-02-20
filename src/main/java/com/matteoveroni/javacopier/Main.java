package com.matteoveroni.javacopier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Matteo Veroni
 */
public class Main {

    private static final String SRC_LINUX = "/home/mavek/src";
    private static final String DEST_LINUX = "/home/mavek/dest";

    public static void main(String[] args) throws IOException {
        Path srcPath = Paths.get(SRC_LINUX);
        Path destPath = Paths.get(DEST_LINUX);
        JavaCopier c = new JavaCopier();
        c.copy(srcPath, destPath, true);
    }
}
