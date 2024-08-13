import { ref, markRaw } from "vue";
import { useDialog } from 'primevue/usedialog';
import { defineStore } from "pinia";
import DepositJobDetailDialogFooter from "@/components/jobs/DepositJobDetailDialogFooter.vue";
import DepositJobDetailDialog from '@/components/jobs/DepositJobDetailDialog.vue';
import DepositJobAvailableActionsDialog from "@/components/jobs/DepositJobAvailableActionsDialog.vue";
import { useJobListDTO, keywords } from "@/stores/depositjob";

export const useContextMenu=defineStore('ContextMenu', ()=>{
    const dialog=useDialog();
    const selectedContextRow = ref();
    const contextMenuModel = ref([
        {label: 'Detail', icon: 'pi pi-fw pi-info-circle', command: () => viewJob(dialog, selectedContextRow)},
        {separator: true},
        {label: 'Retry', icon: 'pi pi-fw pi-refresh', command: () => editJob(dialog, 'retry', selectedContextRow)},
        {label: 'Cancel', icon: 'pi pi-fw pi-times-circle', command: () => editJob(dialog, 'cancel', selectedContextRow)},
        {separator: true},
        {label: 'Pause', icon: 'pi pi-fw pi-pause-circle', command: () => editJob(dialog, 'pause', selectedContextRow)},
        {label: 'Resume', icon: 'pi pi-fw pi-play-circle', command: () => editJob(dialog, 'resume', selectedContextRow)},
        {separator: true},
        {label: 'Terminate and Purge', icon: 'pi pi-fw pi-stop-circle', command: () => editJob(dialog, 'terminate', selectedContextRow)},
    ]);

    return {selectedContextRow, contextMenuModel}
});

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
            data: rowData.value,
        },
        templates: {
            footer: markRaw(DepositJobDetailDialogFooter)
        },
    });
}

const editJob=(dialog:any, action:string, rowData:any)=>{
    let selectedRows=[];
    const jobList = useJobListDTO();
    if(jobList.selectedJobs && jobList.selectedJobs.length > 0){
        selectedRows=jobList.selectedJobs;
    }else{
        selectedRows.push(rowData.value);
    }

    action = action.toUpperCase();

    const req=[];
    for(let rowData of selectedRows){
        if(!isRowDataValidForAction(action, rowData)){
            continue;
        }
        req.push(rowData);
    }
    
    let isValid=true;
    let needConfirm=false;
    let errMsg = '';
    if(selectedRows.length > 1){
        if(req.length == 0){
            isValid=false;
            errMsg="The selected jobs are NOT allowed to apply the " + action + " action. The available status for " + action + " are shown below:";
        }else if(req.length < selectedRows.length){
            isValid=false;
            needConfirm=true;
            errMsg="Some of the selected jobs are NOT allowed to apply the " + action + " action. Click 'Confirm' to continue. The available status for " + action + " are shown below:";
        }
    }else if(selectedRows.length == 1 && req.length < selectedRows.length){
        isValid=false;
        errMsg="The job is NOT allowed to apply the " + action + " action. The available status for " + action + " are shown below:";
    }
    
    // continueProcessDepositJobAction(req, action);




    const dialogRef = dialog.open(DepositJobAvailableActionsDialog, {
        props: {
            // header: 'Warning',
            closable: false,
            style: {
                width: '50rem',
            },
            modal: true,
        },
        data: {
            data: action,
        },
        templates: {
            // header: markRaw(DepositJobAvailableActionsDialog),
            // footer: markRaw(DepositJobDetailDialogFooter)
        },
    });
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
