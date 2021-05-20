package nz.govt.natlib.dashboard.domain.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface InterfaceFlowSetting {
    Logger log = LoggerFactory.getLogger(InterfaceFlowSetting.class);
    String SCAN_MODE_NFS = "NFS";
    String SCAN_MODE_FTP = "FTP";
    String SCAN_MODE_SFTP = "SFTP";
}
