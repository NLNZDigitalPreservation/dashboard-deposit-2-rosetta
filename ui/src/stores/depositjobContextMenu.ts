import { ref } from "vue";

export const selectedContextJob = ref();
export const contextMenuModel = ref([
    {label: 'Detail', icon: 'pi pi-fw pi-info-circle', command: () => viewProduct(selectedContextJob)},
    {separator: true},
    {label: 'Retry', icon: 'pi pi-fw pi-refresh', command: () => deleteProduct(selectedContextJob)},
    {label: 'Cancel', icon: 'pi pi-fw pi-times-circle', command: () => deleteProduct(selectedContextJob)},
    {separator: true},
    {label: 'Pause', icon: 'pi pi-fw pi-pause-circle', command: () => deleteProduct(selectedContextJob)},
    {label: 'Resume', icon: 'pi pi-fw pi-play-circle', command: () => deleteProduct(selectedContextJob)},
    {separator: true},
    {label: 'Terminate and Purge', icon: 'pi pi-fw pi-stop-circle', command: () => deleteProduct(selectedContextJob)},
]);


function getErrorMessageForAction(action, rowData){
    action=action.toUpperCase();
    var stage=rowData.stage, state=rowData.state;
    switch(action){
        case 'PAUSE':
            return 'The jobs with INGEST-RUNNING, DEPOSIT-INITIALED and FINALIZE-RUNNING status can be paused.';
        case 'RESUME':
            return 'The jobs with PAUSED status can be resumed.';
        case 'RETRY':
            return 'The jobs with DEPOSIT-FAILED status can be retried';
        case 'TERMINATE':
            return 'The jobs with FAILED, CANCELED and FINISHED-SUCCEED status can be terminated.';
        case 'CANCEL':
            return 'The RUNNING jobs can NOT be canceled.';
        default:
            return 'Unknown action: ' + action;
    }
}

function isRowDataValidForAction(action, rowData){
    action=action.toUpperCase();
    var stage=rowData.stage, state=rowData.state;
    switch(action){
        case 'PAUSE':
            return ((stage==='INGEST' || stage==='FINALIZE') && state==='RUNNING') || (stage==='DEPOSIT' && state==='INITIALED');
        case 'RESUME':
            return state==='PAUSED';
        case 'RETRY':
            return stage==='DEPOSIT' && state==='FAILED';
        case 'TERMINATE':
            return (stage==='FINISHED' && state==='SUCCEED') || (state==='FAILED') || (state==='CANCELED');
        case 'CANCEL':
            return state!=='RUNNING' && state!=="CANCELED";
        default:
            return false;
    }
}

var gReqNodes;
var gAction;
function handleDepositJobActive(action, selectedRow){
    var selectedRows=gridDepositJobs.getSelectedData();
    if(selectedRows.length===0 && selectedRow && selectedRow.getData()){
        selectedRows=[selectedRow.getData()];
    }

    action = action.toUpperCase();
    var req=[];
    for(var idx=0; idx<selectedRows.length;idx++){
        var rowData=selectedRows[idx];
        if(!isRowDataValidForAction(action, rowData)){
            continue;
        }
        req.push(rowData);
    }

    if(selectedRows.length > 1){
        if(req.length == 0){
            gReqNodes=null;
            showUnavailableActionAlert("The selected jobs are NOT allowed to apply the " + action + " action. The available status for " + action + " are shown below:", action);
            return;
        }else if(req.length < selectedRows.length){
            gReqNodes=req;
            gAction=action;
            showUnavailableActionAlert("Some of the selected jobs are NOT allowed to apply the " + action + " action. Click 'Confirm' to continue. </br> The available status for " + action + " are shown below:", action, continueProcessDepositJobAction);
            return;
        }
    }else if(selectedRows.length == 1 && req.length < selectedRows.length){
        gReqNodes=null;
        var errMsg=getErrorMessageForAction(action, selectedRows[0]);
        showUnavailableActionAlert("The job is NOT allowed to apply the " + action + " action. The available status for " + action + " are shown below:", action);
        return;
    }
    gReqNodes = req;
    gAction=action;
    continueProcessDepositJobAction(req, action);
}
