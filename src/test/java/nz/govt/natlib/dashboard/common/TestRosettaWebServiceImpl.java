package nz.govt.natlib.dashboard.common;

import com.exlibris.dps.*;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.util.CustomizedPdsClient;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestRosettaWebServiceImpl {
    private static final String PDSUrl = "https://slbpdstest.natlib.govt.nz/pds?";
    private static final String ProducerWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/ProducerWebServices?wsdl";
    private static final String DepositWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/DepositWebServices?wsdl";
    private static final String SipWsdlUrl = "https://wlguatoprilb.natlib.govt.nz/dpsws/repository/SipWebServices?wsdl";
    private static final String DPSSearchUrl = "https://wlguatdpsilb.natlib.govt.nz/delivery/sru";
    private static final String DeliveryAccessWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/delivery/DeliveryAccessWS?wsdl";
    private static final RosettaWebServiceImpl rosettaWebService = new RosettaWebServiceImpl(PDSUrl, ProducerWsdlUrl, DepositWsdlUrl, SipWsdlUrl, DeliveryAccessWsdlUrl);

    private static final String _PRODUCER_AGENT_ID = "NLNZ";
    private static final String INSTITUTION = "INS00";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";

    private static final ProducerWebServices producerWebServices = mock(ProducerWebServices.class);
    private static final DepositWebServices depositWebServices = mock(DepositWebServices.class);
    private static final SipWebServices sipWebServices = mock(SipWebServices.class);

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
//        rosettaWebService._init(PDSUrl, ProducerWsdlUrl, DepositWsdlUrl, SipWsdlUrl, DeliveryAccessWsdlUrl);
        CustomizedPdsClient pdsClient = CustomizedPdsClient.getInstance();
        pdsClient.init("http://localhost/", true);


        ReflectionTestUtils.setField(rosettaWebService, "pdsClient", pdsClient);
        ReflectionTestUtils.setField(rosettaWebService, "producerWebServices", producerWebServices);
        ReflectionTestUtils.setField(rosettaWebService, "depositWebServices", depositWebServices);
        ReflectionTestUtils.setField(rosettaWebService, "sipWebServices", sipWebServices);
    }

    @Test
    public void testGetProducer() throws Exception {
        RosettaWebServiceImpl localRosettaWebService = new RosettaWebServiceImpl(PDSUrl, ProducerWsdlUrl, DepositWsdlUrl, SipWsdlUrl, DeliveryAccessWsdlUrl);
//        localRosettaWebService._init();
        try {
            List<Producer> producers= localRosettaWebService.getProducers("leefr");
            producers.forEach(p->{
                System.out.println(p.toString());
            });
        } catch (Exception e) {

        }

    }

    @Disabled
    @Test
    public void testGetDepositActivityByUpdateDateByMaterialFlow() throws Exception {
        String pdsHandle = rosettaWebService.login(INSTITUTION, USERNAME, PASSWORD);
//        PdsUserInfo pdsUserInfo = rosettaWebService.getPdsUserByPdsHandle(pdsHandle);

        when(producerWebServices.getInternalUserIdByExternalId(USERNAME)).thenReturn(_PRODUCER_AGENT_ID);
        String producerAgentID = rosettaWebService.getInternalUserIdByExternalId(USERNAME);
        assert producerAgentID.equals(_PRODUCER_AGENT_ID);

        String depositActivityStatus = "all";
        String updateDateFrom = "01/01/2020";
        String updateDateTo = "18/09/2020";
        String startRecord = "1";
        String endRecord = "20";

//        String xml=


//
//        rWebServices.getProducerXml();
//        when(producerWebServices.getInternalUserIdByExternalId(USERNAME)).thenReturn(_PRODUCER_AGENT_ID);
//        when(producerWebServices.getProducersOfProducerAgent(_PRODUCER_AGENT_ID)).thenReturn(xml);

//        producerWebServices.getProducersOfProducerAgent

        List<Producer> producers = rosettaWebService.getProducers(USERNAME);
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
        String sipId = "749344";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getSipIEs(sipId);
        System.out.println(reply);
    }

    @Test
    public void testGetSipStatus() {
        String sipId = "749344";
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
