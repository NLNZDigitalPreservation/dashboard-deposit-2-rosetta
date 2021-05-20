package nz.govt.natlib.dashboard.common.injection;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InjectionPathScanNFS extends InjectionPathScan {
    public InjectionPathScanNFS(String rootPath) {
        super(rootPath);
    }

    @Override
    public List<UnionFile> listRootDir() {
        return listFile(this.rootPath);
    }

    @Override
    public List<UnionFile> listFile(UnionPath absolutePath) {
        List<UnionFile> retVal = new ArrayList<>();
        File[] files = absolutePath.toFile().listFiles();
        if (files == null) {
            return retVal;
        }
        retVal = Arrays.stream(files).map(f -> {
            return new UnionFile(f.isDirectory(), absolutePath.getAbsolutePath(), f.getName(), f.length());
        }).collect(Collectors.toList());
        return retVal;
    }

    @Override
    public InputStream readFile(UnionPath absolutePath, UnionPath fileName) {
        File f = new File(absolutePath.getAbsolutePath(), fileName.getAbsolutePath());
        if (f.exists() && f.isFile()) {
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean mkdirs(UnionPath absolutePath) {
        File f = absolutePath.toFile();
        if (!f.exists()) {
            return f.mkdirs();
        } else {
            return true;
        }
    }

    @Override
    public boolean copy(UnionPath sourceFile, UnionPath targetFile) {
        try {
            Files.copy(sourceFile.toPath(), targetFile.toPath());
            return true;
        } catch (IOException e) {
            log.error("Failed to copy file: {} -> {}", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
        }
        return false;
    }

    @Override
    public boolean copy(InputStream sourceFile, UnionPath targetFile) {
        try {
            Files.copy(sourceFile, targetFile.toPath());
            return true;
        } catch (IOException e) {
            log.error("Failed to copy file: InputStream -> {}", targetFile.getAbsolutePath());
        }
        return false;
    }

    @Override
    public boolean deleteFile(UnionPath f) {
        try {
            FileDeleteStrategy.FORCE.delete(f.toFile());
            return true;
        } catch (Throwable e) {
            log.error("Failed to delete file: {}", f.getAbsolutePath(), e);
            return false;
        }
    }

    @Override
    public boolean exists(UnionPath f) {
        return f != null && f.toFile().exists();
    }

    @Override
    public boolean rmdirs(UnionPath absolutePath) {
        try {
            FileUtils.cleanDirectory(absolutePath.toFile());
            return true;
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", absolutePath, e);
            return false;
        }
    }
}
