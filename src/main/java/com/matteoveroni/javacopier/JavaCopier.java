package com.matteoveroni.javacopier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Matteo Veroni
 */
public class JavaCopier {

    public void copy(Path src, Path dest) throws IOException {
        Files.walkFileTree(src, new CopyFileVisitor(src, dest));
    }

    public void copy(File src, File dest) throws IOException {
       this.copy(src.toPath(), dest.toPath());
    }

}
