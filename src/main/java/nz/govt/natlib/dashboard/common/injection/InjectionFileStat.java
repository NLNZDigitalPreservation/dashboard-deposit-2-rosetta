package nz.govt.natlib.dashboard.common.injection;

import java.io.File;
import java.util.List;

public class InjectionFileStat {
    private long fileCount = 0;
    private long fileSize = 0;

    public void accumulate(long count, long size) {
        this.fileCount += count;
        this.fileSize += size;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


    public void stat(InjectionPathScan scanHandler, String absolutePath) {
        stat(scanHandler, new UnionPath(absolutePath));
    }

    public void stat(InjectionPathScan scanHandler, UnionPath absolutePath) {
        this.fileSize = 0;
        this.fileCount = 0;
        _stat(scanHandler, absolutePath);
    }

    private void _stat(InjectionPathScan scanHandler, UnionPath absolutePath) {
        List<UnionFile> fileList = scanHandler.listFile(absolutePath);
        if (fileList == null) {
            return;
        }
        for (UnionFile f : fileList) {
            if (f.isFile()) {
                this.accumulate(1, f.getSize());
            } else {
                this.stat(scanHandler, f.getAbsoluteUnionPath());
            }
        }
    }
}
