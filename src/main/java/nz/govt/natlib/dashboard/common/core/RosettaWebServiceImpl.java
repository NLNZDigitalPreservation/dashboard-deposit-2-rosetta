package nz.govt.natlib.dashboard.common.core;

import com.exlibris.digitool.deposit.service.xmlbeans.DepData;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;
import com.exlibris.digitool.repository.ifc.Collection;
import com.exlibris.dps.*;
import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.util.CustomizedPdsClient;

import nz.govt.natlib.ndha.common.exlibris.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.lang.Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RosettaWebServiceImpl {
//    private static final Logger log = LoggerFactory.getLogger(RosettaWebServiceImpl.class);

    private final String pdsUrl;
    private final String wsdlUrlProducer;
    private final String wsdlUrlDeposit;
    private final String wsdlUrlSip;
    private final String wsdlUrlDeliveryAccess;

    private CustomizedPdsClient pdsClient;
    private ProducerWebServices producerWebServices;
    private DepositWebServices depositWebServices;
    private SipWebServices sipWebServices;

    private String dpsSruCmsUrl = "http://wlgortimuweb01.natlib.govt.nz:8080/sru?";
    private String dcSruCmsUrl = "https://ap01-psb.alma.exlibrisgroup.com/view/sru/64NLNZ_MAIN?";
    private String dcProxyUsername = "serverside";
    private String dcProxyPassword = "******";

    public RosettaWebServiceImpl(String pdsUrl, String wsdlUrlProducer, String wsdlUrlDeposit, String wsdlUrlSip, String wsdlUrlDeliveryAccess) {
        this.pdsUrl = pdsUrl;
        this.wsdlUrlProducer = wsdlUrlProducer;
        this.wsdlUrlDeposit = wsdlUrlDeposit;
        this.wsdlUrlSip = wsdlUrlSip;
        this.wsdlUrlDeliveryAccess = wsdlUrlDeliveryAccess;
    }

    public void init() {
        Runnable postponeInitializer = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        _init();
                        System.out.println("Succeed to connect to Rosetta services, and ended retrying.");
                        TimeUnit.SECONDS.sleep(3); //Postpone 3 seconds to wait for the preparation of Rosetta service.
                        return;
                    } catch (Exception e) {
                        System.out.println("Failed to connect to Rosetta services, will retry: " + e.getMessage());
                    }
                }
            }
        };

        Thread processor = new Thread(postponeInitializer);
        processor.start();
    }

    public void _init() throws Exception {
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

    public String getInternalUserIdByExternalId(String userName) {
        if (this.producerWebServices == null) {
            try {
                producerWebServices = (new ProducerWebServices_Service(new URL(wsdlUrlProducer), new QName("http://dps.exlibris.com/", "ProducerWebServices"))).getProducerWebServicesPort();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return this.producerWebServices.getInternalUserIdByExternalId(userName);
    }

    public List<Producer> getProducers(String depositUserName) throws Exception {
        try {
            if (this.producerWebServices == null) {
                producerWebServices = (new ProducerWebServices_Service(new URL(wsdlUrlProducer), new QName("http://dps.exlibris.com/", "ProducerWebServices"))).getProducerWebServicesPort();
            }

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
            String err = String.format("Failed to get producers: depositUserName=%s: %s", depositUserName, e.getMessage());
            System.out.println(err);
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
            if (this.producerWebServices == null) {
                producerWebServices = (new ProducerWebServices_Service(new URL(wsdlUrlProducer), new QName("http://dps.exlibris.com/", "ProducerWebServices"))).getProducerWebServicesPort();
            }

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
            String err = String.format("Failed to get material flows: producerID=%s: %s", producerID, e.getMessage());
            System.out.println(err);
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
        if (pdsClient == null) {
            pdsClient = CustomizedPdsClient.getInstance();
            pdsClient.init(pdsUrl, false);
        }

        if (this.depositWebServices == null) {
            this.depositWebServices = (new DepositWebServices_Service(new URL(wsdlUrlDeposit), new QName("http://dps.exlibris.com/", "DepositWebServices"))).getDepositWebServicesPort();
        }

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
            String err = String.format("Invalid producer: %s", paramsBuf);
            System.out.println(err);
            throw new Exception("Invalid producer: " + paramsBuf);
        }

        if (!isValidMaterialFlow(depositUserProducerId, materialFlowID)) {
            String err = String.format("Invalid material flow: %s", paramsBuf);
            System.out.println(err);
            throw new Exception("Invalid material flow: " + paramsBuf);
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
            String err = String.format("Deposit failed: %s: %s", paramsBuf, e.getMessage());
            System.out.println(err);
            throw e;
        }
    }

    public SipStatusInfo getSIPStatusInfo(String sipId) throws Exception {
        try {
            if (sipWebServices == null) {
                sipWebServices = (new SipWebServices_Service(new URL(wsdlUrlSip), new QName("http://dps.exlibris.com/", "SipWebServices"))).getSipWebServicesPort();
            }
            return sipWebServices.getSIPStatusInfo(sipId, false);
        } catch (Exception_Exception e) {
            String err = String.format("Failed to get SIP StatusInfo: %s: %s", sipId, e.getMessage());
            System.out.println(err);
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
        if (this.depositWebServices == null) {
            try {
                this.depositWebServices = (new DepositWebServices_Service(new URL(wsdlUrlDeposit), new QName("http://dps.exlibris.com/", "DepositWebServices"))).getDepositWebServicesPort();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return this.depositWebServices.getDepositActivityBySubmitDateByMaterialFlow(pdsHandle, depositActivityStatus, materialFlowId, producerID, producerAgentID, updateDateFrom, updateDateTo, startRecord, endRecord);
    }

    public CustomizedPdsClient getPdsClient() {
        if (pdsClient == null) {
            pdsClient = CustomizedPdsClient.getInstance();
            pdsClient.init(pdsUrl, false);
        }
        return pdsClient;
    }

    public ProducerWebServices getProducerWebServices() {
        if (this.producerWebServices == null) {
            try {
                producerWebServices = (new ProducerWebServices_Service(new URL(wsdlUrlProducer), new QName("http://dps.exlibris.com/", "ProducerWebServices"))).getProducerWebServicesPort();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return producerWebServices;
    }

    public DepositWebServices getDepositWebServices() {
        if (this.depositWebServices == null) {
            try {
                this.depositWebServices = (new DepositWebServices_Service(new URL(wsdlUrlDeposit), new QName("http://dps.exlibris.com/", "DepositWebServices"))).getDepositWebServicesPort();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return depositWebServices;
    }

    public SipWebServices getSipWebServices() {
        if (sipWebServices == null) {
            try {
                sipWebServices = (new SipWebServices_Service(new URL(wsdlUrlSip), new QName("http://dps.exlibris.com/", "SipWebServices"))).getSipWebServicesPort();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return sipWebServices;
    }
}
