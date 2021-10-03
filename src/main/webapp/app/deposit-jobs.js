const contextMenuItemsDepositJobsActive={
    "detail": {name: "Detail", icon: "bi bi-info-circle"},
    "sep1": "---------",
    "retry": {name: "Retry", icon: "bi bi-arrow-clockwise", disabled: disableDepositJobContextMenuItemActive},
    "cancel": {name: "Cancel", icon: "bi bi-x-circle", disabled: disableDepositJobContextMenuItemActive},
    "sep2": "---------",
    "pause": {name: "Pause", icon: "bi bi-pause-circle", disabled: disableDepositJobContextMenuItemActive},
    "resume": {name: "Resume", icon: "bi bi-play-circle", disabled: disableDepositJobContextMenuItemActive},
    "sep3": "---------",
    "terminate": {name: "Terminate and Purge", icon: "bi bi-stop-circle", disabled: disableDepositJobContextMenuItemActive},
};

function disableDepositJobContextMenuItemActive(key, opt){
    var rowIndex=$(this).attr('row-index');
    var rowData = gridDepositJobs.getRowByIndex(parseInt(rowIndex));
    if (!rowData) {return;}
    var stage=rowData.stage, state=rowData.state;
    key=key.toUpperCase();

    switch(key){
        case 'PAUSE':
            return !(((stage==='INJECT' || stage==='FINALIZE') && state==='RUNNING') || (stage==='DEPOSIT' && state==='INITIALED'));
        case 'RESUME':
            return state!=='PAUSED';
        case 'RETRY':
            return !(stage==='DEPOSIT' && state==='FAILED');
        case 'TERMINATE':
            return (stage==='DEPOSIT' && state==='SUCCEED') || (stage==='FINALIZE' && state==='SUCCEED');
        case 'CANCEL':
            return !(state==='FAILED');
        default:
            return false;
    }
}

const gridDepositJobsColumnsActive=[
    // {headerName: "#", width:45, checkboxSelection: true, pinned: 'left'},
    {headerName: "ID", field: "id", width: 90, pinned: 'left'},
    {headerName: "Flow", field: "flowName", pinned: 'left', width: 120, cellRenderer: function(row){
        return row.data.appliedFlowSetting.name;
    }},
    {headerName: "JobTitle", field: "injectionTitle", width: 485, pinned: 'left'},

    {headerName: "Status", field: "state", width: 180, pinned: 'left', cellRenderer: function(row){
        var  stage=row.data.stage, state=row.data.state;
        var icon=cellFlagIconActive(stage, state);
        return icon;
    }},     
    {headerName: "StartTime", field: "initialTime", width: 200, cellRenderer: function(row){
        return formatDatetimeFromEpochMilliSeconds(row.data.initialTime);
    }},
    {headerName: "LatestUpdateTime", field: "latestTime", width: 200, cellRenderer: function(row){
       return formatDatetimeFromEpochMilliSeconds(row.data.latestTime);
    }},

    {headerName: "Progress", field: "statusLatest", width: 120, cellRenderer: function(row){
        var stage=row.data.stage, state=row.data.state;
        var percent=calcPercentActive(stage, state);
        return getProgressDIVActive(percent);
    }},

    {headerName: "NumOfFiles", field: "fileCount", width: 160},
    {headerName: "SizeOfFiles", field: "fileSize", width: 160, cellRenderer: function(row){
        return formatContentLength(row.data.fileSize);
    }},

    {headerName: "SipId", field: "sipID", width: 160},
    {headerName: "SipModule", field: "sipModule", width: 160},
    {headerName: "SipStage", field: "sipStage", width: 160},
    {headerName: "SipStatus", field: "sipStatus", width: 160},
    // {headerName: "Deposit Result", field: "resultMessage", width: 200},
];

function getProgressDIVActive(percent){
    return '<div style="width: calc(100% + 44px); height:100%; margin-left:-22px; text-align: center; background: linear-gradient(to right, rgb(40,167,69, 0.8) ' + percent + '%, rgba(192,192,192,0.5) ' + percent + '% 100%);">' + percent + '%</div>';
}

function cellFlagIconActive(stage, state){
    var icon;
    switch(state){
        case 'SUCCEED':
            if (stage==='FINALIZE') {
                icon = '<i class="bi bi-check-circle-fill text-success">&nbsp;</i>';
            }else{
                icon = '<i class="bi bi-cursor-fill text-primary">&nbsp;</i>';
            }
            break;
        case 'FAILED':
            icon = '<i class="bi bi-exclamation-circle-fill text-danger">&nbsp;</i>';
            break;
        case 'CANCELED':
            icon = '<i class="bi bi-x-circle-fill text-secondary">&nbsp;</i>';
            break;
        default:
            icon = '<i class="bi bi-cursor-fill text-primary">&nbsp;</i>';
    }

    return icon+stage+'-'+state;
}

function calcPercentActive(stage, state){
    var percent=0;
    if (stage==='INJECT') {
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


function handleDepositJobActive(action, selectedRow){
    if (action==='detail') {
        setValueDepositJobActive(selectedRow.data);
        modalDepositJobDetails.show();
        return;
    }

    if(action==='terminate' && !confirm("The job will be forced to terminate and purge. Are you sure you will continue?")){
        return;
    }

    var reqNodes=[];
    reqNodes.push(selectedRow.data);

    fetchHttp(PATH_DEPOSIT_JOBS_UPDATE + '?action='+action, reqNodes, function(rspNodes){
        // gridDepositJobs.clear(selectedNodes);
        var dataset=[], map={};
        for(var i=0; i<rspNodes.length; i++){
            map[rspNodes[i].injectionTitle]=rspNodes[i];
        }

        gridDepositJobs.grid.gridOptions.api.forEachNode(function(node, index){
            if (map[node.data.injectionTitle]) {
                node.data=map[node.data.injectionTitle];
                dataset.push(node.data);
            }
        });

        if(action==='terminate'){
            gridDepositJobs.removeByDataSet(dataset);
        }else{
            gridDepositJobs.update(dataset);
        }
    });
}

function searchDepoitJob(){
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
        gridDepositJobs.setRowData(rsp);
        modalDepositJobSearch.hide();
    });
}

function submitNewDepositJob(){
    var req={};
    req['flowId']=$('#new-job-select-material-flow option:selected').attr('value');
    req['flowName']=$('#new-job-select-material-flow').val();
    req['nfsDirectory']=$('#input-nfs-directory').val();
    req['forcedReplaceExistingJob']=$('#forcedReplaceExistingJob').is(':checked');
    fetchHttp(PATH_DEPOSIT_JOBS_NEW, req, function(job){
        // var dataset=[];
        // dataset.push(job);
        // gridDepositJobs.add(dataset);
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

    if (data.state==='FAILED') {
        $('#deposit-job-health').show();
        $('#deposit-job-health').html('<i class="bi bi-exclamation-triangle-fill text-danger"></i>&nbsp;' + data.resultMessage)
    }else{
        $('#deposit-job-health').hide();        
    }

    $('#deposit-job-title').val(data['injectionTitle']);
    $('#deposit-job-path').val(data['injectionPath']);
    $('#deposit-job-flow-name').val(data['appliedFlowSetting']['name']);
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

    if (stage==='INJECT') {
        setProgressBarStyle('#progress-stage-name div[name="inject"]', progress, 'INJECT', state);
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', 'bg-none', 'DEPOSIT', '...' );
        setProgressBarStyle('#progress-stage-name div[name="finalize"]', 'bg-none', 'FINALIZE', '...');
    }else if(stage==='DEPOSIT'){
        setProgressBarStyle('#progress-stage-name div[name="inject"]', 'bg-finished', 'INJECT', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', progress, 'DEPOSIT', state);
        setProgressBarStyle('#progress-stage-name div[name="finalize"]', 'bg-none', 'FINALIZE', '...');
    }else if(stage==='FINALIZE'){
        setProgressBarStyle('#progress-stage-name div[name="inject"]', 'bg-finished', 'INJECT', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', 'bg-finished', 'DEPOSIT', 'FINISHED');
        if (progress!=='bg-finished') {
            setProgressBarStyle('#progress-stage-name div[name="finalize"]', progress, 'FINALIZE', '...');
        }else{
            setProgressBarStyle('#progress-stage-name div[name="finalize"]', progress, 'FINALIZE', 'FINISHED');
        }
    }else{
        setProgressBarStyle('#progress-stage-name div[name="inject"]', 'bg-finished', 'INJECT', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="deposit"]', 'bg-finished', 'DEPOSIT', 'FINISHED');
        setProgressBarStyle('#progress-stage-name div[name="finalize"]', 'bg-finished', 'FINALIZE', state);
    }


    //Value appliedFlowSetting
    setValueMaterialFlow(data.appliedFlowSetting, '#ul-flow-setting-job-applied');

    return true;
}

const modalDepositJobDetails = new bootstrap.Modal(document.getElementById('deposit-job-details'), {keyboard: false});
const modalDepositJobSearch = new bootstrap.Modal(document.getElementById('search-deposit-job'), {keyboard: false});
const modalDepositJobManualNew = new bootstrap.Modal(document.getElementById('new-manual-deposit-job'), {keyboard: false});

const gridDepositJobs=new CustomizedAgGrid($('#grid-deposit-jobs')[0], Object.assign({columnDefs: gridDepositJobsColumnsActive}), null);

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
        gridDepositJobs.filter(val);
    });
    
    fetchHttp(PATH_DEPOSIT_JOBS_ACTIVE_GET, null, function(rsp){
        gridDepositJobs.setRowData(rsp);
    });

    $('#deposit-job-details input').prop('disabled', true);
    $('#deposit-job-details select').prop('disabled', true);

    $.contextMenu({
        selector: '#grid-deposit-jobs .ag-row', 
        trigger: 'right',
        callback: function(key, options) {
            var rowId=$(this).attr('row-id');
            var rowNode = gridDepositJobs.grid.gridOptions.api.getRowNode(rowId);
            handleDepositJobActive(key, rowNode);
        },
        items: contextMenuItemsDepositJobsActive,
    });
}

function applyUpdatedSettingFlowToDepositJobHtmls(){
    var flows=tableFlowSettings.dataset;

    var htmlSearchFlowList='', htmlNewJob='';
    for(var i=0;i<flows.length;i++){
        var flow=flows[i];
        htmlSearchFlowList+='<div class="form-check form-check-inline">';
        htmlSearchFlowList+='<input class="form-check-input" type="checkbox" id="check-stage-'+flow.name+'" flowId="'+flow.id+'" checked>';
        htmlSearchFlowList+='<label class="form-check-label" for="check-stage-'+flow.name+'">'+flow.name+'</label>';
        htmlSearchFlowList+='</div>';

        htmlNewJob+='<option value="${flow.getId()}">'+flow.name+'</option>';
    }
    $('#search-select-material-flow').html(htmlSearchFlowList);
    $('#new-job-select-material-flow').html(htmlNewJob);
}