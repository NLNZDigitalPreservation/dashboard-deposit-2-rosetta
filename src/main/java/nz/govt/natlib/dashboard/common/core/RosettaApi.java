package nz.govt.natlib.dashboard.common.core;

import com.exlibris.digitool.deposit.service.xmlbeans.DepData;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;
import com.exlibris.digitool.repository.ifc.Collection;
import com.exlibris.dps.*;
import com.exlibris.dps.sdk.pds.PdsUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.util.CustomizedPdsClient;

import nz.govt.natlib.ndha.common.exlibris.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Exception;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RosettaApi {
    private static final Logger log = LoggerFactory.getLogger(RosettaApi.class);

    private final String pdsUrl;
    private final String rosettaRestApiUrl;


    private CustomizedPdsClient pdsClient;


    private String dpsSruCmsUrl = "http://wlgortimuweb01.natlib.govt.nz:8080/sru?";
    private String dcSruCmsUrl = "https://ap01-psb.alma.exlibrisgroup.com/view/sru/64NLNZ_MAIN?";
    private String dcProxyUsername = "serverside";
    private String dcProxyPassword = "******";

    public RosettaApi(String pdsUrl, String rosettaRestApiUrl) {
        this.pdsUrl = pdsUrl;
        this.rosettaRestApiUrl = rosettaRestApiUrl;
        this.pdsClient = CustomizedPdsClient.getInstance();
        this.pdsClient.init(pdsUrl, false);
    }

    public String login(String institution, String username, String password) throws Exception {
        try {
            if (pdsClient == null) {
                pdsClient = CustomizedPdsClient.getInstance();
                pdsClient.init(pdsUrl, false);
            }
            return pdsClient.login(institution, username, password);
        } catch (Exception e) {
            String err = String.format("Login failed: institution=%s, username=%s, password=********, %s", institution, username, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public String logout(String pdsHandle) throws Exception {
        try {
            if (pdsClient == null) {
                pdsClient = CustomizedPdsClient.getInstance();
                pdsClient.init(pdsUrl, false);
            }
            return pdsClient.logout(pdsHandle);
        } catch (Exception e) {
            String err = String.format("Logout failed: pdsHandle=%s: %s", pdsHandle, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public PdsUserInfo getPdsUserByPdsHandle(String pdsHandle) throws Exception {
        try {
            if (pdsClient == null) {
                pdsClient = CustomizedPdsClient.getInstance();
                pdsClient.init(pdsUrl, false);
            }
            return pdsClient.getPdsUserByPdsHandle(pdsHandle);
        } catch (Exception e) {
            String err = String.format("Get PdsUserByPdsHandle failed: pdsHandle=%s: %s", pdsHandle, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    private String getBasicAuthenticationHeader(EntityDepositAccountSetting depositAccount) {
        String credential = String.format("%s-institutionCode-%s:%s", depositAccount.getDepositUserName(), depositAccount.getDepositUserInstitute(), depositAccount.getDepositUserPassword());
        return "Basic " + Base64.getEncoder().encodeToString(credential.getBytes());
    }

    private String fetch(EntityDepositAccountSetting depositAccount, String method, String path, Object reqBody) throws Exception {
        String json;
        if (reqBody == null) {
            json = "{}";
        } else {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(reqBody);
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest req = HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.ofString(json))
                .uri(new URI(this.rosettaRestApiUrl + path))
                .header("Authorization", this.getBasicAuthenticationHeader(depositAccount))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (rsp.statusCode() != 200) {
            String err = String.format("Failed to request: %s, error: %s", path, rsp.body());
            log.error(err);
            throw new Exception(err);
        }

        //Parse json
        return rsp.body();
    }

    public List<Producer> getProducers(EntityDepositAccountSetting depositAccount) throws Exception {
        List<Producer> producers = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().GET().uri(new URI(this.rosettaRestApiUrl + "/producers")).header("Authorization", this.getBasicAuthenticationHeader(depositAccount)).build();

            HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (rsp.statusCode() >= 300) {
                log.error("Failed to request producers,{}", depositAccount);
                return producers;
            }

            String xmlReply = rsp.body();
            DepositDataDocument depositReply = DepositDataDocument.Factory.parse(xmlReply);
            DepositDataDocument.DepositData depositData = depositReply.getDepositData();
            DepData[] depDataAry = depositData.getDepDataArray();
            for (DepData depData : depDataAry) {
                producers.add(new Producer(depData.getId(), depData.getDescription()));
            }

            producers.sort(new ProducerComparator());
            return producers;
        } catch (Exception e) {
            String err = String.format("Failed to get producers: depositUserName=%s: %s", depositAccount, e.getMessage());
            log.error(err);
            throw e;
        }
    }

    public boolean isValidProducer(EntityDepositAccountSetting depositAccount, String producerId) throws Exception {
        List<Producer> producers = getProducers(depositAccount);
        for (Producer producer : producers) {
            if (producer.getID().equals(producerId)) {
                return true;
            }
        }
        return false;
    }

    public List<MaterialFlow> getMaterialFlows(EntityDepositAccountSetting depositAccount, String producerID) throws Exception {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().GET().uri(new URI(this.rosettaRestApiUrl + "/producers/producer-profiles/" + producerID + "/material-flows")).header("Authorization", this.getBasicAuthenticationHeader(depositAccount)).build();

            HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (rsp.statusCode() >= 300) {
                log.error("Failed to request material flows,{} {}", depositAccount, producerID);
                return null;
            }

            String xmlReply = rsp.body();

            DepositDataDocument depositReply = DepositDataDocument.Factory.parse(xmlReply);
            DepositDataDocument.DepositData depositData = depositReply.getDepositData();
            DepData[] depDataAry = depositData.getDepDataArray();
            List<MaterialFlow> retVal = new ArrayList<>();
            for (DepData depData : depDataAry) {
                retVal.add(new MaterialFlow(depData.getId(), depData.getDescription()));
            }

            retVal.sort(new MaterialFlowComparator());
            return retVal;
        } catch (Exception e) {
            String err = String.format("Failed to get material flows: producerID=%s: %s", producerID, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public boolean isValidMaterialFlow(EntityDepositAccountSetting depositAccount, String producerId, String materialFlowId) throws Exception {
        List<MaterialFlow> materialFlows = getMaterialFlows(depositAccount, producerId);
        for (MaterialFlow materialFlow : materialFlows) {
            if (materialFlow.getID().equals(materialFlowId)) {
                return true;
            }
        }
        return false;
    }

    public ResultOfDeposit deposit(EntityDepositAccountSetting depositAccount, String injectionRootDirectory, String depositUserProducerId, String materialFlowID, String depositSetID) throws Exception {
        try {
            if (!isValidProducer(depositAccount, depositUserProducerId)) {
                String err = String.format("Invalid producer: %s", depositUserProducerId);
                System.out.println(err);
                throw new Exception("Invalid producer: " + depositUserProducerId);
            }

            if (!isValidMaterialFlow(depositAccount, depositUserProducerId, materialFlowID)) {
                String err = String.format("Invalid material flow: %s", materialFlowID);
                System.out.println(err);
                throw new Exception("Invalid material flow: " + materialFlowID);
            }


            RestRequestDeposit reqBody = new RestRequestDeposit();
            reqBody.setSubdirectory(injectionRootDirectory);
            reqBody.setProducer(new RestRequestDeposit.Producer(depositUserProducerId));
            reqBody.setMaterial_flow(new RestRequestDeposit.MaterialFlow(materialFlowID));

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(reqBody);


            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI(this.rosettaRestApiUrl + "/deposits")).header("Authorization", this.getBasicAuthenticationHeader(depositAccount)).header("Content-Type", "application/json").build();

            HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (rsp.statusCode() >= 300) {
                log.error("Failed to request material flows,{} {}", depositAccount, depositUserProducerId);
                return null;
            }

            String xmlReply = rsp.body();


            DepositResultDocument depositReply = com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument.Factory.parse(xmlReply);
            DepositResultDocument.DepositResult result = depositReply.getDepositResult();
            ResultOfDeposit retVal;
            if (result.getIsError()) {
                retVal = ResultOfDeposit.create(false, result.getMessageDesc(), null);
            } else {
                String sipID = String.format("%d", result.getSipId());
                retVal = ResultOfDeposit.create(true, result.getMessageDesc(), sipID);
            }

            return retVal;
        } catch (Exception e) {
            log.error("Deposit failed: {} {} {} {}", depositAccount, injectionRootDirectory, depositUserProducerId, materialFlowID);
            throw e;
        }
    }

    public SipStatusInfo getSIPStatusInfo(String sipId) throws Exception {
//        try {
//            if (sipWebServices == null) {
//                sipWebServices = (new SipWebServices_Service(new URL(wsdlUrlSip), new QName("http://dps.exlibris.com/", "SipWebServices"))).getSipWebServicesPort();
//            }
//            return sipWebServices.getSIPStatusInfo(sipId, false);
//        } catch (Exception_Exception e) {
//            String err = String.format("Failed to get SIP StatusInfo: %s: %s", sipId, e.getMessage());
//            System.out.println(err);
//            throw e;
//        }
        return null;
    }

    public Collection getCollectionByName(String pdsHandle, String path) {
        return null;
    }


    public CustomizedPdsClient getPdsClient() {
        if (pdsClient == null) {
            pdsClient = CustomizedPdsClient.getInstance();
            pdsClient.init(pdsUrl, false);
        }
        return pdsClient;
    }


}
