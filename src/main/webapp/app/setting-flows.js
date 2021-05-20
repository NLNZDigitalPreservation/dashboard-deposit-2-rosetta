var mapMaterialFlows={};
function combineProducers(items){
    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<option value="' + item.id + '">' + item.id + '-' + item.name + '</option>';

        var flowHtml=combineMaterialFlows(item.materialFlows);
        mapMaterialFlows[item.id]=flowHtml;
    }
    return html;
}
function combineMaterialFlows(items){
    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<option value="' + item.id + '">' + item.id + '-' + item.name + '</option>';
    }
    return html;
}

function saveMaterialFlow(){
    var reqBasic={}, reqInjectionStorage={}, reqBackupStorage={};
    
    reqBasic['id']=$('#modal-flow-setting-pop #flow-setting input[name="id"]').val();
    reqBasic['enabled']=$('#modal-flow-setting-pop #flow-setting input[name="enabled"]').is(':checked');
    reqBasic['name']=$('#modal-flow-setting-pop #flow-setting input[name="name"]').val();
    reqBasic['producerId']=$('#modal-flow-setting-pop #flow-setting select[name="producer"] option:selected').attr('value');
    reqBasic['producerName']=$('#modal-flow-setting-pop #flow-setting select[name="producer"] option:selected').text();
    reqBasic['materialFlowId']=$('#modal-flow-setting-pop #flow-setting select[name="materialFlow"] option:selected').attr('value');
    reqBasic['materialFlowName']=$('#modal-flow-setting-pop #flow-setting select[name="materialFlow"] option:selected').text();
    reqBasic['streamLocation']=$('#modal-flow-setting-pop #flow-setting input[name="streamLocation"]').val();
    reqBasic['injectionCompleteFileName']=$('#modal-flow-setting-pop #flow-setting input[name="injectionCompleteFileName"]').val();
    reqBasic['delays']=$('#modal-flow-setting-pop #flow-setting input[name="delays"]').val();
    reqBasic['delayUnit']=$('#modal-flow-setting-pop #flow-setting select[name="delayUnit"] option:selected').attr('value');
    reqBasic['maxActiveDays']=$('#modal-flow-setting-pop #flow-setting input[name="maxActiveDays"]').val();
    reqBasic['maxSaveDays']=$('#modal-flow-setting-pop #flow-setting input[name="maxSaveDays"]').val();
    reqBasic['backupEnabled']=$('#modal-flow-setting-pop #backup-location input[name="enabled"]').is(':checked');

    var weeklyMaxThreads=[];
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="mon"]').val());
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="tue"]').val());
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="wed"]').val());
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="thu"]').val());
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="fri"]').val());
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="sat"]').val());
    weeklyMaxThreads.push($('#modal-flow-setting-pop #flow-setting input[name="sun"]').val());
    reqBasic['weeklyMaxConcurrency']=weeklyMaxThreads;

    reqInjectionStorage['id']=$('#modal-flow-setting-pop #injection-location input[name="id"]').val();
    reqInjectionStorage['scanMode']=$('#modal-flow-setting-pop #injection-location select[name="scanMode"] option:selected').attr('value');
    reqInjectionStorage['rootPath']=$('#modal-flow-setting-pop #injection-location input[name="rootPath"]').val();
    reqInjectionStorage['ftpServer']=$('#modal-flow-setting-pop #injection-location input[name="ftpServer"]').val();
    reqInjectionStorage['ftpPort']=$('#modal-flow-setting-pop #injection-location input[name="ftpPort"]').val();
    reqInjectionStorage['ftpUsername']=$('#modal-flow-setting-pop #injection-location input[name="ftpUsername"]').val();
    reqInjectionStorage['ftpPassword']=$('#modal-flow-setting-pop #injection-location input[name="ftpPassword"]').val();

    reqBackupStorage['id']=$('#modal-flow-setting-pop #backup-location input[name="id"]').val();
    reqBackupStorage['scanMode']=$('#modal-flow-setting-pop #backup-location select[name="scanMode"] option:selected').attr('value');
    reqBackupStorage['rootPath']=$('#modal-flow-setting-pop #backup-location input[name="rootPath"]').val();
    reqBackupStorage['ftpServer']=$('#modal-flow-setting-pop #backup-location input[name="ftpServer"]').val();
    reqBackupStorage['ftpPort']=$('#modal-flow-setting-pop #backup-location input[name="ftpPort"]').val();
    reqBackupStorage['ftpUsername']=$('#modal-flow-setting-pop #backup-location input[name="ftpUsername"]').val();
    reqBackupStorage['ftpPassword']=$('#modal-flow-setting-pop #backup-location input[name="ftpPassword"]').val();

    var req={
        flowSetting: reqBasic,
        injectionEndPoint: reqInjectionStorage,
        backupEndPoint: reqBackupStorage
    };

    console.log(req);

    fetchHttp(PATH_SETTING_FLOW_SAVE, req, function(rsp){
        tableFlowSettings.update(rsp.flowSetting);
        applyUpdatedSettingFlowToDepositJobHtmls();
        modalFlowSettingPop.hide();
        toastr.success("Successfully save the material flow: " + rsp.flowSetting.name);
    });
}

function deleteMaterialFlow(){
    var id=$('#modal-flow-setting-pop #flow-setting input[name="id"]').val();
    if (!id) {return;}
    var name=$('#flow-setting input[name="name"]').val();
    fetchHttp(PATH_SETTING_FLOW_DELETE + '?id='+id, null, function(rsp){
        tableFlowSettings.delete(id);
        applyUpdatedSettingFlowToDepositJobHtmls();
        modalFlowSettingPop.hide();
        toastr.success("Successfully delete the material flow:" + name);
    });
}

function editMaterialFlow(id){
    if (!id) {return;}
    detailMaterialFlow(id, '#modal-flow-setting-pop');
    $('#modal-flow-setting-pop button[name="delete"]').show();
    modalFlowSettingPop.show();
}

function detailMaterialFlow(id, module){
    fetchHttp(PATH_SETTING_FLOW_DETAIL + '?id='+id, null, function(rsp){
        setValueMaterialFlow(rsp, module);
    });
}

function newMaterialFlow(){
    var reqBasic={
        id: null,
        name: '',
        enabled: true,
        producerId: '',
        producerName: '',
        materialFlowId: '',
        materialFlowName: '',
        streamLocation: 'content/streams',
        injectionCompleteFileName: 'ready-for-ingestion-FOLDER-COMPLETED',
        delays: 60,
        delayUnit: 'S',
        maxActiveDays: 14,
        maxSaveDays: 365,
        backupEnabled: true,
        weeklyMaxConcurrency: [1,1,1,1,1,1,1]
    }

    var reqInjectionStorage={
        id: null,
        scanMode: 'NFS',
        ftpServer: '127.0.0.1',
        ftpPort: 21,
        ftpUsername: '',
        ftpPassword: '',
        ftpProxyEnabled: false,
        ftpProxyHost: '127.0.0.1',
        ftpProxyPort: 3128,
        ftpProxyUsername: '',
        ftpProxyPassword: ''
    }

    var reqBackupStorage=Object.assign({},reqInjectionStorage);
    var data={
       flowSetting: reqBasic,
       injectionEndPoint: reqInjectionStorage,
       backupEndPoint: reqBackupStorage
    }

    setValueMaterialFlow(data, '#modal-flow-setting-pop');

    $('#modal-flow-setting-pop button[name="delete"]').hide();
    modalFlowSettingPop.show();
}

function setValueMaterialFlow(data, module){
    var reqBasic=data.flowSetting, reqInjectionStorage=data.injectionEndPoint, reqBackupStorage=data.backupEndPoint;
    $(module+' #flow-setting input[name="id"]').val(reqBasic['id']);
    $(module+' #flow-setting input[name="enabled"]').prop('checked', reqBasic['enabled']);
    $(module+' #flow-setting input[name="name"]').val(reqBasic['name']);
    $(module+' #flow-setting select[name="producer"]').val(reqBasic['producerId']);
    $(module+' #flow-setting select[name="materialFlow"]').val(reqBasic['materialFlowId']);
    $(module+' #flow-setting input[name="streamLocation"]').val(reqBasic['streamLocation']);
    $(module+' #flow-setting input[name="injectionCompleteFileName"]').val(reqBasic['injectionCompleteFileName']);
    $(module+' #flow-setting input[name="delays"]').val(reqBasic['delays']);
    $(module+' #flow-setting select[name="delayUnit"]').val(reqBasic['delayUnit']);
    $(module+' #flow-setting input[name="maxActiveDays"]').val(reqBasic['maxActiveDays']);
    $(module+' #flow-setting input[name="maxSaveDays"]').val(reqBasic['maxSaveDays']);
    $(module+' #backup-location input[name="enabled"]').prop('checked', reqBasic['backupEnabled']);

    var weeklyMaxThreads=reqBasic['weeklyMaxConcurrency'];
    $(module+' #flow-setting input[name="mon"]').val(weeklyMaxThreads[0]);
    $(module+' #flow-setting input[name="tue"]').val(weeklyMaxThreads[1]);
    $(module+' #flow-setting input[name="wed"]').val(weeklyMaxThreads[2]);
    $(module+' #flow-setting input[name="thu"]').val(weeklyMaxThreads[3]);
    $(module+' #flow-setting input[name="fri"]').val(weeklyMaxThreads[4]);
    $(module+' #flow-setting input[name="sat"]').val(weeklyMaxThreads[5]);
    $(module+' #flow-setting input[name="sun"]').val(weeklyMaxThreads[6]);    

    $(module+' #injection-location input[name="id"]').val(reqInjectionStorage['id']);
    $(module+' #injection-location select[name="scanMode"]').val(reqInjectionStorage['scanMode']);
    $(module+' #injection-location input[name="rootPath"]').val(reqInjectionStorage['rootPath']);
    $(module+' #injection-location input[name="ftpServer"]').val(reqInjectionStorage['ftpServer']);
    $(module+' #injection-location input[name="ftpPort"]').val(reqInjectionStorage['ftpPort']);
    $(module+' #injection-location input[name="ftpUsername"]').val(reqInjectionStorage['ftpUsername']);
    $(module+' #injection-location input[name="ftpPassword"]').val(reqInjectionStorage['ftpPassword']);

    $(module+' #backup-location input[name="id"]').val(reqBackupStorage['id']);
    $(module+' #backup-location select[name="scanMode"]').val(reqBackupStorage['scanMode']);
    $(module+' #backup-location input[name="rootPath"]').val(reqBackupStorage['rootPath']);
    $(module+' #backup-location input[name="ftpServer"]').val(reqBackupStorage['ftpServer']);
    $(module+' #backup-location input[name="ftpPort"]').val(reqBackupStorage['ftpPort']);
    $(module+' #backup-location input[name="ftpUsername"]').val(reqBackupStorage['ftpUsername']);
    $(module+' #backup-location input[name="ftpPassword"]').val(reqBackupStorage['ftpPassword']);

    if (!reqBasic.id || reqBasic.auditRst) {
        $('#modal-flow-setting-pop div[name="audit"]').hide();
    }else{
        $('#modal-flow-setting-pop div[name="audit"]').show();
        $('#modal-flow-setting-pop div[name="audit"]').html('<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + reqBasic.auditMsg);
    }
}

const modalFlowSettingPop = new bootstrap.Modal(document.getElementById('modal-flow-setting-pop'), {keyboard: false});

const defHeadersFlowSettings=[
    {headerName: "ID", field: "id"},
    {headerName: "Name", field: "name", cellRenderer: function(row){
        return '<a href="#"  onclick="editMaterialFlow('+row.id+')">'+row.name+'</a>';
    }},
    {headerName: "Enabled", field: "enabled"},
    {headerName: "Health", field: "auditRst"},
];


function initFlowSettings(){
    fetchHttp(PATH_RAW_MATERIAL_FLOW, null, function(rsp){
        var html=combineProducers(rsp);
        $('#flow-setting select[name="producer"]').html(html);
        var selectedId=$('#flow-setting select[name="producer"] option:selected').val();
        $('#flow-setting select[name="materialFlow"]').html(mapMaterialFlows[selectedId]);

        $('#flow-setting select[name="producer"]').change(function() {
            var selectedId=$('#flow-setting select[name="producer"] option:selected').val();
            var flows=mapMaterialFlows[selectedId];

            console.log(flows);

            $('#flow-setting select[name="materialFlow"]').html(flows);
        });
    });

    var template=$('#template-flow-setting').html();
    $('#ul-flow-setting-read').html(template);
    $('#ul-flow-setting-edit').html(template);

    $('#ul-flow-setting-read input').prop('disabled', true);
    $('#ul-flow-setting-read select').prop('disabled', true);
}

const tableFlowSettings=new DashboardTable('table-flow-settings', defHeadersFlowSettings);