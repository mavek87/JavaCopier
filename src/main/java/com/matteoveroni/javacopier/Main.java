package com.matteoveroni.javacopier;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Matteo Veroni
 */
public class Main {

    public static void main(String[] args) {
        String src = "/home/mavek/src";
        String dest = "/home/mavek/dest";
        Path srcPath = Paths.get(src);
        Path destPath = Paths.get(dest);
        JavaCopier c = new JavaCopier();

    }
}
