package nz.govt.natlib.dashboard.common.injection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestInjectionPathScanNFS extends InjectionPathScanTester {
    @BeforeAll
    public static void init() {
        injectPathScanClient = new InjectionPathScanNFS(injectRootPath);
        backupPathScanClient = new InjectionPathScanNFS(backupRootPath);
    }

    @Test
    public void testRead() throws IOException {
        super.testReadInternal();
    }

    @Test
    public void testNFSCopyFiles() throws IOException {
        super.testNFSCopyFilesInternal();
    }

    @Test
    public void testDelete() throws IOException {
        super.testDeleteInternal();
    }

    @Test
    public void testList() {
        super.testListDir();
    }

    @AfterEach
    public void clearSubFolders() {
        super.clearSubFolders();
    }

    @BeforeEach
    public void initSubFolder() {
        super.initSubFolder();
    }
}
