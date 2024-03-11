var contextMenuDepositJobsActive = [
    {
        label:"<i class='bi bi-info-circle text-success'>&nbsp;</i> Detail",
        action:function(e, row){
            setValueDepositJobActive(row.getData());
            modalDepositJobDetails.show();
        }
    },
    {separator:true},
    {
        label:"<i class='bi bi-arrow-clockwise text-success'>&nbsp;</i> Retry",
        action:function(e, row){
            handleDepositJobActive("retry",row);
        }
    },
    {
        label:"<i class='bi bi-x-circle text-success'>&nbsp;</i> Cancel",
        action:function(e, row){
            handleDepositJobActive("cancel",row);
        }
    },
    {separator:true},
    {
        label:"<i class='bi bi-pause-circle text-success'>&nbsp;</i> Pause",
        action:function(e, row){
            handleDepositJobActive("pause",row);
        }
    },
    {
        label:"<i class='bi bi-play-circle text-success'>&nbsp;</i> Resume",
        action:function(e, row){
            handleDepositJobActive("resume",row);
        }
    },
    {separator:true},
    {
        label:"<i class='bi bi-stop-circle text-success'>&nbsp;</i> Terminate and Purge",
        action:function(e, row){
            handleDepositJobActive("terminate",row);
        }
    }
]

const gridDepositJobsColumnsActive=[
    {formatter:"rowSelection", width:5, titleFormatter:"rowSelection", hozAlign:"center", headerSort:false, cellClick:function(e, cell){
        cell.getRow().toggleSelect();
    }},
    {title: "ID", field: "id", width: 90},
    {title: "Producer Name", field: "producerName", width: 350},
    {title: "Materialflow Name", field: "materialName", width: 350},
    {title: "JobTitle", field: "injectionTitle", width: 485},
    {title: "Stage", field: "stage", width: 120},
    {title: "State", field: "state", width: 120},
    {title: "JobInitialTime", field: "initialTime", width: 200, formatter: formatCellDatetimeFromEpochMilliSeconds},
    {title: "LatestUpdateTime", field: "latestTime", width: 200, formatter: formatCellDatetimeFromEpochMilliSeconds},
    {title: "Progress", field: "statusLatest", width: 120, formatter: function(cell){
        var data=cell.getRow().getData();
        var stage=data.stage, state=data.state;
        var percent=calcPercentActive(stage, state);
        return getProgressDIVActive(stage, state, percent);
    }},
    {title: "NumOfFiles", field: "fileCount", width: 160},
    {title: "SizeOfFiles", field: "fileSize", width: 160, formatter: function(cell){
        return formatContentLength(cell.getValue());
    }},
    {title: "DepositStartTime", field: "depositStartTime", width: 200, formatter: formatCellDatetimeFromEpochMilliSeconds},
    {title: "DepositEndTime", field: "depositEndTime", width: 200, formatter: formatCellDatetimeFromEpochMilliSeconds},
    {title: "SipId", field: "sipID", width: 160},
    {title: "SipModule", field: "sipModule", width: 160},
    {title: "SipStage", field: "sipStage", width: 160},
    {title: "SipStatus", field: "sipStatus", width: 160},
];

function formatDatasets(dataNodes){
    if (!dataNodes){
        return [];
    }

    for (var i=0; i<dataNodes.length; i++){
        var node=dataNodes[i];
        node["producerName"]=node.appliedFlowSetting.producerName;
        node["materialName"]=node.appliedFlowSetting.materialFlowName;
    }

    return dataNodes;
}

function formatCellDatetimeFromEpochMilliSeconds(cell){
    return formatDatetimeFromEpochMilliSeconds(cell.getValue());
}

function getProgressDIVActive(stage, state, percent){
    if(stage==='FINISHED' && state==='CANCELED'){
        return '<div style="width: calc(100% + 44px); height:100%; margin-left:-22px; text-align: center; background: linear-gradient(to right, rgb(40,167,69, 0.8) ' + 0 + '%, rgba(192,192,192,0.45) ' + 0 + '% 100%);">' + percent + '%</div>';
    }else{
        return '<div style="width: calc(100% + 44px); height:100%; margin-left:-22px; text-align: center; background: linear-gradient(to right, rgb(40,167,69, 0.8) ' + percent + '%, rgba(192,192,192,0.5) ' + percent + '% 100%);">' + percent + '%</div>';
    }
}

function calcPercentActive(stage, state){
    var percent=0;
    if (stage==='INGEST') {
        percent=0;
    }else if(stage==='DEPOSIT'){
        percent=33.33;
    }else if(stage==='FINALIZE'){
        percent=66.77;
    }else{
        percent=100;
    }


    var percentState=0.0;
    if (state==='INITIALED') {
        percentState=0.1;
    }else if(state==='RUNNING' || state==='PAUSED'){
        percentState=0.5;
    }else{
        percentState=1.0;
    }

    percent+=percentState*33.33;

    percent=percent.toFixed(2);

    if (percent>99) {
        percent=100;
    }

    return percent;
}

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

 function continueProcessDepositJobAction(reqNodes, action){
    if (!reqNodes || reqNodes.length===0) {
        return;
    }

    if(action.toUpperCase() ==='TERMINATE' && !confirm("The selected jobs and the related actual contents will be forced to be terminated and purged. Would you like to continue?")){
        return;
    }

    fetchHttp(PATH_DEPOSIT_JOBS_UPDATE + '?action='+action, reqNodes, function(rspNodes){
          gridDepositJobs.updateData(rspNodes);
    });
}

function submitNewDepositJob(){
    var req={};
    req['flowId']=$('#new-job-select-material-flow option:selected').attr('value');
    req['flowName']=$('#new-job-select-material-flow').val();
    req['nfsDirectory']=$('#input-nfs-directory').val();
    req['forcedReplaceExistingJob']=$('#forcedReplaceExistingJob').is(':checked');
    fetchHttp(PATH_DEPOSIT_JOBS_NEW, req, function(job){
        toastr.info("The job is scheduled.");
        modalDepositJobManualNew.hide();
    });
}

const PROGRESS_BAR_BG=['bg-none', 'bg-initialed', 'bg-running', 'bg-paused', 'bg-finished'];
function setProgressBarStyle(objFilter, className, stage, state){
    for(var i=0; i<PROGRESS_BAR_BG.length; i++){
        $(objFilter).removeClass(PROGRESS_BAR_BG[i]);
    }
    $(objFilter).addClass(className);
    $(objFilter).html(stage + ' (' + state + ')');
}

function setValueDepositJobActive(data){
    if (!data) {return false;}

    if((data.resultMessage && data.resultMessage !== "") || data.state==='FAILED'){
        $('#deposit-job-health').show();
        $('#deposit-job-health').html('<i class="bi bi-exclamation-triangle-fill text-danger"></i>&nbsp;' + data.resultMessage)
    }else{
        $('#deposit-job-health').hide();
    }


    $('#deposit-job-title').val(data['injectionTitle']);
    $('#deposit-job-path').val(data['injectionPath']);
    $('#deposit-job-flow-name').val(data['appliedFlowSetting']['materialFlowName']);
    $('#deposit-job-stage').val(data['stage']);
    $('#deposit-job-state').val(data['state']);
    // $('#deposit-job-percent').val(calcPercentActive(data['stage'], data['state'])+'%');
    $('#deposit-job-initial-time').val(formatDatetimeFromEpochMilliSeconds(data['initialTime']));
    $('#deposit-job-latest-time').val(formatDatetimeFromEpochMilliSeconds(data['latestTime']));
    $('#deposit-job-deposit-start').val(formatDatetimeFromEpochMilliSeconds(data['depositStartTime']));
    $('#deposit-job-deposit-end').val(formatDatetimeFromEpochMilliSeconds(data['depositEndTime']));
    $('#deposit-job-sip-id').val(data['sipID']);
    $('#deposit-job-sip-module').val(data['sipModule']);
    $('#deposit-job-sip-stage').val(data['sipStage']);
    $('#deposit-job-sip-status').val(data['sipStatus']);
    $('#deposit-job-sip-msg').val(data['resultMessage']);
    $('#deposit-job-file-count').val(data['fileCount']);
    $('#deposit-job-file-size').val(formatContentLength(data['fileSize']) + ' (' + data['fileSize'] + ' bytes)');

    var stage=data['stage'], state=data['state'];
    var percent=0;
    
    var progress='bg-none';
    if (state==='INITIALED') {
        progress='bg-initialed';
    }else if(state==='RUNNING' || state==='PAUSED'){
        progress='bg-running';
    }else{
        progress='bg-finished';
    }

    if (stage==='INGEST') {
        setProgressBarStyle('#progress-stage-name div[name="inject"]', progress, 'INGEST', state);
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', 'bg-none', 'DEPOSIT', '...' );
        setProgressBarStyle('#progress-stage-name div[name="finalize"]', 'bg-none', 'FINALIZE', '...');
    }else if(stage==='DEPOSIT'){
        setProgressBarStyle('#progress-stage-name div[name="inject"]', 'bg-finished', 'INGEST', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', progress, 'DEPOSIT', state);
        setProgressBarStyle('#progress-stage-name div[name="finalize"]', 'bg-none', 'FINALIZE', '...');
    }else if(stage==='FINALIZE'){
        setProgressBarStyle('#progress-stage-name div[name="inject"]', 'bg-finished', 'INGEST', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', 'bg-finished', 'DEPOSIT', 'FINISHED');
        if (progress!=='bg-finished') {
            setProgressBarStyle('#progress-stage-name div[name="finalize"]', progress, 'FINALIZE', '...');
        }else{
            setProgressBarStyle('#progress-stage-name div[name="finalize"]', progress, 'FINALIZE', 'FINISHED');
        }
    }else{
        setProgressBarStyle('#progress-stage-name div[name="inject"]', 'bg-finished', 'INGEST', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', 'bg-finished', 'DEPOSIT', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="finalize"]', 'bg-finished', 'FINALIZE', state);
    }

    return true;
}

const modalDepositJobDetails = new bootstrap.Modal(document.getElementById('deposit-job-details'), {keyboard: false});
const modalDepositJobManualNew = new bootstrap.Modal(document.getElementById('deposit-job-details'), {keyboard: false});
const modalDepositJobSearch = new bootstrap.Modal(document.getElementById('search-deposit-job'), {keyboard: false});

const gridDepositJobs=new Tabulator('#grid-deposit-jobs', {
//    height: "100%",
    index:"id",
    data:[],
    layout: 'fitDataStretch',
//    groupBy: ['producerName', 'materialName'],
    rowContextMenu: contextMenuDepositJobsActive, //add context menu to rows
    columns: gridDepositJobsColumnsActive,
});

var jobStartDate=moment().subtract(14, 'days'), jobEndDate=moment();
function initDepositJob(){
    var template=$('#template-flow-setting').html();
    $('#ul-flow-setting-job-applied').html(template);

    $('#search-dt-deposit-job').daterangepicker(
      { timePicker: false,
        startDate: jobStartDate,
        endDate: jobEndDate,
        locale: {
          format: 'MMM/DD/YYYY'
        }
      },
      function (start, end) {
        // $('#reportrange span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'))
        console.log(start+"~"+end);
        jobStartDate=start;
        jobEndDate=end;
      }
    );

    $('#filter-deposit-job').on('input', function(){
        var val=$(this).val();
        if(!val || val.toString().length===0){
            gridDepositJobs.clearFilter();
            return
        }

        //Filter all the fields which match the input
        gridDepositJobs.setFilter(function(data, param){
            param=param.toString().toUpperCase();
            content=JSON.stringify(data).toUpperCase();
            return content.includes(param);
        }, val);
    });
    
    fetchHttp(PATH_DEPOSIT_JOBS_ACTIVE_GET, null, function(rsp){
        var datasets=formatDatasets(rsp);
        gridDepositJobs.replaceData(datasets);
    });

    $('#deposit-job-details input').prop('disabled', true);
    $('#deposit-job-details select').prop('disabled', true);
}

function searchDepositJob(){
    var req={
        dtStart: moment(jobStartDate).valueOf(),
        dtEnd: moment(jobEndDate).valueOf()
    };

    var flowIds=[];
    $('#search-select-material-flow input:checked').each(function(){
        flowIds.push($(this).attr('flowId'));
    });
    req['flowIds']=flowIds;

    var stages=[];
    $('#search-select-stages input:checked').each(function(){
        stages.push($(this).attr('name'));
    });
    req['stages']=stages;

    var states=[];
    $('#search-select-states input:checked').each(function(){
        states.push($(this).attr('name'));
    });
    req['states']=states;

    fetchHttp(PATH_DEPOSIT_JOBS_SEARCH, req, function(rsp){
        var datasets=formatDatasets(rsp);
        gridDepositJobs.replaceData(datasets);
        modalDepositJobSearch.hide();
    });
}

function groupByDepositJobs(){
    var producerName=$('#flexCheckProducerName').is(':checked');
    var materialFlowName=$('#flexCheckMaterialFlowName').is(':checked');
    var stage=$('#flexCheckStage').is(':checked');
    var state=$('#flexCheckState').is(':checked');

    var groupByFields=[];
    if(producerName){
        groupByFields.push('producerName');
    }
    if(materialFlowName){
        groupByFields.push('materialName');
    }
    if(stage){
        groupByFields.push('stage');
    }
    if(state){
        groupByFields.push('state');
    }

    if(groupByFields.length===0){
        gridDepositJobs.setGroupBy(false);
    }else{
        gridDepositJobs.setGroupBy(groupByFields);
    }
}

function exportSelectedJobs(){
    var selectedRows=gridDepositJobs.getSelectedData();
    if(!selectedRows || selectedRows.length===0){
        toastr.warning("Please select some jobs!");
        return;
    }
    var req=[];
    for(var idx=0; idx<selectedRows.length;idx++){
        var job=selectedRows[idx];
        req.push(job.id);
    }

    fetch(PATH_DEPOSIT_JOBS_EXPORT_DATA, { 
        method: 'POST',
        redirect: 'follow',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(req)
    }).then((res) => {
        if (res.ok) {
            return res.blob();
        }
        return null;
    }).then((blob) => {
        if(blob){
            saveAs(blob, "deposit_jobs.xlsx");
        }
    });
}

const modalRedepositJob = new bootstrap.Modal(document.getElementById('redeposit-job'), {keyboard: false});
function showModalRedepositJob(){
    modalRedepositJob.show();
}

function redepositJob(){
    var subFolder=$("#subFolderRedepositJob").val();
    fetchHttp(PATH_DEPOSIT_JOBS_REDEPOSIT+"?subFolder="+subFolder, null, function(rsp){
        modalRedepositJob.hide();
        toastr.info("Succeed to redeposit the job. The sub-folder will be scanned automatically.")
    });
}