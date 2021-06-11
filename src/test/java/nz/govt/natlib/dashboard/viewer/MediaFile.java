package nz.govt.natlib.dashboard.viewer;

import java.util.Comparator;

/*
 * VideoFile class is used to handle the files within the IE
 *
 * @author Mathachan Kulathinal - 20.Apr.2015
 */
public class MediaFile {

    private String pid;
    private String label;
    private int fileOrder; // File Sequence Number
    private String fileType; // File Type: mp3,mp4 etc
    private String imagePath;

    public MediaFile(String pid, String label, int sequence, String type) {
        this.pid = pid;
        this.label = label;
        this.fileOrder = sequence;
        this.fileType = type;
        this.imagePath = "";
    }
    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @return the fileOrder
     */
    public int getFileOrder() {
        return fileOrder;
    }
    /**
     * @param fileOrder the fileOrder to set
     */
    public void setFileOrder(int fileOrder) {
        this.fileOrder = fileOrder;
    }
    /**
     * @return the fileType
     */
    public String getFileType() {
        return fileType;
    }
    /**
     * @param fileType the fileType to set
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Implement the comparator method to handle sorting by file Sequence Number
    public static Comparator<MediaFile> MediaFilesBySequenceNumber = new Comparator<MediaFile>() {
        public int compare(MediaFile mf1, MediaFile mf2) {
            int s1 = mf1.getFileOrder();
            int s2 = mf2.getFileOrder();
            // Ascending order
            return s1-s2;
        }
    };
}