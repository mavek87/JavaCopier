package com.matteoveroni.javacopier;

import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author Matteo Veroni
 */
public class JavaCopier {

    public void copy(Path src, Path dest) {

    }

    public void copy(File src, File dest) {
       this.copy(src.toPath(), dest.toPath());
    }

}
