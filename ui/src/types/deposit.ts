interface MaterialFlow {
    id: number;
    actualContentBackupOptions: string;
    actualContentDeleteOptions: string;
    auditMsg: string;
    auditRst: boolean;
    backupEnabled: boolean;
    backupPath: string;
    backupSubFolders: string;
    delayUnit: string;
    delays: number;
    depositAccountId: number;
    enabled: boolean;
    injectionCompleteFileName: string;
    materialFlowId: string;
    materialFlowName: string;
    maxActiveDays: number;
    maxSaveDays: number;
    producerId: string;
    producerName: string;
    rootPath: string;
}

interface DepositJob {
    id: number;
    actualContentDeleted: boolean;
    itMsg: string;
    auditRst: boolean;
    backupCompleted: boolean;
    depositEndTime: number;
    depositSetId: string;
    depositStartTime: number;
    fileCount: number;
    fileSize: number;
    initialTime: number;
    injectionPath: string;
    injectionTitle: string;
    latestTime: number;
    resultMessage: string;
    sipID: string;
    sipModule: string;
    sipStage: string;
    sipStatus: string;
    stage: string;
    state: string;
    successful: boolean;
    appliedFlowSetting: MaterialFlow;
}

interface JobQueryCondition {
    startTime: number;
    endTime: number;
    materialFlows: number[];
    stages: string[];
    states: string[];
}
