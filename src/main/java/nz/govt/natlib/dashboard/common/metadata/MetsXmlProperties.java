package nz.govt.natlib.dashboard.common.metadata;

import java.util.ArrayList;
import java.util.List;

public class MetsXmlProperties {
    private String title;
    private String cmdId;
    private String objectIdentifierType;
    private String objectIdentifierValue;
    private String recordId;
    private String system;
    private List<GeneralFileCharacters> listFiles = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCmdId() {
        return cmdId;
    }

    public void setCmdId(String cmdId) {
        this.cmdId = cmdId;
    }

    public String getObjectIdentifierType() {
        return objectIdentifierType;
    }

    public void setObjectIdentifierType(String objectIdentifierType) {
        this.objectIdentifierType = objectIdentifierType;
    }

    public String getObjectIdentifierValue() {
        return objectIdentifierValue;
    }

    public void setObjectIdentifierValue(String objectIdentifierValue) {
        this.objectIdentifierValue = objectIdentifierValue;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public List<GeneralFileCharacters> getListFiles() {
        return listFiles;
    }

    public void setListFiles(List<GeneralFileCharacters> listFiles) {
        this.listFiles = listFiles;
    }

    public void newGeneralFileCharacters() {
        listFiles.add(new GeneralFileCharacters());
    }

    public GeneralFileCharacters getLastGeneralFileCharacters() {
        if (listFiles.size() > 0) {
            return listFiles.get(listFiles.size() - 1);
        } else {
            return null;
        }
    }

    public static class GeneralFileCharacters {
        private String label;
        private String fileMIMEType;
        private String fileCreatedDate;
        private String fileModificationDate;
        private String fileOriginalName;
        private String fileOriginalPath;
        private String fileSizeBytes;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getFileMIMEType() {
            return fileMIMEType;
        }

        public void setFileMIMEType(String fileMIMEType) {
            this.fileMIMEType = fileMIMEType;
        }

        public String getFileCreatedDate() {
            return fileCreatedDate;
        }

        public void setFileCreatedDate(String fileCreatedDate) {
            this.fileCreatedDate = fileCreatedDate;
        }

        public String getFileModificationDate() {
            return fileModificationDate;
        }

        public void setFileModificationDate(String fileModificationDate) {
            this.fileModificationDate = fileModificationDate;
        }

        public String getFileOriginalName() {
            return fileOriginalName;
        }

        public void setFileOriginalName(String fileOriginalName) {
            this.fileOriginalName = fileOriginalName;
        }

        public String getFileOriginalPath() {
            return fileOriginalPath;
        }

        public void setFileOriginalPath(String fileOriginalPath) {
            this.fileOriginalPath = fileOriginalPath;
        }

        public String getFileSizeBytes() {
            return fileSizeBytes;
        }

        public void setFileSizeBytes(String fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
        }
    }
}
