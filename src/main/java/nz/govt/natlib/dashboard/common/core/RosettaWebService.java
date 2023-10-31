package nz.govt.natlib.dashboard.common.core;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.govt.natlib.dashboard.common.core.dto.DtoDepositReq;
import nz.govt.natlib.dashboard.common.core.dto.DtoDepositRsp;
import nz.govt.natlib.dashboard.common.core.dto.DtoMaterialFlowRsp;
import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;
import nz.govt.natlib.dashboard.common.metadata.ResultOfDeposit;
import nz.govt.natlib.dashboard.common.metadata.SipStatusInfo;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.util.CustomizedPdsClient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;

public class RosettaWebService {
    private static final Logger log = LoggerFactory.getLogger(RosettaWebService.class);

    private final String pdsUrl;
    private RosettaRestApi restApi;


    private CustomizedPdsClient pdsClient;


    public RosettaWebService(String pdsUrl, String rosettaRestApiUrl) {
        this.pdsUrl = pdsUrl;
        this.restApi = new RosettaRestApi(rosettaRestApiUrl);
        this.pdsClient = CustomizedPdsClient.getInstance();
        this.pdsClient.init(pdsUrl, false);
    }

    public String login(String institution, String username, String password) throws Exception {
        try {
            return pdsClient.login(institution, username, password);
        } catch (Exception e) {
            String err = String.format("Login failed: institution=%s, username=%s, password=********, %s", institution, username, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public String logout(String pdsHandle) throws Exception {
        try {
            return pdsClient.logout(pdsHandle);
        } catch (Exception e) {
            String err = String.format("Logout failed: pdsHandle=%s: %s", pdsHandle, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public PdsUserInfo getPdsUserByPdsHandle(String pdsHandle) throws Exception {
        try {
            return pdsClient.getPdsUserByPdsHandle(pdsHandle);
        } catch (Exception e) {
            String err = String.format("Get PdsUserByPdsHandle failed: pdsHandle=%s: %s", pdsHandle, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }


    private Object jsonToObject(String json, Class<?> valueType) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            log.error("Failed to convert json to object", e);
        }

        return null;
    }

    public List<DtoProducersRsp.Producer> getProducers(EntityDepositAccountSetting depositAccount) throws Exception {
        List<DtoProducersRsp.Producer> producers;
        try {
            String ret = this.restApi.fetch(depositAccount, "GET", "/producers", null);
            DtoProducersRsp producersRsp = (DtoProducersRsp) this.jsonToObject(ret, DtoProducersRsp.class);
            if (producersRsp != null && producersRsp.getProducer() != null) {
                producers = producersRsp.getProducer();
            } else {
                producers = new ArrayList<>();
            }
            return producers;
        } catch (Exception e) {
            String err = String.format("Failed to get producers: depositUserName=%s: %s", depositAccount, e.getMessage());
            log.error(err);
            throw e;
        }
    }

    public boolean isValidProducer(EntityDepositAccountSetting depositAccount, String producerId) throws Exception {
        List<DtoProducersRsp.Producer> producers = getProducers(depositAccount);
        for (DtoProducersRsp.Producer producer : producers) {
            if (producer.getId().equals(producerId)) {
                return true;
            }
        }
        return false;
    }

    public List<DtoMaterialFlowRsp.MaterialFlow> getMaterialFlows(EntityDepositAccountSetting depositAccount, String producerID) throws Exception {
        List<DtoMaterialFlowRsp.MaterialFlow> materialFlows;
        try {
            String ret = this.restApi.fetch(depositAccount, "GET", "/producers/producer-profiles/" + producerID + "/material-flows", null);
            DtoMaterialFlowRsp rsp = (DtoMaterialFlowRsp) this.jsonToObject(ret, DtoMaterialFlowRsp.class);
            if (rsp != null && rsp.getProfile_material_flow() != null) {
                materialFlows = rsp.getProfile_material_flow();
            } else {
                materialFlows = new ArrayList<>();
            }
            return materialFlows;
        } catch (Exception e) {
            String err = String.format("Failed to get material flows: producerID=%s: %s", producerID, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public boolean isValidMaterialFlow(EntityDepositAccountSetting depositAccount, String producerId, String materialFlowId) throws Exception {
        List<DtoMaterialFlowRsp.MaterialFlow> materialFlows = getMaterialFlows(depositAccount, producerId);
        for (DtoMaterialFlowRsp.MaterialFlow materialFlow : materialFlows) {
            if (materialFlow.getId().equals(materialFlowId)) {
                return true;
            }
        }
        return false;
    }

    public ResultOfDeposit deposit(EntityDepositAccountSetting depositAccount, String injectionRootDirectory, String depositUserProducerId, String materialFlowID, String depositSetID) throws Exception {
        try {
            if (!isValidProducer(depositAccount, depositUserProducerId)) {
                log.warn("Invalid producer: {}", depositUserProducerId);
            }

            if (!isValidMaterialFlow(depositAccount, depositUserProducerId, materialFlowID)) {
                log.warn("Invalid material flow: {}", materialFlowID);
            }

            DtoDepositReq reqBody = new DtoDepositReq();
            reqBody.setSubdirectory(injectionRootDirectory);
            reqBody.setProducer(new DtoDepositReq.Producer(depositUserProducerId));
            reqBody.setMaterial_flow(new DtoDepositReq.MaterialFlow(materialFlowID));

            String rsp = this.restApi.fetch(depositAccount, "POST", "/deposits", reqBody);
            DtoDepositRsp ret = (DtoDepositRsp) this.jsonToObject(rsp, DtoDepositRsp.class);

            boolean result = false;
            String sipId = "", sipReason = "";
            if (ret != null) {
                if (!StringUtils.equalsIgnoreCase(ret.getStatus(), "Rejected") && !StringUtils.equalsIgnoreCase(ret.getStatus(), "Declined")) {
                    result = !StringUtils.isEmpty(ret.getSip_id());
                }

                sipId = ret.getSip_id();
                sipReason = ret.getSip_reason();
            }

            return ResultOfDeposit.create(result, sipId, sipReason);
        } catch (Exception e) {
            log.error("Deposit failed: {} {} {} {}", depositAccount, injectionRootDirectory, depositUserProducerId, materialFlowID);
            throw e;
        }
    }

    public SipStatusInfo getSIPStatusInfo(EntityDepositAccountSetting depositAccount, String sipId) throws Exception {
        String rsp = this.restApi.fetch(depositAccount, "POST", "/sips/" + sipId, null);
        return (SipStatusInfo) this.jsonToObject(rsp, SipStatusInfo.class);
    }


    public CustomizedPdsClient getPdsClient() {
        if (pdsClient == null) {
            pdsClient = CustomizedPdsClient.getInstance();
            pdsClient.init(pdsUrl, false);
        }
        return pdsClient;
    }

    public RosettaRestApi getRestApi() {
        return restApi;
    }

    public void setRestApi(RosettaRestApi restApi) {
        this.restApi = restApi;
    }

    public void setPdsClient(CustomizedPdsClient pdsClient) {
        this.pdsClient = pdsClient;
    }
}
