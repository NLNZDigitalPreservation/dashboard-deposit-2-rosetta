package nz.govt.natlib.dashboard.common.injection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class TestInjectionUtil extends InjectionTester {
    @Test
    public void testBackupFiles() {
        String injectionSubDirectory = "20201030_GTG_parent_grouping__Get_Growing";
        InjectionPathScan injectionPathScanClient = new InjectionPathScanNFS(injectRootPath);
        InjectionPathScan injectionBackupClient = new InjectionPathScanNFS(backupRootPath);
        File curPath = new File(injectionPathScanClient.getRootPath(), injectionSubDirectory);

        assert InjectionUtils.copyFiles(injectionPathScanClient, injectionBackupClient, curPath);

        List<UnionFile> listOriginalFiles = injectionPathScanClient.listFile(new UnionPath(injectionPathScanClient.getRootPath(), injectionSubDirectory + File.separator + "content"));
        List<UnionFile> listBackupFiles = injectionPathScanClient.listFile(new UnionPath(injectionBackupClient.getRootPath(), injectionSubDirectory + File.separator + "content"));
        assert listOriginalFiles.size() == listBackupFiles.size();

        assert InjectionUtils.deleteFiles(injectionPathScanClient, curPath);

        listOriginalFiles = injectionPathScanClient.listFile(new UnionPath(injectionPathScanClient.getRootPath(), injectionSubDirectory));
        assert listOriginalFiles == null || listOriginalFiles.size() == 0;
    }

    @Test
    public void testDeleteFiles() {
        String injectionSubDirectory = "20201030_GTG_parent_grouping__Get_Growing";
        InjectionPathScan injectionBackupClient = new InjectionPathScanNFS(injectRootPath);
        File curPath = new File(injectionBackupClient.getRootPath(), injectionSubDirectory);

        boolean rstVal = InjectionUtils.deleteFiles(injectionBackupClient, curPath);
        assert rstVal;

        List<UnionFile> listOriginalFiles = injectionBackupClient.listFile(curPath.getAbsolutePath());
        assert listOriginalFiles == null || listOriginalFiles.size() == 0;
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
