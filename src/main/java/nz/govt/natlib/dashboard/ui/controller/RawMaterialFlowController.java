package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.core.dto.DtoMaterialFlowRsp;
import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RawMaterialFlowController {
    private static final Logger log = LoggerFactory.getLogger(RawMaterialFlowController.class);

    @Autowired
    private RosettaWebService rosettaWebService;

    @Autowired
    private RepoDepositAccount repoDepositAccount;

    @RequestMapping(path = DashboardConstants.PATH_RAW_PRODUCERS, method = {RequestMethod.POST, RequestMethod.GET})
    public List<DtoProducersRsp.Producer> getRawProducers(@RequestParam("depositAccountId") Long depositAccountId) {
        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(depositAccountId);
        if (depositAccount == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("There is no Deposit Account related to the id [%d]", depositAccountId));
        }
        if (depositAccount.getProducers() == null) {
            return new ArrayList<>();
        } else {
            return depositAccount.getProducers();
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_RAW_MATERIAL_FLOWS, method = {RequestMethod.POST, RequestMethod.GET})
    public List<DtoMaterialFlowRsp.MaterialFlow> getRawMaterialFlows(@RequestParam("depositAccountId") Long depositAccountId, @RequestParam("producerId") String producerId) throws Exception {
        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(depositAccountId);
        if (depositAccount == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("There is no Deposit Account related to the id [%d]", depositAccountId));
        }
        return rosettaWebService.getMaterialFlows(depositAccount, producerId);
    }
}
