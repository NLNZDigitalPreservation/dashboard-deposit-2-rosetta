package nz.govt.natlib.dashboard.common;

import com.google.common.io.Files;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import nz.govt.natlib.dashboard.domain.service.GlobalSettingService;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;

public class BasicTester {
    protected static final File testDir = Files.createTempDir();
    protected static final String storagePath = new File(testDir, "storage").getAbsolutePath();
    protected static final String scanRootPath = new File(testDir, "magazine").getAbsolutePath();
    protected static final String streamPath = "content/streams";
    protected static final String completedFileName = "ready-for-ingestion-FOLDER-COMPLETED";

    protected static final String subFolderName = "20201029_TVG_parent_grouping__TV_Guide";
    protected static final String metsXmlFileName = "mets.xml";

    protected static final String testFileName_1 = "test_item_1.txt";
    protected static final int testFileLength_1 = 537;

    protected static final String testFileName_2 = "test_item_2.txt";
    protected static final int testFileLength_2 = 219;

    protected static final String sipId = "12345";
    protected static final String pdsHandle = DashboardHelper.getUid();


    protected static RepoIdGenerator repoIdGenerator;
    protected static RepoFlowSetting repoFlowSetting;
    protected static RepoStorageLocation repoStorageLocation;
    protected static RepoDepositJobActive repoDepositJobActive;
    protected static RepoDepositJobHistory repoDepositJobHistory;
    protected static RepoGlobalSetting repoGlobalSetting;
    protected static RepoWhiteList repoWhiteList;

    protected static RosettaWebService rosettaWebService;
    protected static DepositJobService depositJobService;
    protected static GlobalSettingService globalSettingService;

    public static void init() throws IOException {
        //Initial services
        repoIdGenerator = new RepoIdGenerator();
        ReflectionTestUtils.setField(repoIdGenerator, "systemStoragePath", storagePath);
        repoIdGenerator.init();

        repoFlowSetting = new RepoFlowSetting();
        ReflectionTestUtils.setField(repoFlowSetting, "systemStoragePath", storagePath);
        ReflectionTestUtils.setField(repoFlowSetting, "idGenerator", repoIdGenerator);
        repoFlowSetting.init();

        repoStorageLocation = new RepoStorageLocation();
        ReflectionTestUtils.setField(repoStorageLocation, "systemStoragePath", storagePath);
        ReflectionTestUtils.setField(repoStorageLocation, "idGenerator", repoIdGenerator);
        repoStorageLocation.init();

        repoDepositJobActive = new RepoDepositJobActive();
        ReflectionTestUtils.setField(repoDepositJobActive, "systemStoragePath", storagePath);
        ReflectionTestUtils.setField(repoDepositJobActive, "idGenerator", repoIdGenerator);
        repoDepositJobActive.init();

        repoDepositJobHistory = new RepoDepositJobHistory();
        ReflectionTestUtils.setField(repoDepositJobHistory, "systemStoragePath", storagePath);
        repoDepositJobHistory.init();

        repoGlobalSetting = new RepoGlobalSetting();
        ReflectionTestUtils.setField(repoGlobalSetting, "systemStoragePath", storagePath);
        repoGlobalSetting.init();

        repoWhiteList = new RepoWhiteList();
        ReflectionTestUtils.setField(repoWhiteList, "systemStoragePath", storagePath);
        ReflectionTestUtils.setField(repoWhiteList, "idGenerator", repoIdGenerator);
        repoWhiteList.init();

        EntityGlobalSetting entityGlobalSetting = new EntityGlobalSetting();
        entityGlobalSetting.setDepositUserInstitute("INS01");
        entityGlobalSetting.setDepositUserName("annoy");
        entityGlobalSetting.setDepositUserPassword("***");
        repoGlobalSetting.save(entityGlobalSetting);

        rosettaWebService = mock(RosettaWebService.class);

        depositJobService = new DepositJobService();
        ReflectionTestUtils.setField(depositJobService, "rosettaWebService", rosettaWebService);
//        ReflectionTestUtils.setField(depositJobService, "globalSettingService", globalSettingService);
        ReflectionTestUtils.setField(depositJobService, "repoJobActive", repoDepositJobActive);
        ReflectionTestUtils.setField(depositJobService, "repoJobHistory", repoDepositJobHistory);
        ReflectionTestUtils.setField(depositJobService, "repoFlowSetting", repoFlowSetting);
        ReflectionTestUtils.setField(depositJobService, "repoStorageLocation", repoStorageLocation);
//        ReflectionTestUtils.setField(depositJobService, "depositJobActiveTimeSlotDays", 1L);

        globalSettingService = new GlobalSettingService();
        ReflectionTestUtils.setField(globalSettingService, "repoGlobalSetting", repoGlobalSetting);
        ReflectionTestUtils.setField(globalSettingService, "repoWhiteList", repoWhiteList);
        ReflectionTestUtils.setField(globalSettingService, "rosettaWebService", rosettaWebService);
    }

    @AfterAll
    public static void clearTempFiles() {
        repoIdGenerator.close();
        repoFlowSetting.close();
        repoStorageLocation.close();
        repoDepositJobActive.close();
        repoDepositJobHistory.close();
        repoGlobalSetting.close();
        repoWhiteList.close();
        try {
            FileUtils.cleanDirectory(testDir);
            testDir.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    public void clearSubFolders() {
        File fRootPath = new File(scanRootPath);
        if (!fRootPath.exists()) {
            return;
        }

        try {
            FileUtils.cleanDirectory(fRootPath);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    public void initSubFolder() {
        clearSubFolders();

        File targetDir = new File(scanRootPath, subFolderName);
        if (!targetDir.exists()) {
            boolean rstVal = targetDir.mkdirs();
            assert rstVal;
        } else {
            try {
                FileUtils.cleanDirectory(targetDir);
            } catch (IOException e) {
                e.printStackTrace();
                assert false;
            }
        }

        File targetDirStream = new File(targetDir, streamPath);
        if (!targetDirStream.exists()) {
            boolean rstVal = targetDirStream.mkdirs();
            assert rstVal;
        }

        //Put files
        File f1 = new File(targetDirStream, testFileName_1);
        try {
            FileUtils.write(f1, RandomStringUtils.random(testFileLength_1, true, true));
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        File f2 = new File(targetDirStream, testFileName_2);
        try {
            FileUtils.write(f2, RandomStringUtils.random(testFileLength_2, true, true));
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        //Put mets.xml
        Resource resource = new ClassPathResource(metsXmlFileName);
        File metsXml = new File(targetDirStream.getParent(), metsXmlFileName);
        try {
            FileUtils.copyFile(resource.getFile(), metsXml);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    public void addReadyForIngestionFile() {
        try {
            FileUtils.write(new File(new File(scanRootPath, subFolderName), completedFileName), "");
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
