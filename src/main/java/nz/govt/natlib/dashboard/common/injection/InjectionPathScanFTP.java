package nz.govt.natlib.dashboard.common.injection;

import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InjectionPathScanFTP extends InjectionPathScan {
    private FTPClient ftpClient;

    public InjectionPathScanFTP(String rootPath) {
        super(rootPath);
    }

    public void init(EntityStorageLocation ftpEndPoint) throws IOException {
        if (ftpEndPoint.isFtpProxyEnabled()) {
            this.ftpClient = new FTPHTTPClient(ftpEndPoint.getFtpProxyHost(), ftpEndPoint.getFtpProxyPort());
        } else {
            this.ftpClient = new FTPClient();
        }

        ftpClient.setControlEncoding("UTF-8");
        ftpClient.connect(ftpEndPoint.getFtpServer());
        int reply = ftpClient.getReplyCode();
        log.debug("Connect StatusCode: {}", reply);
        if (reply != 530 && !FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("Could not connect to FTP");
        }

        ftpClient.user(ftpEndPoint.getFtpUsername());
        ftpClient.pass(ftpEndPoint.getFtpPassword());

        if (ftpClient.getStatus() == null) {
            throw new IOException("FTP Status null");
        } else {
            log.debug("Connect StatusCode: {}", ftpClient.getStatus());
        }

//        ftpClient.enterLocalPassiveMode();
        ftpClient.enterLocalActiveMode();
        ftpClient.setFileType(2);
        ftpClient.setRemoteVerificationEnabled(false);
        ftpClient.setUseEPSVwithIPv4(true);
    }

    @Override
    public List<UnionFile> listRootDir() {
        return listFile(this.rootPath);
    }

    @Override
    public List<UnionFile> listFile(UnionPath absolutePath) {
        try {
            ftpClient.enterLocalActiveMode();
            this.ftpClient.changeWorkingDirectory(absolutePath.getAbsolutePath());
            return Arrays.stream(this.ftpClient.listFiles()).map(ftpFile -> new UnionFile(ftpFile.isDirectory(), absolutePath.getAbsolutePath(), ftpFile.getName(), ftpFile.getSize())).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("List dirs from [{}] failed", absolutePath, e);
        }
        return new ArrayList<>();
    }

    @Override
    public InputStream readFile(UnionPath absolutePath, UnionPath fileName) {
        try {
            ftpClient.enterLocalActiveMode();
            this.ftpClient.changeWorkingDirectory(absolutePath.getAbsolutePath());
            return this.ftpClient.retrieveFileStream(fileName.getAbsolutePath());
        } catch (IOException e) {
            log.error("List dirs from [{}] failed", absolutePath, e);
        }
        return null;
    }

    @Override
    public void disconnect() {
        try {
            this.ftpClient.disconnect();
        } catch (IOException e) {
            log.error("Close FTPClient", e);
        }
    }

    private boolean _mkdir(UnionPath curPath) throws IOException {
        if (DashboardHelper.isNull(curPath) || StringUtils.isEmpty(curPath.getAbsolutePath())) {
            return false;
        }

        if (ftpClient.changeWorkingDirectory(curPath.getAbsolutePath())) {
            return true;
        }

        boolean rstMakeParentDir = _mkdir(curPath.getParentPath());
        if (!rstMakeParentDir) {
            return false;
        }

        ftpClient.changeWorkingDirectory(curPath.getParent());
        ftpClient.makeDirectory(curPath.getName());

        return true;
    }

    @Override
    public boolean mkdirs(UnionPath absolutePath) {
        try {
            return _mkdir(absolutePath);
        } catch (IOException e) {
            log.error("Failed to make dirs: {}", absolutePath, e);
        }
        return false;
    }

    @Override
    public boolean copy(UnionPath sourceFile, UnionPath targetFile) {
        try {
            return copy(Files.newInputStream(sourceFile.toPath()), targetFile);
        } catch (IOException e) {
            log.error("Failed to copy file: {} -> {}", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
        }
        return false;
    }

    @Override
    public boolean copy(InputStream sourceFile, UnionPath targetFile) {
        try {
            ftpClient.enterLocalPassiveMode();
//            ftpClient.enterRemotePassiveMode();
            return ftpClient.storeFile(targetFile.getAbsolutePath(), sourceFile);
        } catch (IOException e) {
            log.error("Failed to copy file: InputStream -> {}", targetFile.getAbsolutePath(), e);
        }
        return false;
    }

    @Override
    public boolean deleteFile(UnionPath f) {
        try {
            return ftpClient.deleteFile(f.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to delete file: {}", f.getAbsolutePath());
        }
        return false;
    }

    @Override
    public boolean exists(UnionPath f) {
        try {
            String size = ftpClient.getSize(f.getAbsolutePath());
            return !DashboardHelper.isNull(size);
        } catch (IOException e) {
            log.error("Failed to get size: {}", f.getAbsolutePath());
            return false;
        }
    }

    @Override
    public boolean rmdirs(UnionPath absolutePath) {
        try {
            return _rmdir(absolutePath);
        } catch (IOException e) {
            log.error("Failed to delete: {}", absolutePath, e);
            return false;
        }
    }


    private boolean _rmdir(UnionPath curPath) throws IOException {
        if (!ftpClient.changeWorkingDirectory(curPath.getAbsolutePath())) {
            return false;
        }

        boolean previousHandleResult = true;

        ftpClient.enterLocalActiveMode();

        FTPFile[] directories = ftpClient.listDirectories();
        if (directories != null) {
            for (FTPFile d : directories) {
                if (!_rmdir(new UnionPath(d.toString()))) {
                    previousHandleResult = false;
                }
            }
        }

        FTPFile[] files = ftpClient.listFiles();
        if (files != null) {
            for (FTPFile f : files) {
                if (!ftpClient.deleteFile(f.toString())) {
                    previousHandleResult = false;
                }
            }
        }

        if (!previousHandleResult) {
            log.error("Failed to delete: {}", curPath);
            return false;
        }

        return ftpClient.removeDirectory(curPath.getAbsolutePath());
    }
}
