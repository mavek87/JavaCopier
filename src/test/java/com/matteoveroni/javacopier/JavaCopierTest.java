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

/**
 * @author Matteo Veroni
 */
public class JavaCopierTest {

    private File srcFile;
    private File destFile;
    private File srcDir;
    private File destDir;

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
        if (srcDir != null && srcDir.exists()) {
            srcDir.delete();
        }
        if (destDir != null && destDir.exists()) {
            destDir.delete();
        }
    }

    @Test
    public void copySrcToNullDestFail() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = null;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        JavaCopier.copy(srcFile.toPath(), destFile.toPath());
    }

    @Test
    public void copyNullSrcToDestFail() throws IOException {
        srcFile = null;
        destFile = File.createTempFile("destFile", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        JavaCopier.copy(srcFile.toPath(), destFile.toPath());
    }

    @Test
    public void copyNullSrcToNullDestFail() throws IOException {
        srcFile = null;
        destFile = null;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        JavaCopier.copy(srcFile.toPath(), destFile.toPath());
    }

    @Test
    public void copyNotCreatedSrcFileToDestFail() throws IOException {
        srcFile = new File("srcFile");
        destFile = createTempFileWithStandardContent("destFile");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_MUST_EXIST);

        JavaCopier.copy(srcFile.toPath(), destFile.toPath());
    }

    @Test
    public void copySrcDirIntoFileDestFail() throws IOException {
        srcDir = new File("srcDir");
        srcDir.mkdir();
        destFile = createTempFileWithStandardContent("destFile");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_CANNOT_COPY_DIR_INTO_FILE);

        JavaCopier.copy(srcDir.toPath(), destFile.toPath());
    }

    @Test
    public void copyFileToNotExistingFileWithReplaceCopyOption() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = new File("destFile");

        JavaCopier.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue(isSameFile(srcFile.toPath(), destFile.toPath()));
    }

    @Test
    public void copyFileToExistingDestFileWithReplaceCopyOptionOverwrite() throws IOException {
        srcFile = createTempFileWithContent("srcFile", "src content");
        destFile = createTempFileWithContent("destFile", "dest content");

        JavaCopier.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue(isSameFile(srcFile.toPath(), destFile.toPath()));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void copyFileToExistingFileWithoutReplaceCopyOptionFails() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = File.createTempFile("destFile", null);

        JavaCopier.copy(srcFile.toPath(), destFile.toPath());
    }

    @Test
    public void copyFileToDir() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destDir = new File("destDir");
        destDir.mkdir();

        JavaCopier.copy(srcFile.toPath(), destDir.toPath());

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destDir is not a directory", destDir.isDirectory());
        assertTrue("Error, srcFile is not being copied into destDir", isFileInsideDir(srcFile, destDir));
    }

    private boolean isFileInsideDir(File file, File dir) throws IOException {
        File[] filesInDestDir = dir.listFiles();
        boolean srcIsCopied = false;
        for (File fileInDestDir : filesInDestDir) {
            if (isSameFile(fileInDestDir.toPath(), file.toPath())) {
                srcIsCopied = true;
                break;
            }
        }
        return srcIsCopied;
    }

    private File createTempFileWithContent(String prefix, String fileContent) throws IOException {
        File file = File.createTempFile(prefix, null);
        Files.write(file.toPath(), fileContent.getBytes());
        // TODO: verify this code
        file.deleteOnExit();
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
