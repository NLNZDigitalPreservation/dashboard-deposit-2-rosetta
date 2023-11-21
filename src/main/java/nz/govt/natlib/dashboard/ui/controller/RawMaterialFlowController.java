package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.core.dto.DtoMaterialFlowRsp;
import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import nz.govt.natlib.dashboard.ui.command.RawMaterialFlowCommand;
import nz.govt.natlib.dashboard.ui.command.RawProducerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RawMaterialFlowController {
    private static final Logger log = LoggerFactory.getLogger(RawMaterialFlowController.class);

    @Autowired
    private RosettaWebService rosettaWebService;

    @Autowired
    private RepoDepositAccount repoDepositAccount;


    @RequestMapping(path = DashboardConstants.PATH_RAW_PRODUCER_MATERIAL_FLOW, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getRawProducersAndMaterialFlows(@RequestParam("depositAccountId") Long depositAccountId) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(depositAccountId);
        if (depositAccount == null) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg(String.format("There is no Deposit Account related to the id [%d]", depositAccountId));
            return rstVal;
        }


        try {
//            List<DtoProducersRsp.Producer> producers = rosettaWebService.getProducers(depositAccount);
            List<DtoProducersRsp.Producer> producers = depositAccount.getProducers();
            List<RawProducerCommand> rawProducerList = producers.stream().map(producer -> {
                RawProducerCommand producerCmd = new RawProducerCommand();
                producerCmd.setId(producer.getId());
                producerCmd.setName(producer.getName());
                return producerCmd;
            }).collect(Collectors.toList());
            producers.clear();

//            for (RawProducerCommand producerCmd : rawProducerList) {
//                List<DtoMaterialFlowRsp.MaterialFlow> materialFlows = rosettaWebService.getMaterialFlows(depositAccount, producerCmd.getId());
//                List<RawMaterialFlowCommand> rawMaterialFlowList = materialFlows.stream().map(flow -> {
//                    RawMaterialFlowCommand flowCmd = new RawMaterialFlowCommand();
//                    flowCmd.setId(flow.getId());
//                    flowCmd.setName(flow.getName());
//                    return flowCmd;
//                }).collect(Collectors.toList());
//                materialFlows.clear();
//
//                producerCmd.setMaterialFlows(rawMaterialFlowList);
//            }
            rstVal.setRspBody(rawProducerList);
            rawProducerList.forEach(RawProducerCommand::clear);
            rawProducerList.clear();
        } catch (Exception e) {
            log.error("Failed to get raw producers", e);
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg(String.format("Failed to get the raw producers related to the id [%d]", depositAccountId));
        }
        return rstVal;
    }
}
