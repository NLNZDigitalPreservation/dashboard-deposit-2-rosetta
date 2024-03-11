package nz.govt.natlib.dashboard.util;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CustomizedPdsClient {
    private static CustomizedPdsClient pds = null;
    private String baseUrl = null;
    private boolean isTestMode = false;
    private static final String PDS_HANDLE = "&pds_handle=";
    private static final String AUTHENTICATION_ERROR = "Failed to authenticate user.";

    private CustomizedPdsClient() {
    }

    public static CustomizedPdsClient getInstance() {
        if (pds != null) {
            return pds;
        } else {
            pds = new CustomizedPdsClient();
            return pds;
        }
    }

    public void init(String baseUrl, boolean isTestMode) {
        this.baseUrl = baseUrl;
        this.isTestMode = isTestMode;
    }

    public String login(String institution, String user, String password) throws Exception {
        if (this.isTestMode) {
            return "241200811372143992420081372111";
        } else {
            StringBuffer dataUrl = new StringBuffer();
            dataUrl.append("func=login");
            dataUrl.append("&institute=").append(URLEncoder.encode(institution, "UTF-8"));
            dataUrl.append("&bor_id=").append(URLEncoder.encode(user, "UTF-8"));
            dataUrl.append("&bor_verification=").append(URLEncoder.encode(password, "UTF-8"));
            URL url = new URL(this.baseUrl);
            StringBuffer content = this.getUrlContent(url, dataUrl);
            return this.extractPdsHandle(content.toString());
        }
    }

    public String logout(String pdsHandle) throws Exception {
        StringBuffer baseBufferUrl = new StringBuffer();
        baseBufferUrl.append(this.baseUrl);
        StringBuffer dataUrl = new StringBuffer();
        dataUrl.append("func=logout&pds_handle=").append(pdsHandle);
        URL url = new URL(baseBufferUrl.toString());
        this.getUrlContent(url, dataUrl);
        return null;
    }

    private StringBuffer getUrlContent(URL url, StringBuffer dataUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(dataUrl.toString());
        wr.flush();
        wr.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
        StringBuffer content = new StringBuffer();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }

        bufferedReader.close();
        return content;
    }

    private String extractPdsHandle(String content) throws Exception {
        StringBuffer pdsHandle = new StringBuffer();
        int startPos = content.indexOf("&pds_handle=");
        if (startPos < 0) {
            throw new Exception("Failed to authenticate user.");
        } else {
            startPos += "&pds_handle=".length();
            content = content.substring(startPos);
            if (!Character.isDigit(content.charAt(0))) {
                throw new Exception("Failed to authenticate user.");
            } else {
                for (int i = 0; Character.isDigit(content.charAt(i)); ++i) {
                    pdsHandle.append(content.charAt(i));
                }

                return pdsHandle.toString();
            }
        }
    }

    public PdsUserInfo getPdsUserByPdsHandle(String pdsHandle) throws Exception {
        if (this.isTestMode) {
            return this.demoPdsUserInfo();
        } else {
            StringBuffer baseBufferUrl = new StringBuffer();
            baseBufferUrl.append(this.baseUrl);
            StringBuffer dataUrl = new StringBuffer();
            dataUrl.append("func=get-attribute&attribute=BOR_INFO&pds_handle=").append(pdsHandle);
            URL url = new URL(baseBufferUrl.toString());
            return this.parseUserInfo(this.getUrlContent(url, dataUrl).toString());
        }
    }

    private PdsUserInfo demoPdsUserInfo() {
        PdsUserInfo info = new PdsUserInfo();
        info.setUserName("AGENT01");
        info.setBorDeptM("INS01");
        return info;
    }

    private PdsUserInfo parseUserInfo(String xml) throws Exception {
        PdsUserInfo info = new PdsUserInfo();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(xml));
        Node node = document.selectSingleNode("//bor-info/id");
        if (node != null) {
            info.setUserId(node.getText());
        }

        node = document.selectSingleNode("//bor-info/name");
        if (node != null) {
            info.setUserName(node.getText());
        }

        node = document.selectSingleNode("//bor-info/group");
        if (node != null) {
            info.setBorGroup(node.getText());
        }

        node = document.selectSingleNode("//bor-info/expiry_date");
        if (node != null) {
            info.setExpiryDate(node.getText());
        }

        node = document.selectSingleNode("//bor-info/bor_dept_m");
        if (node != null) {
            info.setBorDeptM(node.getText());
        }

        node = document.selectSingleNode("//bor-info/bor_group_m");
        if (node != null) {
            info.setBorGroup(node.getText());
        }

        node = document.selectSingleNode("//bor-info/course_enrollment_m");
        if (node != null) {
            info.setBorGroupM(node.getText());
        }

        node = document.selectSingleNode("//bor-info/bor_tuples_m");
        if (node != null) {
            info.setBorTuplesM(node.getText());
        }

        return info;
    }
}
