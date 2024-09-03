package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class RawMaterialFlowController {
    private static final Logger log = LoggerFactory.getLogger(RawMaterialFlowController.class);

    @Autowired
    private RosettaWebService rosettaWebService;

    @Autowired
    private RepoDepositAccount repoDepositAccount;

    @RequestMapping(path = DashboardConstants.PATH_RAW_PRODUCERS, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getRawProducers(@RequestBody RawProducersCommand cmd) {
        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(cmd.depositAccountId);
        if (depositAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("There is no Deposit Account related to the id [%d]", cmd.depositAccountId));
        }
        try {
            String ret = rosettaWebService.getProducers(depositAccount, cmd.limit, cmd.offset, cmd.name);
            return ResponseEntity.ok().body(ret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_RAW_PRODUCER_PROFILE, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getRawProducerProfile(@RequestBody RawProducerProfileCommand cmd) {
        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(cmd.depositAccountId);
        if (depositAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("There is no Deposit Account related to the id [%d]", cmd.depositAccountId));
        }
        try {
            String ret = rosettaWebService.getProducerProfileId(depositAccount, cmd.producerId);
            if(StringUtils.isEmpty(ret)){
                return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok().body(ret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_RAW_MATERIAL_FLOWS, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getRawMaterialFlows(@RequestBody RawMaterialFlowsCommand cmd) throws Exception {
        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(cmd.depositAccountId);
        if (depositAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("There is no Deposit Account related to the id [%d]", cmd.depositAccountId));
        }
        try {
            String ret = rosettaWebService.getMaterialFlows(depositAccount, cmd.producerId, cmd.limit, cmd.offset, cmd.name);
            return ResponseEntity.ok().body(ret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class RawProducersCommand {
        private long depositAccountId;
        private int offset;
        private int limit;
        private String name;

        public long getDepositAccountId() {
            return depositAccountId;
        }

        public void setDepositAccountId(long depositAccountId) {
            this.depositAccountId = depositAccountId;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public static class RawProducerProfileCommand {
        private long depositAccountId;
        private String producerId;

        public long getDepositAccountId() {
            return depositAccountId;
        }

        public void setDepositAccountId(long depositAccountId) {
            this.depositAccountId = depositAccountId;
        }

        public String getProducerId() {
            return producerId;
        }

        public void setProducerId(String producerId) {
            this.producerId = producerId;
        }
    }
    public static class RawMaterialFlowsCommand {
        private long depositAccountId;
        private String producerId;
        private int offset;
        private int limit;
        private String name;

        public long getDepositAccountId() {
            return depositAccountId;
        }

        public void setDepositAccountId(long depositAccountId) {
            this.depositAccountId = depositAccountId;
        }

        public String getProducerId() {
            return producerId;
        }

        public void setProducerId(String producerId) {
            this.producerId = producerId;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

