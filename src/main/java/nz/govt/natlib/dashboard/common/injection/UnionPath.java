package nz.govt.natlib.dashboard.common.injection;

import nz.govt.natlib.dashboard.util.DashboardHelper;

import java.io.File;
import java.nio.file.Path;

public class UnionPath {
    private final String path;

    public UnionPath(String path) {
        this.path = path;
    }

    public UnionPath(Path path) {
        this.path = path.toString();
    }

    public UnionPath(File path) {
        this.path = path.getAbsolutePath();
    }

    public UnionPath(String prefix, String suffix) {
        this.path = prefix + File.separator + suffix;
    }

    public UnionPath(File prefix, String suffix) {
        this.path = prefix.getAbsolutePath() + File.separator + suffix;
    }

    public static UnionPath of(String path) {
        return new UnionPath(path);
    }

    public static UnionPath of(Path path) {
        return new UnionPath(path);
    }

    public static UnionPath of(File path) {
        return new UnionPath(path);
    }

    public File toFile() {
        return new File(path);
    }

    public Path toPath() {
        return Path.of(path);
    }

    public String getAbsolutePath() {
        return path;
    }

    public String getParent() {
        if (DashboardHelper.isEmpty(path)) {
            return path;
        }

        String s = path.replace('\\', '/');

        int idxLastSeparator = s.lastIndexOf('/');
        if (idxLastSeparator <= 0) {
            return path;
        }

        return s.substring(0, idxLastSeparator);
    }

    public UnionPath getParentPath() {
        return new UnionPath(getParent());
    }

    public String getName() {
        if (DashboardHelper.isEmpty(path)) {
            return path;
        }

        String s = path.replace('\\', '/');

        int idxLastSeparator = s.lastIndexOf('/');
        if (idxLastSeparator <= 0) {
            return path;
        }

        return s.substring(idxLastSeparator + 1);
    }

    public String toString() {
        return path;
    }
}
