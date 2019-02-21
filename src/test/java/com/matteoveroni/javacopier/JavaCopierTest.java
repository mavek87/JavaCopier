package com.matteoveroni.javacopier;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class JavaCopierTest {

    private final JavaCopier javaCopier = new JavaCopier();
    private File srcFile;
    private File destFile;

    @After
    public void tearDown() {
        if (srcFile != null && srcFile.exists()) {
            srcFile.delete();
        }
        if (destFile != null && destFile.exists()) {
            destFile.delete();
        }
    }

    @Test
    public void copyFileToFile() throws IOException {
        srcFile = File.createTempFile("srcFile", null);
        destFile = File.createTempFile("destFile", null);

        javaCopier.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
    }

}
