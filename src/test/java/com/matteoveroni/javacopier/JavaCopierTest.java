package com.matteoveroni.javacopier;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @Author: Matteo Veroni
 */
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
    public void copyFileToNotExistingFileWithReplaceCopyOption() throws IOException {
        srcFile = createTempFileWithContent("srcFile");
        destFile = new File("destFile");

        javaCopier.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue(isSameFile(srcFile.toPath(), destFile.toPath()));
        assertEquals(readFileContent(srcFile.toPath()), readFileContent(destFile.toPath()));
    }

    @Test
    public void copyFileToExistingFileWithReplaceCopyOption() throws IOException {
        srcFile = createTempFileWithContent("srcFile");
        destFile = File.createTempFile("destFile", null);

        javaCopier.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue(isSameFile(srcFile.toPath(), destFile.toPath()));
        assertEquals(readFileContent(srcFile.toPath()), readFileContent(destFile.toPath()));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void copyFileToExistingFileWithoutReplaceCopyOptionFails() throws IOException {
        srcFile = createTempFileWithContent("srcFile");
        destFile = File.createTempFile("destFile", null);

        javaCopier.copy(srcFile, destFile);
    }

    private File createTempFileWithContent(String prefix) throws IOException {
        File file = File.createTempFile(prefix, null);
        String fileContent = "Standard content";
        Files.write(file.toPath(), fileContent.getBytes());
        return file;
    }

    private boolean isSameFile(Path file1, Path file2) throws IOException {
        byte[] f1 = Files.readAllBytes(file1);
        byte[] f2 = Files.readAllBytes(file2);
        return Arrays.equals(f1, f2);
    }

    private String readFileContent(Path file) throws IOException {
        byte[] encoded = Files.readAllBytes(file);
        return new String(encoded, Charset.defaultCharset());
    }


}
