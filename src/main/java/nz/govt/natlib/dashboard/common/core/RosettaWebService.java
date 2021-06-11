package nz.govt.natlib.dashboard.common.core;

import com.exlibris.digitool.repository.ifc.Collection;
import com.exlibris.dps.SipStatusInfo;
import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.metadata.MetsXmlProperties;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;

import java.util.List;

public interface RosettaWebService {
    String login(String institution, String username, String password) throws Exception;

    String logout(String pdsHandle) throws Exception;

    PdsUserInfo getPdsUserByPdsHandle(String pdsHandle) throws Exception;

    String getInternalUserIdByExternalId(String userName);

    List<Producer> getProducers(String depositUserName) throws Exception;

    boolean isValidProducer(String depositUserName, String producerId) throws Exception;

    List<MaterialFlow> getMaterialFlows(String producerID) throws Exception;

    boolean isValidMaterialFlow(String producerId, String materialFlowId) throws Exception;

    ResultOfDeposit deposit(String injectionRootDirectory, String pdsHandle, String depositUserInstitution, String depositUserProducerId, String materialFlowID, String depositSetID) throws Exception;

    SipStatusInfo getSIPStatusInfo(String sipId) throws Exception;

    Collection getCollectionByName(String pdsHandle, String path);

    String getDepositActivityBySubmitDateByMaterialFlow(String pdsHandle, String depositActivityStatus, String materialFlowId, String producerID, String producerAgentID, String updateDateFrom, String updateDateTo, String startRecord, String endRecord);

     int getNumberOfRecords(MetsXmlProperties prop);
}
