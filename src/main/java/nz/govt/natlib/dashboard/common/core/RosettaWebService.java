package nz.govt.natlib.dashboard.common.core;

import nz.govt.natlib.dashboard.common.metadata.PdsUserInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.govt.natlib.dashboard.common.core.dto.*;
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
    private  RosettaRestApi dpsRestAPI;
    private  RosettaRestApi sipRestAPI;
    private CustomizedPdsClient pdsClient;

    public RosettaWebService(String pdsUrl, String restApiDpsUrl, String restApiSipUrl, boolean isTestMode) {
        this.dpsRestAPI = new RosettaRestApi(restApiDpsUrl);
        this.sipRestAPI = new RosettaRestApi(restApiSipUrl);
        this.pdsClient = CustomizedPdsClient.getInstance();
        this.pdsClient.init(pdsUrl, isTestMode);
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

    public String getProducers(EntityDepositAccountSetting depositAccount, int limit, int offset, String name) throws Exception {
        String path;
        if (StringUtils.isEmpty(name)) {
            path = String.format("/producers?limit=%d&offset=%d", limit, offset);
        } else {
            path = String.format("/producers?limit=%d&offset=%d&name=%s", limit, offset, name);
        }
        return this.dpsRestAPI.fetch(depositAccount, "GET", path, null);
    }

    public List<DtoProducersRsp.Producer> getProducers(EntityDepositAccountSetting depositAccount) throws Exception {
        List<DtoProducersRsp.Producer> producers = new ArrayList<>();
        int offset = 0;
        while (true) {
            String ret = this.dpsRestAPI.fetch(depositAccount, "GET", "/producers?limit=100&offset=" + offset, null);
            DtoProducersRsp rsp = (DtoProducersRsp) this.jsonToObject(ret, DtoProducersRsp.class);
            log.debug("Got producers, offset={}", offset);
            if (rsp != null && rsp.getTotal_record_count() > 0 && rsp.getProducer() != null) {
                producers.addAll(rsp.getProducer());
                offset += 1; //offset means next datasets
            } else {
                break;
            }
        }
        log.debug("{} producers with account: {}", producers.size(), depositAccount);
        return producers;
    }

    public String getProducerProfileId(EntityDepositAccountSetting depositAccount, String producerId) throws Exception {
        String ret = this.dpsRestAPI.fetch(depositAccount, "GET", "/producers/" + producerId, null);
        DtoProducerDetailRsp rsp = (DtoProducerDetailRsp) this.jsonToObject(ret, DtoProducerDetailRsp.class);
        if (rsp != null && rsp.getProfile() != null) {
            return rsp.getProfile().getId();
        } else {
            log.error("Can not find the producer profile with the producer id: {}", producerId);
            return null;
        }
    }

    public boolean isValidProducer(EntityDepositAccountSetting depositAccount, String producerId) throws Exception {
        String profileId = this.getProducerProfileId(depositAccount, producerId);
        return !StringUtils.isEmpty(profileId);
    }

    public String getMaterialFlows(EntityDepositAccountSetting depositAccount, String producerId, int limit, int offset, String name) throws Exception {
        String profileId = this.getProducerProfileId(depositAccount, producerId);
        if (StringUtils.isEmpty(profileId)) {
            return null;
        }

        String path;
        if (StringUtils.isEmpty(name)) {
            path = String.format("/producers/producer-profiles/%s/material-flows?limit=%d&offset=%d", profileId, limit, offset);
        } else {
            path = String.format("/producers/producer-profiles/%s/material-flows?limit=%d&offset=%d&name=%s", profileId, limit, offset, name);
        }
        return this.dpsRestAPI.fetch(depositAccount, "GET", path, null);
    }

    public List<DtoMaterialFlowRsp.MaterialFlow> getMaterialFlows(EntityDepositAccountSetting depositAccount, String producerId) throws Exception {
        List<DtoMaterialFlowRsp.MaterialFlow> materialFlows = new ArrayList<>();

        String profileId = this.getProducerProfileId(depositAccount, producerId);
        if (StringUtils.isEmpty(profileId)) {
            return materialFlows;
        }

        int offset = 0;
        while (true) {
            String ret = this.dpsRestAPI.fetch(depositAccount, "GET", "/producers/producer-profiles/" + profileId + "/material-flows?limit=100&offset=" + offset, null);
            DtoMaterialFlowRsp rsp = (DtoMaterialFlowRsp) this.jsonToObject(ret, DtoMaterialFlowRsp.class);
            if (rsp != null && rsp.getTotal_record_count() > 0 && rsp.getProfile_material_flow() != null) {
                materialFlows.addAll(rsp.getProfile_material_flow());
                offset += 1;
            } else {
                break;
            }
        }
        return materialFlows;
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

    public ResultOfDeposit deposit(EntityDepositAccountSetting depositAccount, String injectionRootDirectory, String depositUserProducerId, String materialFlowID) throws Exception {
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

            String rsp = this.dpsRestAPI.fetch(depositAccount, "POST", "/deposits", reqBody);
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
        String rsp = this.sipRestAPI.fetch(depositAccount, "GET", "/sips/" + sipId, null);
        return (SipStatusInfo) this.jsonToObject(rsp, SipStatusInfo.class);
    }

    public void setDpsRestAPI(RosettaRestApi dpsRestAPI) {
        this.dpsRestAPI = dpsRestAPI;
    }

    public void setSipRestAPI(RosettaRestApi sipRestAPI) {
        this.sipRestAPI = sipRestAPI;
    }

    public void setPdsClient(CustomizedPdsClient pdsClient) {
        this.pdsClient = pdsClient;
    }
}
