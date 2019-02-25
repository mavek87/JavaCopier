package com.matteoveroni.javacopier;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

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
    public void copySrcToNullDestFails() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        JavaCopier.copy(srcFile.toPath(), null);
    }

    @Test
    public void copyNullSrcToExistingDestFails() throws IOException {
        destFile = File.createTempFile("destFile", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        JavaCopier.copy(null, destFile.toPath());
    }

    @Test
    public void copyNullSrcToNullDestFails() throws IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_OR_DEST_NULL);

        JavaCopier.copy(null, null);
    }

    @Test
    public void copyNotExistingSrcFileToExistingDestFail() throws IOException {
        srcFile = new File("srcFile");
        destFile = createTempFileWithStandardContent("destFile");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(JavaCopier.ERROR_MSG_SRC_MUST_EXIST);

        JavaCopier.copy(srcFile.toPath(), destFile.toPath());
    }

    @Test
    public void copySingleFileToNotExistingFile() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = new File("destFile");

        CopyStatusReport copyStatusReport = JavaCopier.copy(srcFile.toPath(), destFile.toPath());

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue("Error, after copy destFile has not the same content of srcFile",
                isSameFileContent(srcFile.toPath(), destFile.toPath())
        );
        assertEquals(copyStatusReport.getFinalResult(), CopyStatusReport.FinalResult.COPY_SUCCESSFUL);
    }

    @Test
    public void copySingleFileToExistingFileWithoutReplaceCopyOptionFails() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destFile = File.createTempFile("destFile", null);

        CopyStatusReport copyStatusReport = JavaCopier.copy(srcFile.toPath(), destFile.toPath());

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertEquals(0, copyStatusReport.getNumberOfCopiedFiles());
        assertEquals(1, copyStatusReport.getNumberOfCopiesFailed());
        assertEquals(0, copyStatusReport.getCopyHistory().getCopiedFiles().size());
        assertEquals(1, copyStatusReport.getCopyHistory().getCopiesFailed().size());
        assertEquals(copyStatusReport.getFinalResult(), CopyStatusReport.FinalResult.COPY_FAILED);
    }

    @Test
    public void copySingleFileToExistingDestFileWithReplaceCopyOptionOverwrite() throws IOException {
        srcFile = createTempFileWithContent("srcFile", "src content");
        destFile = createTempFileWithContent("destFile", "dest content");

        CopyStatusReport copyStatusReport = JavaCopier.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertTrue("Error, after copy destFile has not the same content of srcFile",
                isSameFileContent(srcFile.toPath(), destFile.toPath())
        );
        assertEquals(1, copyStatusReport.getNumberOfCopiedFiles());
        assertEquals(0, copyStatusReport.getNumberOfCopiesFailed());
        assertEquals(1, copyStatusReport.getCopyHistory().getCopiedFiles().size());
        assertEquals(0, copyStatusReport.getCopyHistory().getCopiesFailed().size());
        assertEquals(copyStatusReport.getFinalResult(), CopyStatusReport.FinalResult.COPY_SUCCESSFUL);
    }

    @Test
    public void copySingleFileToDir() throws IOException {
        srcFile = createTempFileWithStandardContent("srcFile");
        destDir = new File("destDir");
        destDir.mkdir();

        CopyStatusReport copyStatusReport = JavaCopier.copy(srcFile.toPath(), destDir.toPath());

        assertTrue("Error, srcFile is not a canonical file", srcFile.isFile());
        assertTrue("Error, destDir is not a directory", destDir.isDirectory());
        assertTrue("Error, srcFile is not being copied into destDir", isFileInsideDir(srcFile, destDir));
        assertEquals(1, copyStatusReport.getNumberOfCopiedFiles());
        assertEquals(0, copyStatusReport.getNumberOfCopiesFailed());
        assertEquals(1, copyStatusReport.getCopyHistory().getCopiedFiles().size());
        assertEquals(0, copyStatusReport.getCopyHistory().getCopiesFailed().size());
        assertEquals(copyStatusReport.getFinalResult(), CopyStatusReport.FinalResult.COPY_SUCCESSFUL);
    }

    @Test
    public void copySrcDirIntoExistingEmptyFileDest() throws IOException {
        srcDir = new File("srcDir");
        srcDir.mkdir();
        File srcFile1 = new File(srcDir + File.separator + "srcFile1.pdf");
        srcFile1.createNewFile();
        File srcFile2 = new File(srcDir + File.separator + "srcFile2.zip");
        srcFile2.createNewFile();
        destFile = File.createTempFile("destFile", null);

        CopyStatusReport copyStatusReport = JavaCopier.copy(srcDir.toPath(), destFile.toPath(), new FileOutputStream(destFile));

        assertTrue("Error, srcDir is not a directory", srcDir.isDirectory());
        assertTrue("Error, destFile doesn't exist", destFile.exists());
        assertTrue("Error, destFile is not a canonical file", destFile.isFile());
        assertEquals(3, copyStatusReport.getNumberOfCopiedFiles());
        assertEquals(0, copyStatusReport.getNumberOfCopiesFailed());
        assertEquals(3, copyStatusReport.getCopyHistory().getCopiedFiles().size());
        assertEquals(0, copyStatusReport.getCopyHistory().getCopiesFailed().size());
        assertTrue(copyStatusReport.getCopyHistory().getCopiedFiles().contains(Paths.get(String.valueOf(srcDir.toPath().toAbsolutePath()))));
        assertTrue(copyStatusReport.getCopyHistory().getCopiedFiles().contains(Paths.get(String.valueOf(srcFile1.toPath().toAbsolutePath()))));
        assertTrue(copyStatusReport.getCopyHistory().getCopiedFiles().contains(Paths.get(String.valueOf(srcFile2.toPath().toAbsolutePath()))));
        assertEquals(copyStatusReport.getFinalResult(), CopyStatusReport.FinalResult.COPY_SUCCESSFUL);
        srcFile1.delete();
        srcFile2.delete();
    }

    private boolean isFileInsideDir(File file, File dir) throws IOException {
        File[] filesInDestDir = dir.listFiles();
        boolean srcIsCopied = false;
        for (File fileInDestDir : filesInDestDir) {
            if (isSameFileContent(fileInDestDir.toPath(), file.toPath())) {
                srcIsCopied = true;
                break;
            }
        }
        return srcIsCopied;
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

    private boolean isSameFileContent(Path file1, Path file2) throws IOException {
        byte[] f1 = Files.readAllBytes(file1);
        byte[] f2 = Files.readAllBytes(file2);
        return Arrays.equals(f1, f2);
    }

    private String readFileContent(Path file) throws IOException {
        byte[] encoded = Files.readAllBytes(file);
        return new String(encoded, Charset.defaultCharset());
    }
}
