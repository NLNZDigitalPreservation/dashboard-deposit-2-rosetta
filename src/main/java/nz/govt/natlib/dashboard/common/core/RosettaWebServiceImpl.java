package nz.govt.natlib.dashboard.common.core;

import com.exlibris.digitool.deposit.service.xmlbeans.DepData;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;
import com.exlibris.digitool.repository.ifc.Collection;
import com.exlibris.dps.*;
import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.util.CustomizedPdsClient;
import nz.govt.natlib.dashboard.domain.service.UserAccessService;

import nz.govt.natlib.ndha.common.exlibris.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.lang.Exception;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RosettaWebServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(UserAccessService.class);
    private CustomizedPdsClient pdsClient;
    private ProducerWebServices producerWebServices;
    private DepositWebServices depositWebServices;
    private SipWebServices sipWebServices;

    private String dpsSruCmsUrl = "http://wlgortimuweb01.natlib.govt.nz:8080/sru?";
    private String dcSruCmsUrl = "https://ap01-psb.alma.exlibrisgroup.com/view/sru/64NLNZ_MAIN?";
    private String dcProxyUsername = "leefr";
    private String dcProxyPassword = "******";

    public void init(String pdsUrl, String wsdlUrlProducer, String wsdlUrlDeposit, String wsdlUrlSip, String wsdlUrlDeliveryAccess) {
        Runnable postponeInitializer = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(30); //Postpone 30 seconds to wait for the preparation of Rosetta service.

                        _init(pdsUrl, wsdlUrlProducer, wsdlUrlDeposit, wsdlUrlSip, wsdlUrlDeliveryAccess);

                        log.info("Succeed to connect to Rosetta services, and ended retrying.");
                        return;
                    } catch (Exception e) {
                        log.warn("Failed to connect to Rosetta services, will retry.", e);
                    }
                }
            }
        };

        Thread processor = new Thread(postponeInitializer);
        processor.start();
    }

    public void _init(String pdsUrl, String wsdlUrlProducer, String wsdlUrlDeposit, String wsdlUrlSip, String wsdlUrlDeliveryAccess) throws Exception {
        pdsClient = CustomizedPdsClient.getInstance();
        pdsClient.init(pdsUrl, false);

        producerWebServices = (new ProducerWebServices_Service(new URL(wsdlUrlProducer), new QName("http://dps.exlibris.com/", "ProducerWebServices"))).getProducerWebServicesPort();
        depositWebServices = (new DepositWebServices_Service(new URL(wsdlUrlDeposit), new QName("http://dps.exlibris.com/", "DepositWebServices"))).getDepositWebServicesPort();
        sipWebServices = (new SipWebServices_Service(new URL(wsdlUrlSip), new QName("http://dps.exlibris.com/", "SipWebServices"))).getSipWebServicesPort();
    }

    public String getDpsSruCmsUrl() {
        return dpsSruCmsUrl;
    }

    public void setDpsSruCmsUrl(String dpsSruCmsUrl) {
        this.dpsSruCmsUrl = dpsSruCmsUrl;
    }

    public String getDcSruCmsUrl() {
        return dcSruCmsUrl;
    }

    public void setDcSruCmsUrl(String dcSruCmsUrl) {
        this.dcSruCmsUrl = dcSruCmsUrl;
    }

    public String getDcProxyUsername() {
        return dcProxyUsername;
    }

    public void setDcProxyUsername(String dcProxyUsername) {
        this.dcProxyUsername = dcProxyUsername;
    }

    public String getDcProxyPassword() {
        return dcProxyPassword;
    }

    public void setDcProxyPassword(String dcProxyPassword) {
        this.dcProxyPassword = dcProxyPassword;
    }

    public String login(String institution, String username, String password) throws Exception {
        try {
            return pdsClient.login(institution, username, password);
        } catch (Exception e) {
            log.error("Login failed: institution={}, username={}, password={}", institution, username, "*********", e);
            throw e;
        }
    }

    public String logout(String pdsHandle) throws Exception {
        try {
            return pdsClient.logout(pdsHandle);
        } catch (Exception e) {
            log.error("Logout failed: pdsHandle={}", pdsHandle, e);
            throw e;
        }
    }

    public PdsUserInfo getPdsUserByPdsHandle(String pdsHandle) throws Exception {
        try {
            return pdsClient.getPdsUserByPdsHandle(pdsHandle);
        } catch (Exception e) {
            log.error("Logout failed: pdsHandle={}", pdsHandle, e);
            throw e;
        }
    }

    public String getInternalUserIdByExternalId(String userName) {
        return this.producerWebServices.getInternalUserIdByExternalId(userName);
    }

    public List<Producer> getProducers(String depositUserName) throws Exception {
        try {
            String producerAgentId = this.producerWebServices.getInternalUserIdByExternalId(depositUserName);
            String xmlReply = this.producerWebServices.getProducersOfProducerAgent(producerAgentId);
            DepositDataDocument depositReply = DepositDataDocument.Factory.parse(xmlReply);
            DepositDataDocument.DepositData depositData = depositReply.getDepositData();
            DepData[] depDataAry = depositData.getDepDataArray();
            List<Producer> retVal = new ArrayList<>();
            for (DepData depData : depDataAry) {
                retVal.add(new Producer(depData.getId(), depData.getDescription()));
            }

            retVal.sort(new ProducerComparator());
            return retVal;
        } catch (Exception e) {
            log.error("Failed to get producers: depositUserName={}", depositUserName, e);
            throw e;
        }
    }

    public boolean isValidProducer(String depositUserName, String producerId) throws Exception {
        List<Producer> producers = getProducers(depositUserName);
        for (Producer producer : producers) {
            if (producer.getID().equals(producerId)) {
                return true;
            }
        }
        return false;
    }

    public List<MaterialFlow> getMaterialFlows(String producerID) throws Exception {
        try {
            String xmlReply = this.producerWebServices.getMaterialFlowsOfProducer(producerID);
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
            log.error("Failed to get material flows: producerID={}", producerID, e);
            throw e;
        }
    }

    public boolean isValidMaterialFlow(String producerId, String materialFlowId) throws Exception {
        List<MaterialFlow> materialFlows = getMaterialFlows(producerId);
        for (MaterialFlow materialFlow : materialFlows) {
            if (materialFlow.getID().equals(materialFlowId)) {
                return true;
            }
        }
        return false;
    }

    public ResultOfDeposit deposit(String injectionRootDirectory, String pdsHandle, String depositUserInstitution, String depositUserProducerId, String materialFlowID, String depositSetID) throws Exception {
        StringBuilder paramsBuf = new StringBuilder(System.lineSeparator());
        paramsBuf.append("ingestRootDirectory: ").append(injectionRootDirectory).append(System.lineSeparator());
        paramsBuf.append("pdsHandle: ").append(pdsHandle).append(System.lineSeparator());
        paramsBuf.append("depositUserInstitution: ").append(depositUserInstitution).append(System.lineSeparator());
        paramsBuf.append("depositUserProducerId: ").append(depositUserProducerId).append(System.lineSeparator());
        paramsBuf.append("materialFlowID: ").append(materialFlowID).append(System.lineSeparator());
        paramsBuf.append("depositSetID: ").append(depositSetID).append(System.lineSeparator());

        PdsUserInfo pdsUserInfo = pdsClient.getPdsUserByPdsHandle(pdsHandle);
        String depositUserName = pdsUserInfo.getUserName();
        if (!isValidProducer(depositUserName, depositUserProducerId)) {
            log.error("Invalid producer: {}", paramsBuf.toString());
            throw new Exception("Invalid producer: " + paramsBuf.toString());
        }

        if (!isValidMaterialFlow(depositUserProducerId, materialFlowID)) {
            log.error("Invalid material flow: {}", paramsBuf.toString());
            throw new Exception("Invalid material flow: " + paramsBuf.toString());
        }

        DepositResultDocument.DepositResult result;
        try {
            String xmlReply = this.depositWebServices.submitDepositActivity(pdsHandle, materialFlowID, injectionRootDirectory, depositUserProducerId, depositSetID);
            DepositResultDocument depositReply = com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument.Factory.parse(xmlReply);
            result = depositReply.getDepositResult();
            ResultOfDeposit retVal;
            if (result.getIsError()) {
                retVal = ResultOfDeposit.create(false, result.getMessageDesc(), null);
            } else {
                String sipID = String.format("%d", result.getSipId());
                retVal = ResultOfDeposit.create(true, result.getMessageDesc(), sipID);
            }

            return retVal;
        } catch (Exception e) {
            log.error("Deposit failed: {}", paramsBuf.toString(), e);
            throw e;
        }
    }

    public SipStatusInfo getSIPStatusInfo(String sipId) throws Exception {
        try {
            return sipWebServices.getSIPStatusInfo(sipId, false);
        } catch (Exception_Exception e) {
            log.error("Failed to get SIP StatusInfo: {}", sipId, e);
            throw e;
        }
    }

    public Collection getCollectionByName(String pdsHandle, String path) {
        return null;
    }

    public String getDepositActivityBySubmitDateByMaterialFlow(String pdsHandle,
                                                               String depositActivityStatus,
                                                               String materialFlowId,
                                                               String producerID,
                                                               String producerAgentID,
                                                               String updateDateFrom,
                                                               String updateDateTo,
                                                               String startRecord,
                                                               String endRecord) {
        return this.depositWebServices.getDepositActivityBySubmitDateByMaterialFlow(pdsHandle, depositActivityStatus, materialFlowId, producerID, producerAgentID, updateDateFrom, updateDateTo, startRecord, endRecord);
    }

    public CustomizedPdsClient getPdsClient() {
        return pdsClient;
    }

    public ProducerWebServices getProducerWebServices() {
        return producerWebServices;
    }

    public DepositWebServices getDepositWebServices() {
        return depositWebServices;
    }

    public SipWebServices getSipWebServices() {
        return sipWebServices;
    }
}
