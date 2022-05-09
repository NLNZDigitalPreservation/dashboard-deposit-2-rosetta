package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import nz.govt.natlib.dashboard.ui.command.RawMaterialFlowCommand;
import nz.govt.natlib.dashboard.ui.command.RawProducerCommand;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

public class RawMaterialFlowController {
    private static final Logger log = LoggerFactory.getLogger(RawMaterialFlowController.class);

    @Autowired
    private RosettaWebService rosettaWebService;

    @Autowired
    private RepoDepositAccount repoDepositAccount;


    @RequestMapping(path = DashboardConstants.PATH_RAW_PRODUCER_MATERIAL_FLOW, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getRawProducersAndMaterialFlows(@RequestParam("depositAccountId") Long depositAccountId) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityDepositAccountSetting depositAccountSetting = repoDepositAccount.getById(depositAccountId);
        if (depositAccountSetting == null) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg(String.format("There is no Deposit Account related to the id [%d]", depositAccountId));
            return rstVal;
        }

        String depositUsername = depositAccountSetting.getDepositUserName();
        if (StringUtils.isEmpty(depositUsername)) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("Please input the deposit username to query the producers");
            return rstVal;
        }

        try {
            List<Producer> producers = rosettaWebService.getProducers(depositUsername);
            List<RawProducerCommand> rawProducerList = producers.stream().map(producer -> {
                RawProducerCommand producerCmd = new RawProducerCommand();
                producerCmd.setId(producer.getID());
                producerCmd.setName(producer.getDescription());
                return producerCmd;
            }).collect(Collectors.toList());

            for (RawProducerCommand producerCmd : rawProducerList) {
                List<MaterialFlow> materialFlows = rosettaWebService.getMaterialFlows(producerCmd.getId());
                List<RawMaterialFlowCommand> rawMaterialFlowList = materialFlows.stream().map(flow -> {
                    RawMaterialFlowCommand flowCmd = new RawMaterialFlowCommand();
                    flowCmd.setId(flow.getID());
                    flowCmd.setName(flow.getDescription());
                    return flowCmd;
                }).collect(Collectors.toList());
                producerCmd.setMaterialFlows(rawMaterialFlowList);
            }
            rstVal.setRspBody(rawProducerList);
        } catch (Exception e) {
            log.error("Failed to get raw producers", e);
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg(String.format("Failed to get the raw producers related to the id [%d]", depositAccountId));
        }
        return rstVal;
    }
}
