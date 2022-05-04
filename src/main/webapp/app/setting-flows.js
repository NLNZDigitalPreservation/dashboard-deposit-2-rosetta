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
    
    // Basic settings
    reqBasic['id']=$('#flow-settings input[name="id"]').val();
    reqBasic['enabled']=$('#flow-settings input[name="enabled"]').is(':checked');
    reqBasic['name']=$('#flow-settings input[name="name"]').val();
    reqBasic['producerId']=$('#flow-settings select[name="producer"] option:selected').attr('value');
    reqBasic['producerName']=$('#flow-settings select[name="producer"] option:selected').text();
    reqBasic['materialFlowId']=$('#flow-settings select[name="materialFlow"] option:selected').attr('value');
    reqBasic['materialFlowName']=$('#flow-settings select[name="materialFlow"] option:selected').text();
    reqInjectionStorage['rootPath']=$('#flow-settings input[name="rootPath"]').val();

    // Advanced settings
    reqBasic['streamLocation']=$('#flow-settings input[name="streamLocation"]').val();
    reqBasic['injectionCompleteFileName']=$('#flow-settings input[name="injectionCompleteFileName"]').val();
    reqBasic['delays']=$('#flow-settings input[name="delays"]').val();
    reqBasic['delayUnit']='seconds';
    reqBasic['maxActiveDays']=$('#flow-settings input[name="maxActiveDays"]').val();
    reqBasic['maxSaveDays']=$('#flow-settings input[name="maxSaveDays"]').val();

    var weeklyMaxThreads=[];
    weeklyMaxThreads.push($('#flow-settings input[name="mon"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="tue"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="wed"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="thu"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="fri"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="sat"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="sun"]').val());
    reqBasic['weeklyMaxConcurrency']=weeklyMaxThreads;

    console.log(reqBasic);

    fetchHttp(PATH_SETTING_FLOW_SAVE, reqBasic, function(rsp){
        toastr.success("Successfully save the material flow: " + rsp.name);
        fetchHttp('/restful/setting/flow/all/get', null, initFlowSettingsList); 
    });
}

function deleteMaterialFlow(id){
    var name=$('#flow-setting input[name="name"]').val();
    fetchHttp(PATH_SETTING_FLOW_DELETE + '?id='+id, null, function(rsp){
        toastr.success("Successfully delete the material flow:" + name);
        fetchHttp('/restful/setting/flow/all/get', null, initFlowSettingsList); 
    });
}

function detailMaterialFlow(id, module){
    fetchHttp(PATH_SETTING_FLOW_DETAIL + '?id='+id, null, function(rsp){
        setValueMaterialFlow(rsp, module);
    });
}

function newMaterialFlow(){
    var flowSetting={
        id: null,
        name: '',
        enabled: true,
        producerId: '',
        producerName: '',
        materialFlowId: '',
        materialFlowName: '',
        rootPath: '',
        streamLocation: 'content/streams',
        injectionCompleteFileName: 'ready-for-ingestion-FOLDER-COMPLETED',
        delays: 60,
        delayUnit: 'S',
        maxActiveDays: 14,
        maxSaveDays: 365,
        weeklyMaxConcurrency: [1,1,1,1,1,1,1]
    }

    setValueMaterialFlow(flowSetting, '#flow-settings');
}

function setValueMaterialFlow(data, module){
    var reqBasic=data;
    $(module+' input[name="id"]').val(reqBasic['id']);
    $(module+' input[name="enabled"]').prop('checked', reqBasic['enabled']);
    $(module+' input[name="name"]').val(reqBasic['name']);
    $(module+' select[name="producer"]').val(reqBasic['producerId']);
    $(module+' select[name="materialFlow"]').val(reqBasic['materialFlowId']);
    $(module+' select[name="rootPath"]').val(reqBasic['rootPath']);

    $(module+' input[name="streamLocation"]').val(reqBasic['streamLocation']);
    $(module+' input[name="injectionCompleteFileName"]').val(reqBasic['injectionCompleteFileName']);
    $(module+' input[name="delays"]').val(reqBasic['delays']);
    $(module+' select[name="delayUnit"]').val(reqBasic['delayUnit']);
    $(module+' input[name="maxActiveDays"]').val(reqBasic['maxActiveDays']);
    $(module+' input[name="maxSaveDays"]').val(reqBasic['maxSaveDays']);

    var weeklyMaxThreads=reqBasic['weeklyMaxConcurrency'];
    $(module+' input[name="mon"]').val(weeklyMaxThreads[0]);
    $(module+' input[name="tue"]').val(weeklyMaxThreads[1]);
    $(module+' input[name="wed"]').val(weeklyMaxThreads[2]);
    $(module+' input[name="thu"]').val(weeklyMaxThreads[3]);
    $(module+' input[name="fri"]').val(weeklyMaxThreads[4]);
    $(module+' input[name="sat"]').val(weeklyMaxThreads[5]);
    $(module+' input[name="sun"]').val(weeklyMaxThreads[6]);    

    var healthAuditMsg;
    if (!reqBasic.id || reqBasic.auditRst) {
        healthAuditMsg='OK';
    }else{
        healthAuditMsg='<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + reqBasic.auditMsg;
    }
    $(module+' div[name="audit"]').html(healthAuditMsg);
}

// const modalFlowSettingPop = new bootstrap.Modal(document.getElementById('modal-flow-setting-pop'), {keyboard: false});
function initProducerMaterialflowSelect(data){
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
}

function initFlowSettingsList(items){
    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<li class="list-group-item d-flex justify-content-between align-items-start">';
        html+='<a class="producer-name" href="#" data-id="'+item.producerId+'">'+item.pruducerName+'</a>';
        html+='<span class="badge bg-danger rounded-pill"><a class="producer-action-delete" href="#" class="text-white" data-id="'+item.producerId+'>Delete</a></span>';
        html+='</li>';
    }
    $('#flow-settings-list').html(html);
}

