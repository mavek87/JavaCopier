package com.matteoveroni.javacopier;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import static org.junit.Assert.assertFalse;

/**
 * @Author: Matteo Veroni
 */
public class JavaCopierTest {

    private final JavaCopier javaCopier = new JavaCopier();
    private File srcFile;
    private File destFile;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void copySrcToNullDestFail() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = null;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        javaCopier.copy(srcFile, destFile);
    }

    @Test
    public void copyNullSrcToDestFail() throws IOException {
        srcFile = null;
        destFile = File.createTempFile("destFile", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        javaCopier.copy(srcFile, destFile);
    }

    @Test
    public void copyNullSrcToNullDestFail() throws IOException {
        srcFile = null;
        destFile = null;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        javaCopier.copy(srcFile, destFile);
    }

    @Test
    public void copyNotCreatedSrcFileToDestFail() throws IOException {
        srcFile = new File("srcFile");
        destFile = createTempFileWithStandardContent("destFile");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_MUST_EXIST);

        javaCopier.copy(srcFile, destFile);
    }

    @Test
    public void copyFileToNotExistingFileWithReplaceCopyOption() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = new File("destFile");

        javaCopier.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue(isSameFile(srcFile.toPath(), destFile.toPath()));
        assertEquals(readFileContent(srcFile.toPath()), readFileContent(destFile.toPath()));
    }

    @Test
    public void copyFileToExistingDestFileWithReplaceCopyOptionOverwrite() throws IOException {
        srcFile = createTempFileWithContent("srcFile", "src content");
        destFile = createTempFileWithContent("destFile", "dest content");

        javaCopier.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue(isSameFile(srcFile.toPath(), destFile.toPath()));
        assertEquals(readFileContent(srcFile.toPath()), readFileContent(destFile.toPath()));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void copyFileToExistingFileWithoutReplaceCopyOptionFails() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = File.createTempFile("destFile", null);

        javaCopier.copy(srcFile, destFile);
    }

    private File createTempFileWithContent(String prefix, String fileContent) throws IOException {
        File file = File.createTempFile(prefix, null);
        Files.write(file.toPath(), fileContent.getBytes());
        return file;
    }

    private File createTempFileWithStandardContent(String prefix) throws IOException {
        String standardFileContent = "Standard content";
        return createTempFileWithContent(prefix, standardFileContent);
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
