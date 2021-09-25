package nz.govt.natlib.dashboard.common;

import com.exlibris.dps.*;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.Exception;
import java.net.CookieHandler;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TestRosettaWebServiceImpl {
    private static String PDSUrl = "https://slbpdstest.natlib.govt.nz/pds?";
    private static String ProducerWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/ProducerWebServices?wsdl";
    private static String DepositWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/DepositWebServices?wsdl";
    private static String SipWsdlUrl = "https://wlguatoprilb.natlib.govt.nz/dpsws/repository/SipWebServices?wsdl";
    private static String DPSSearchUrl = "https://wlguatdpsilb.natlib.govt.nz/delivery/sru";
    private static String DeliveryAccessWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/delivery/DeliveryAccessWS?wsdl";
    private static RosettaWebServiceImpl rosettaWebService = new RosettaWebServiceImpl();

    private static final String INSTITUTION = "INS00";
    private static final String USERNAME = "leefr";
    private static final String PASSWORD = "*****";

    @BeforeAll
    static void init() throws Exception {
//        String proxyHost = "192.168.1.65";
//        String proxyPort = "3128";
//        String proxyHost = "wlgproxyforservers.dia.govt.nz";
//        String proxyPort = "8080";
//        System.setProperty("http.proxyHost", proxyHost);
//        System.setProperty("http.proxyPort", proxyPort);
//        System.setProperty("https.proxyHost", proxyHost);
//        System.setProperty("https.proxyPort", proxyPort);
        rosettaWebService.init(PDSUrl, ProducerWsdlUrl, DepositWsdlUrl, SipWsdlUrl, DeliveryAccessWsdlUrl);
    }

    @Disabled
    @Test
    public void testGetDepositActivityByUpdateDateByMaterialFlow() throws Exception {
        String pdsHandle = rosettaWebService.login(INSTITUTION, USERNAME, PASSWORD);
//        PdsUserInfo pdsUserInfo = rosettaWebService.getPdsUserByPdsHandle(pdsHandle);
        String producerAgentID = rosettaWebService.getInternalUserIdByExternalId("leefr");
        String depositActivityStatus = "all";
        String updateDateFrom = "01/01/2020";
        String updateDateTo = "18/09/2020";
        String startRecord = "1";
        String endRecord = "20";

        List<Producer> producers = rosettaWebService.getProducers("leefr");
        for (Producer producer : producers) {
            List<MaterialFlow> materialFlows = rosettaWebService.getMaterialFlows(producer.getID());
            for (MaterialFlow materialFlow : materialFlows) {
                String materialFlowId = materialFlow.getID();
                String producerID = producer.getID();
                String reply = rosettaWebService.getDepositActivityBySubmitDateByMaterialFlow(pdsHandle,
                        depositActivityStatus,
                        materialFlowId,
                        producerID,
                        producerAgentID,
                        updateDateFrom,
                        updateDateTo,
                        startRecord,
                        endRecord
                );

                System.out.println(reply);
            }
        }
    }

    @Test
    public void testGetSipIEs() {
        String sipId = "742449";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getSipIEs(sipId);
        System.out.println(reply);
    }

    @Test
    public void testGetSipStatus() {
        String sipId = "742449";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getSipStatus(sipId);
        System.out.println(reply);
    }

    @Test
    public void testGetSipStatusInfo() throws Exception_Exception {
        String sipId = "742449";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        SipStatusInfo reply = sipApi.getSIPStatusInfo(sipId, false);
        System.out.println(reply);
    }

    @Test
    public void testHeartbeat() throws Exception_Exception {
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getHeartBit();
        System.out.println(reply);
    }

    @Disabled
    @Test
    public void testGetProducerDetails() throws Exception {
        String pdsHandle = rosettaWebService.login(INSTITUTION, USERNAME, PASSWORD);
        String producerId = "6355932079";
        ProducerWebServices pApi = rosettaWebService.getProducerWebServices();
        String reply = pApi.getProducerDetails(pdsHandle, producerId);
        System.out.println(reply);
    }

    private static String generateDPSSession(String pid, String pdsHandle) throws IOException, InterruptedException {
        String deliveryUrl = String.format("https://slbpdstest.natlib.govt.nz/goto/logon/https://ndhadelivertest.natlib.govt.nz/delivery/DeliveryManagerServlet?dps_pid=%s&pds_handle=%s&calling_system=del&institute=", pid, pdsHandle);

        ConcurrentHashMap<String, List<String>> cookieHeaders = new ConcurrentHashMap<>();
        CookieHandler cookieHandler = new DeliveryCookieHandler(cookieHeaders);
        List<String> cookies = new ArrayList<>();
        cookies.add("PDS_HANDLE=" + pdsHandle);
        cookies.add("PDSILB03=wlguatrosiapp02");
        cookieHeaders.put("Cookie", cookies);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(deliveryUrl))
                .timeout(Duration.ofSeconds(30))
//                .POST(HttpRequest.BodyPublishers.ofString(loginString))
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieHandler)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        httpResponse.headers().map().forEach((k, v) -> {
            System.out.println(k + ": " + String.join(",", v));
        });

        String rosettaDeliveryResponse = httpResponse.body();
        if (rosettaDeliveryResponse == null || rosettaDeliveryResponse.trim().isEmpty()) {
            return null;
        }
        System.out.println(rosettaDeliveryResponse);

        int iFrameStartPosition = rosettaDeliveryResponse.indexOf("<iframe");
        int iFrameEndPosition = rosettaDeliveryResponse.indexOf("</iframe>");

        if (iFrameStartPosition > 0 && iFrameEndPosition > 0) {
            rosettaDeliveryResponse = rosettaDeliveryResponse.substring(iFrameStartPosition, iFrameEndPosition);
        }

        int parameterNamePosition = rosettaDeliveryResponse.indexOf("dps_dvs=");
        if (parameterNamePosition < 0) {
            return null;
        }

        String remainingString = rosettaDeliveryResponse.substring(parameterNamePosition);
        String[] parameterPairs = remainingString.split("[&]");
        if (parameterPairs.length <= 0) {
            return null;
        }

        String parameterNameValuePair = parameterPairs[0];
        return (parameterNameValuePair == null ? null : parameterNameValuePair.replace("dps_dvs=", ""));
    }
}
