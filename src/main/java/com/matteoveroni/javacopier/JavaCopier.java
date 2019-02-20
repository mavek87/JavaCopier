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

    public void copy(Path src, Path dest, boolean preserveAttributes) throws IOException {
        Files.walkFileTree(src, new CopyFileVisitor(src, dest, preserveAttributes));
    }

    public void copy(File src, File dest, boolean preserveAttributes) throws IOException {
        this.copy(src.toPath(), dest.toPath(), preserveAttributes);
    }

}
