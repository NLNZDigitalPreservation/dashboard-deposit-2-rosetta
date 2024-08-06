import { ref } from "vue";
import { useDialog } from 'primevue/usedialog';
import { defineStore } from "pinia";

import DepositJobDetailDialog from '@/components/jobs/DepositJobDetailDialog.vue';

export const useContextMenu=defineStore('ContextMenu', ()=>{
    const dialog=useDialog();
    const selectedContextRow = ref();
    const contextMenuModel = ref([
        {label: 'Detail', icon: 'pi pi-fw pi-info-circle', command: () => viewJob(dialog, selectedContextRow)},
        {separator: true},
        {label: 'Retry', icon: 'pi pi-fw pi-refresh', command: () => editJob('retry', selectedContextRow)},
        {label: 'Cancel', icon: 'pi pi-fw pi-times-circle', command: () => editJob('cancel', selectedContextRow)},
        {separator: true},
        {label: 'Pause', icon: 'pi pi-fw pi-pause-circle', command: () => editJob('pause', selectedContextRow)},
        {label: 'Resume', icon: 'pi pi-fw pi-play-circle', command: () => editJob('resume', selectedContextRow)},
        {separator: true},
        {label: 'Terminate and Purge', icon: 'pi pi-fw pi-stop-circle', command: () => editJob('terminate', selectedContextRow)},
    ]);



    return {selectedContextRow, contextMenuModel}
});

const viewJob=(dialog:any, rowData:any)=>{
    if(!rowData){
        console.log("There is no row selected.");
        return;
    }

    const dialogRef = dialog.open(DepositJobDetailDialog, {
        props: {
            header: 'Deposit Job',
            closable: true,
            style: {
                width: '75rem',
            },
            modal: true,
        },
        data: {
            job: rowData.value,
        }
    });
}

const editJob=(action:string, rowData:any)=>{
    console.log(action);
}

const getErrorMessageForAction = (action:string, rowData:any) => {
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

const isRowDataValidForAction = (action:string, rowData:any) => {
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

const handleDepositJobActive = (action:string, selectedRow:any) => {
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
