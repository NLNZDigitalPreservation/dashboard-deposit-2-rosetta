package nz.govt.natlib.dashboard.common.injection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class InjectionPathScanTester extends InjectionTester {
    protected static InjectionPathScan injectPathScanClient;
    protected static InjectionPathScan backupPathScanClient;

    public void testReadInternal() throws IOException {
        File directory = new File(injectPathScanClient.getRootPath(), subFolderName);
        InputStream fileInputStream = injectPathScanClient.readFile(directory.getAbsolutePath(), completedFileName);

        File tempFile = File.createTempFile(completedFileName, ".done");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        Files.copy(fileInputStream, tempFile.toPath());
        fileInputStream.close();

        assert tempFile.exists();

        tempFile.delete();
    }

    public void testNFSCopyFilesInternal() throws IOException {
        File directory = new File(injectPathScanClient.getRootPath(), subFolderName);
        File directoryBackup = new File(backupPathScanClient.getRootPath(), subFolderName);

        InputStream fileInputStream = injectPathScanClient.readFile(directory.getAbsolutePath(), "content/" + metsXmlFileName);

        boolean rstMakeBackupDir = backupPathScanClient.mkdirs(directoryBackup.getAbsolutePath());
        assert rstMakeBackupDir;

        File targetFile = new File(directoryBackup, metsXmlFileName);
        if (backupPathScanClient.exists(targetFile.getAbsolutePath())) {
            backupPathScanClient.deleteFile(targetFile.getAbsolutePath());
        }

        boolean rstCopy = backupPathScanClient.copy(fileInputStream, targetFile.getAbsolutePath());
        assert rstCopy;

        fileInputStream.close();
    }

    public void testDeleteInternal() throws IOException {
        File directory = new File(injectPathScanClient.getRootPath(), subFolderName);
        String fileName = "test-delete";

        File tempFile = File.createTempFile(fileName, ".txt");
        Files.writeString(tempFile.toPath(), "This is a test");

        boolean rstCopy = injectPathScanClient.copy(UnionPath.of(tempFile), new UnionPath(directory, fileName));
        assert rstCopy;

        boolean rstDeleteFile = injectPathScanClient.deleteFile(new UnionPath(directory, fileName));
        assert rstDeleteFile;

        tempFile.deleteOnExit();
    }

    public void testListDir() {
        List<UnionFile> rootDirs = injectPathScanClient.listRootDir();
        assert rootDirs != null;
        assert rootDirs.size() > 0;

        List<UnionFile> subDirs = injectPathScanClient.listFile(new UnionPath(injectRootPath, subFolderName));
        assert subDirs != null;
        assert subDirs.size() > 0;
    }
}
