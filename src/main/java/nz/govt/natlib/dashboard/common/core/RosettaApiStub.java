package nz.govt.natlib.dashboard.common.core;

import com.exlibris.dps.SipStatusInfo;
import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;

import java.util.ArrayList;
import java.util.List;

public class RosettaApiStub implements RosettaApi {
    private static final String INSTITUTION = "INS00";
    private static final String USERNAME = "myself";
    private static final String PASSWORD = "******";

    private static final String PDSHANDLE = "ACE3-54FG-ACE3-54FG-ACE3-54FG";

    private static final String SIPID = "87430354";

    @Override
    public String login(String institution, String username, String password) throws Exception {
        return RosettaApiStub.PDSHANDLE;
    }

    @Override
    public String logout(String pdsHandle) throws Exception {
        return null;
    }

    @Override
    public PdsUserInfo getPdsUserByPdsHandle(String pdsHandle) throws Exception {
        PdsUserInfo info = new PdsUserInfo();
        info.setUserId(RosettaApiStub.USERNAME);
        info.setUserName(RosettaApiStub.USERNAME);
        info.setPid(RosettaApiStub.PDSHANDLE);
        return info;
    }

    @Override
    public String getInternalUserIdByExternalId(String userName) {
        return RosettaApiStub.PDSHANDLE;
    }

    @Override
    public List<Producer> getProducers(String depositUserName) throws Exception {
        Producer p = new Producer("31415926", "Pi");
        List<Producer> l = new ArrayList<>();
        l.add(p);
        return l;
    }

    @Override
    public boolean isValidProducer(String depositUserName, String producerId) throws Exception {
        return true;
    }

    @Override
    public List<MaterialFlow> getMaterialFlows(String producerID) throws Exception {
        MaterialFlow m = new MaterialFlow("31415926", "Pi");
        List<MaterialFlow> l = new ArrayList<>();
        l.add(m);
        return l;
    }

    @Override
    public boolean isValidMaterialFlow(String producerId, String materialFlowId) throws Exception {
        return true;
    }

    @Override
    public ResultOfDeposit deposit(String injectionRootDirectory, String pdsHandle, String depositUserInstitution, String depositUserProducerId, String materialFlowID, String depositSetID) throws Exception {
        ResultOfDeposit ret = new ResultOfDeposit(true, "OK", SIPID);
        return ret;
    }

    @Override
    public SipStatusInfo getSIPStatusInfo(String sipId) throws Exception {
        SipStatusInfo info = new SipStatusInfo();
        info.setSipId(SIPID);
        info.setStatus("SUCCESS");
        info.setStage("Finished");
        info.setModule("AUTO");
        return info;
    }
}
