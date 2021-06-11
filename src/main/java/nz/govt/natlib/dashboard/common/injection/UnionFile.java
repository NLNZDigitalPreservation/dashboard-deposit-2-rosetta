package nz.govt.natlib.dashboard.common.injection;

import java.io.File;

public class UnionFile {
    private boolean pathFlag;
    private String path;
    private String name;
    private long size;

    public UnionFile() {

    }

    public UnionFile(boolean pathFlag, String path, String name, long size) {
        this.pathFlag = pathFlag;
        this.path = path;
        this.name = name;
        this.size = size;
    }

    public boolean isPath() {
        return pathFlag;
    }

    public boolean isFile() {
        return !pathFlag;
    }

    public void setPathFlag(boolean pathFlag) {
        this.pathFlag = pathFlag;
    }

    public File getAbsolutePath() {
        return new File(path, name);
    }

    public UnionPath getAbsoluteUnionPath() {
        return new UnionPath(path + File.separator + name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
