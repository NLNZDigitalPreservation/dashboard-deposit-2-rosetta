package nz.govt.natlib.dashboard.domain.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityCommon;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class RepoAbstract {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${system.storage.path}")
    protected String systemStoragePath;

    @Autowired
    protected RepoIdGenerator idGenerator;

    protected String subStoragePath;
    protected EnumEntityKey entityKey;

    public String obj2Json(Object obj) {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    public Object json2Object(String json, Class<?> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String read(String dir, String fileName) {
        return read(new File(dir, fileName));
    }

    public String read(File fullPath) {
        if (fullPath == null || !fullPath.isFile() || !fullPath.exists()) {
            log.error("Invalid input parameter, fullPath: {}", fullPath);
            return null;
        }
        try {
            return FileUtils.readFileToString(fullPath);
        } catch (IOException e) {
            log.error("Failed to read: {}", fullPath.getAbsolutePath(), e);
            return null;
        }
    }

    public boolean save(String dir, String fileName, Object obj) {
        return save(new File(dir, fileName), obj);
    }

    public boolean save(File fullPath, Object obj) {
        if (fullPath == null || obj == null) {
            log.error("Invalie input parameter");
            return false;
        }

        if (!fullPath.getParentFile().exists() && !fullPath.getParentFile().mkdirs()) {
            log.error("Failed to make directory: {}", fullPath.getParent());
            return false;
        }

        String json = obj2Json(obj);
        try {
            FileUtils.write(fullPath, json);
            return true;
        } catch (IOException e) {
            log.error("Failed to save obj: {}", fullPath.getAbsolutePath(), e);
            return false;
        }
    }

    public boolean delete(String dir, String fileName) {
        return delete(new File(dir, fileName));
    }

    public boolean delete(File fullPath) {
        if (fullPath == null || !fullPath.exists()) {
            log.error("Invalid input parameter");
            return false;
        }
        try {
            Files.delete(fullPath.toPath());
            return true;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fullPath.getAbsolutePath(), e);
            return false;
        }
    }


    public void save(EntityCommon obj) {
        if (DashboardHelper.isNull(obj.getId())) {
            obj.setId(nextId());
        }

        String fileName = String.format("%d.json", obj.getId());
        save(this.subStoragePath, fileName, obj);
    }

    public Object getById(Long id, Class<?> clazz) {
        String fileName = String.format("%d.json", id);
        String json = read(this.subStoragePath, fileName);
        if (!StringUtils.isEmpty(json)) {
            return json2Object(json, clazz);
        }
        return null;
    }

    public void deleteById(Long id) {
        String fileName = String.format("%d.json", id);
        delete(this.subStoragePath, fileName);
    }

    public void deleteAll() {
        try {
            Files.delete(Path.of(this.subStoragePath));
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", this.subStoragePath);
        }
    }

    private Long nextId() {
        return idGenerator.nextId(entityKey);
    }
}
