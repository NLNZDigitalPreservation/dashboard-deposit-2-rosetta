package nz.govt.natlib.dashboard.common.injection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

abstract public class InjectionPathScan {
    protected static final Logger log = LoggerFactory.getLogger(InjectionPathScan.class);

    protected String rootPath;

    public String getRootPath() {
        return this.rootPath;
    }

    public InjectionPathScan(String rootPath) {
        this.rootPath = rootPath;
    }

    public abstract List<UnionFile> listRootDir();

    public abstract List<UnionFile> listFile(UnionPath absolutePath);

    public List<UnionFile> listFile(String absolutePath) {
        return listFile(new UnionPath(absolutePath));
    }

    public abstract InputStream readFile(UnionPath absolutePath, UnionPath fileName);

    public InputStream readFile(String absolutePath, String fileName) {
        return readFile(new UnionPath(absolutePath), new UnionPath(fileName));
    }

    public abstract boolean mkdirs(UnionPath absolutePath);

    public boolean mkdirs(String absolutePath) {
        return mkdirs(new UnionPath(absolutePath));
    }

    public boolean mkdir(String absolutePath) {
        return mkdirs(new UnionPath(absolutePath));
    }

    public abstract boolean rmdirs(UnionPath absolutePath);

    public boolean rmdirs(String absolutePath) {
        return rmdirs(new UnionPath(absolutePath));
    }

    public abstract boolean copy(UnionPath sourceFile, UnionPath targetFile);

    public abstract boolean copy(InputStream sourceFile, UnionPath targetFile);

    public boolean copy(InputStream sourceFile, String targetFile) {
        return copy(sourceFile, new UnionPath(targetFile));
    }

    public abstract boolean deleteFile(UnionPath f);

    public boolean deleteFile(String f) {
        return deleteFile(new UnionPath(f));
    }

    public abstract boolean exists(UnionPath f);

    public boolean exists(String f) {
        return exists(new UnionPath(f));
    }

    public abstract void disconnect();
}
