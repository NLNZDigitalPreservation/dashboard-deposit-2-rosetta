package nz.govt.natlib.dashboard.common.injection;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

public class TestNFS {
    private static final String PROMPT = "NFSv41: ";

    @Disabled
    @Test
    public void testNFSList() throws IOException, InterruptedException {
        String filePath = "/media/sf_frank";

        File dir = new File(filePath);
        System.out.println("File: " + filePath + ", exist: " + dir.exists());
        File[] list = dir.listFiles();
        for (File f : list) {
            System.out.println(f.getAbsolutePath());
            if (f.getName().endsWith("AJHR")) {
                File[] subList = f.listFiles();
                Arrays.stream(subList).forEach(System.out::println);
            }
        }
    }

    @Disabled
    @Test
    public void testNFSListWithURL() throws IOException, InterruptedException {
//        System.setProperty("java.net.useSystemProxies", "true");
        URL url = new URL("file:/media/sf_frank");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        conn.setRequestProperty("Referrer Policy", "strict-origin-when-cross-origin");

        List<String> list = IOUtils.readLines(conn.getInputStream());
        list.forEach(System.out::println);
    }

    //    Y:\ndha\pre-deposit_prod\frank\magazine
    @Disabled
    @Test
    public void testNFSFileListWithURL() throws IOException, InterruptedException {
        URL url = new URL("file://Y:\\\\ndha\\pre-deposit_prod\\frank\\magazine\\");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        conn.setRequestProperty("Referrer Policy", "strict-origin-when-cross-origin");

        List<String> list = IOUtils.readLines(conn.getInputStream());
        list.forEach(System.out::println);
    }
}
