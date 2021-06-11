package nz.govt.natlib.dashboard.common.injection;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public class InjectionTester {

    public static final String streamPath = "content/streams";
    public static final String completedFileName = "ready-for-ingestion-FOLDER-COMPLETED";

    public static final String subFolderName = "20201029_TVG_parent_grouping__TV_Guide";
    public static final String metsXmlFileName = "mets.xml";

    public static final String testFileName_1 = "test_item_1.txt";
    public static final int testFileLength_1 = 537;

    public static final String testFileName_2 = "test_item_2.txt";
    public static final int testFileLength_2 = 219;

    public static String rootPath = Files.createTempDir().getAbsolutePath();
    //    protected static  String storagePath = new File(rootDir, "storage").getAbsolutePath();
    public static String injectRootPath = new File(rootPath, "magazine").getAbsolutePath();
    public static String backupRootPath = new File(rootPath, "magazine_backup").getAbsolutePath();

    public void clearSubFolders() {
        File fRootPath = new File(injectRootPath);
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

        File targetDir = new File(injectRootPath, subFolderName);
        if (!targetDir.exists()) {
            boolean rstVal = targetDir.mkdirs();
            if (!rstVal) {
                System.out.println(targetDir + ": " + ExceptionUtils.getStackTrace(new Exception()));
            }
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

        //Put the injection finished file
        try {
            FileUtils.write(new File(new File(injectRootPath, subFolderName), completedFileName), "");
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
