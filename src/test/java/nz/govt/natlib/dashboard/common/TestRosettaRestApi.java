package nz.govt.natlib.dashboard.common;

import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.core.dto.DtoMaterialFlowRsp;
import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;
import nz.govt.natlib.dashboard.common.metadata.SipStatusInfo;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;

public class TestRosettaRestApi {
    private static final String PDSUrl = "https://slbpdstest.natlib.govt.nz/pds?";
    private static final String RestApiUrl = "https://wlguatdpsilb.natlib.govt.nz/rest/v0";

    private static final RosettaWebService restApi = new RosettaWebService(PDSUrl, RestApiUrl, true);

    private static final String _PRODUCER_AGENT_ID = "NLNZ";
    private static final String INSTITUTION = "INS00";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final EntityDepositAccountSetting depositAccount = new EntityDepositAccountSetting();

    @BeforeAll
    static void init() throws Exception {
        depositAccount.setDepositUserInstitute(INSTITUTION);
        depositAccount.setDepositUserName(USERNAME);
        depositAccount.setDepositUserPassword(PASSWORD);

//        CustomizedPdsClient pdsClient = CustomizedPdsClient.getInstance();
//        pdsClient.init("http://localhost/", true);
    }


    //    @Test
    public void testGetProducer() throws Exception {
        List<DtoProducersRsp.Producer> producers = restApi.getProducers(depositAccount);
        assert producers != null;
        assert !producers.isEmpty();

        final Set<String> set = new HashSet<>();
        producers.forEach(p -> {
            if (set.contains(p.getId())) {
                System.out.println(p.getId() + ": " + p.getName());
            } else {
                set.add(p.getId());
            }
        });
        assert producers.size() == set.size();
    }


    //    @Disabled
//    @Test
    public void testGetMaterialFlow() throws Exception {
        List<DtoProducersRsp.Producer> producers = restApi.getProducers(depositAccount);
        assert producers != null;
        assert !producers.isEmpty();


        for (DtoProducersRsp.Producer producer : producers) {
            List<DtoMaterialFlowRsp.MaterialFlow> materialFlows = restApi.getMaterialFlows(depositAccount, producer.getId());
            if (materialFlows == null || materialFlows.isEmpty()) {
                continue;
            }
            System.out.printf("Producer: %s %s", producer.getId(), producer.getName());
        }
    }

    @Test
    public void testGetSipStatusInfo() throws Exception {
        String sipId = "742449";
        try {
            SipStatusInfo sipStatusInfo = restApi.getSIPStatusInfo(depositAccount, sipId);
//        assert sipStatusInfo != null;
//        System.out.println(sipStatusInfo.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
