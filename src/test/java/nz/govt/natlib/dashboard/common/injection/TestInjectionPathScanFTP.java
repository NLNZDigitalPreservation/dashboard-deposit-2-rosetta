package nz.govt.natlib.dashboard.common.injection;

import nz.govt.natlib.dashboard.common.metadata.EnumStorageMode;
import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;

public class TestInjectionPathScanFTP extends InjectionPathScanTester {
    private static final String PATH_SEPARATOR = "/";

    @BeforeAll
    public static void init() {
        String subFolder = DashboardHelper.getUid();
        rootPath = String.format("/home/dashboard/deposit/%s", subFolder);
        injectRootPath = String.format("/home/dashboard/deposit/%s/magazine", subFolder);
        backupRootPath = String.format("/home/dashboard/deposit/%s/magazine_backup", subFolder);

        EntityStorageLocation injectLocationFTP = new EntityStorageLocation();
        injectLocationFTP.setScanMode(EnumStorageMode.FTP.toString());
        injectLocationFTP.setRootPath(injectRootPath);
        injectLocationFTP.setFtpServer("localhost");
        injectLocationFTP.setFtpPort(21);
        injectLocationFTP.setFtpUsername("dashboard");
        injectLocationFTP.setFtpPassword("deposit@111");

        injectPathScanClient = InjectionUtils.createPathScanClient(injectLocationFTP);
        injectLocationFTP.setRootPath(backupRootPath);

        backupPathScanClient = InjectionUtils.createPathScanClient(injectLocationFTP);
    }

    @AfterAll
    public static void close() {
        injectPathScanClient.disconnect();
        backupPathScanClient.disconnect();
    }

    @Ignore
//    @Test
    public void testRead() throws IOException {
        super.testReadInternal();
    }

    @Ignore
//    @Test
    public void testNFSCopyFiles() throws IOException {
        super.testNFSCopyFilesInternal();
    }

    @Ignore
//    @Test
    public void testDelete() throws IOException {
        super.testDeleteInternal();
    }

    @Ignore
//    @Test
    public void testList() {
        super.testListDir();
    }

//    @Test
    public void testChangeDirAndMakeDirs() {
        String subFolder = injectRootPath + PATH_SEPARATOR + DashboardHelper.getUid();

        assert injectPathScanClient.mkdirs(subFolder);

        assert injectPathScanClient.rmdirs(subFolder);
    }

    @AfterEach
    public void clearSubFolders() {
        injectPathScanClient.rmdirs(rootPath);
    }

    @BeforeEach
    public void initSubFolder() {
        clearSubFolders();

        String targetDirStream = injectRootPath + PATH_SEPARATOR + subFolderName + PATH_SEPARATOR + streamPath;
        injectPathScanClient.mkdirs(targetDirStream);

        //Put files
        try {
            ByteArrayInputStream buf = new ByteArrayInputStream(RandomStringUtils.random(testFileLength_1, true, true).getBytes());
            injectPathScanClient.copy(buf, targetDirStream + PATH_SEPARATOR + testFileName_1);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        try {
            ByteArrayInputStream buf = new ByteArrayInputStream(RandomStringUtils.random(testFileLength_2, true, true).getBytes());
            injectPathScanClient.copy(buf, targetDirStream + PATH_SEPARATOR + testFileName_2);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        //Put mets.xml
        Resource resource = new ClassPathResource(metsXmlFileName);
        try {
            InputStream inputStream = resource.getInputStream();
            injectPathScanClient.copy(inputStream, UnionPath.of(targetDirStream).getParent() + PATH_SEPARATOR + metsXmlFileName);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        //Put the injection finished file
        try {
            File tempFile = File.createTempFile(completedFileName, ".done");
            InputStream inputStream = new FileInputStream(tempFile);
            injectPathScanClient.copy(inputStream, injectRootPath + PATH_SEPARATOR + subFolderName + PATH_SEPARATOR + completedFileName);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
