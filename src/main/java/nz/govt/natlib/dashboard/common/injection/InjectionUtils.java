package nz.govt.natlib.dashboard.common.injection;

import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.domain.entity.InterfaceFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InjectionUtils implements InterfaceFlowSetting {
    private static final Logger log = LoggerFactory.getLogger(InjectionUtils.class);

    public static boolean copyFiles(InjectionPathScan injectionPathScanClient, InjectionPathScan injectionBackupPathScanClient, File curPath) {
        int lenRootPath = injectionPathScanClient.getRootPath().length();
        String rightSubPath = curPath.getAbsolutePath().substring(lenRootPath);
        File targetLocation = new File(injectionBackupPathScanClient.getRootPath(), rightSubPath);
        if (curPath.isFile()) {
            InputStream inputStream = injectionPathScanClient.readFile(curPath.getParent(), curPath.getName());
            try {
                return injectionBackupPathScanClient.copy(inputStream, targetLocation.getAbsolutePath());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close input stream: {}", curPath.getAbsolutePath());
                }
            }
        }

        injectionBackupPathScanClient.mkdirs(targetLocation.getAbsolutePath());
        List<UnionFile> listFile = injectionPathScanClient.listFile(curPath.getAbsolutePath());
        for (UnionFile f : listFile) {
            if (!copyFiles(injectionPathScanClient, injectionBackupPathScanClient, f.getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }

    public static boolean deleteFiles(InjectionPathScan injectionPathScanClient, File curPath) {
        if (curPath.isDirectory()) {
            List<UnionFile> listFile = injectionPathScanClient.listFile(curPath.getAbsolutePath());
            for (UnionFile f : listFile) {
                if (!deleteFiles(injectionPathScanClient, f.getAbsolutePath())) {
                    return false;
                }
            }
        }

        int tryTimes = 3;
        for (int i = 0; i < tryTimes; i++) {
            boolean rstVal = injectionPathScanClient.deleteFile(curPath.getAbsolutePath());
            if (!rstVal) {
                log.error("Failed to delete: {}", curPath.getAbsolutePath());
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    log.error("Sleep failed", e);
                }
            } else {
                return true;
            }
        }

        return false;
    }

    public static InjectionPathScan createPathScanClient(EntityStorageLocation storage) {
        if (DashboardHelper.isNull(storage)) {
            return null;
        }

        InjectionPathScan injectionPathScanClient;
        if (storage.getScanMode().equals(SCAN_MODE_NFS)) {
            injectionPathScanClient = new InjectionPathScanNFS(storage.getRootPath());
        } else if (storage.getScanMode().equals(SCAN_MODE_FTP)) {
            injectionPathScanClient = new InjectionPathScanFTP(storage.getRootPath());
            try {
                ((InjectionPathScanFTP) injectionPathScanClient).init(storage);
            } catch (IOException e) {
                log.error("Failed to create FTPClient", e);
                return null;
            }
        } else {
            injectionPathScanClient = null;
        }
        return injectionPathScanClient;
    }
}
