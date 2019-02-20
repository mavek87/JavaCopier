package com.matteoveroni.javacopier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Matteo Veroni
 */
public class Main {

    private static final String SRC_LINUX = "/home/mavek/src/a.txt";
    private static final String DEST_LINUX = "/home/mavek/dest/a.txt";

    public static void main(String[] args) throws IOException {
        Path srcPath = Paths.get(SRC_LINUX);
        Path destPath = Paths.get(DEST_LINUX);
        JavaCopier jc = new JavaCopier();
//        jc.copy(srcPath, destPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        jc.copy(srcPath, destPath);
    }
}
