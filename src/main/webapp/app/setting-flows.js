const modalFlow = new bootstrap.Modal(document.getElementById('modal-flow-settings'), {keyboard: false});

function showPanelFlow(){
    fetchHttp(PATH_SETTING_FLOW_ALL_GET, null, updateFlowList);
    fetchHttp(PATH_RAW_MATERIAL_FLOW, null, initProducerMaterialflowSelector);
    $('.side-panel').hide();
    $('#panel-settings-flow').show();
}

function updateFlowList(items){
    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<li class="list-group-item d-flex justify-content-between align-items-start">';
        html+='<a href="#" class="titles" data-id="'+item.id+'">';
        html+='<div class="ms-2 me-auto">';
        html+='<div class="fw-bold">'+item.flowId+'</div>';
        html+=item.flowName;
        html+='</div>';
        html+=' </a>';
        html+='<a href="#" class="actions text-white" data-id="'+item.id+'"><span class="badge bg-danger rounded-pill">Delete</span></a>';
        html+='</li>';
    }
    $('#flow-settings-list').html(html);

    $('#flow-settings-list .titles').click(function(){
        var id=$(this).attr('data-id');

        fetchHttp(PATH_SETTING_FLOW_DETAIL + '?id='+id, null, function(rsp){
              setValueProducer(rsp);
        });
    });


    $('#flow-settings-list .actions').click(function(){
        var id=$(this).attr('data-id');

       fetchHttp(PATH_SETTING_FLOW_DELETE + '?id='+id, null, function(rsp){
              var name=$('#modal-flow-settings input[name="name"]').val();
              toastr.success("Succeed to delete the material flow:" + name);
              fetchHttp(PATH_SETTING_FLOW_ALL_GET, null, updateFlowList);
        });
    });
}


function newMaterialFlow(){
    var initialSetting={
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

    setValueMaterialFlow(initialSetting, '');
}

function setValueMaterialFlow(data){
    var data=data;
    $('#flow-settings input[name="id"]').val(data['id']);
    $('#flow-settings input[name="enabled"]').prop('checked', data['enabled']);
    $('#flow-settings input[name="name"]').val(data['name']);
    $('#flow-settings select[name="producer"]').val(data['producerId']);
    $('#flow-settings select[name="materialFlow"]').val(data['materialFlowId']);
    $('#flow-settings select[name="rootPath"]').val(data['rootPath']);

    $('#flow-settings input[name="streamLocation"]').val(data['streamLocation']);
    $('#flow-settings input[name="injectionCompleteFileName"]').val(data['injectionCompleteFileName']);
    $('#flow-settings input[name="delays"]').val(data['delays']);
    $('#flow-settings select[name="delayUnit"]').val(data['delayUnit']);
    $('#flow-settings input[name="maxActiveDays"]').val(data['maxActiveDays']);
    $('#flow-settings input[name="maxSaveDays"]').val(data['maxSaveDays']);

    var weeklyMaxThreads=data['weeklyMaxConcurrency'];
    $('#flow-settings input[name="mon"]').val(weeklyMaxThreads[0]);
    $('#flow-settings input[name="tue"]').val(weeklyMaxThreads[1]);
    $('#flow-settings input[name="wed"]').val(weeklyMaxThreads[2]);
    $('#flow-settings input[name="thu"]').val(weeklyMaxThreads[3]);
    $('#flow-settings input[name="fri"]').val(weeklyMaxThreads[4]);
    $('#flow-settings input[name="sat"]').val(weeklyMaxThreads[5]);
    $('#flow-settings input[name="sun"]').val(weeklyMaxThreads[6]);    

    var healthAuditMsg;
    if (!data.id || data.auditRst) {
        healthAuditMsg='OK';
    }else{
        healthAuditMsg='<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + data.auditMsg;
    }
    $('#flow-settings div[name="audit"]').html(healthAuditMsg);
}


function saveMaterialFlow(){
    var data={};
    
    // Basic settings
    data['id']=$('#flow-settings input[name="id"]').val();
    data['enabled']=$('#flow-settings input[name="enabled"]').is(':checked');
    data['name']=$('#flow-settings input[name="name"]').val();
    data['producerId']=$('#flow-settings select[name="producer"] option:selected').attr('value');
    data['producerName']=$('#flow-settings select[name="producer"] option:selected').text();
    data['materialFlowId']=$('#flow-settings select[name="materialFlow"] option:selected').attr('value');
    data['materialFlowName']=$('#flow-settings select[name="materialFlow"] option:selected').text();
    reqInjectionStorage['rootPath']=$('#flow-settings input[name="rootPath"]').val();

    // Advanced settings
    data['streamLocation']=$('#flow-settings input[name="streamLocation"]').val();
    data['injectionCompleteFileName']=$('#flow-settings input[name="injectionCompleteFileName"]').val();
    data['delays']=$('#flow-settings input[name="delays"]').val();
    data['delayUnit']='seconds';
    data['maxActiveDays']=$('#flow-settings input[name="maxActiveDays"]').val();
    data['maxSaveDays']=$('#flow-settings input[name="maxSaveDays"]').val();

    var weeklyMaxThreads=[];
    weeklyMaxThreads.push($('#flow-settings input[name="mon"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="tue"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="wed"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="thu"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="fri"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="sat"]').val());
    weeklyMaxThreads.push($('#flow-settings input[name="sun"]').val());
    data['weeklyMaxConcurrency']=weeklyMaxThreads;

    console.log(data);

    fetchHttp(PATH_SETTING_FLOW_SAVE, data, function(rsp){
        toastr.success("Successfully save the material flow: " + data['name']);
        modalFlow.hide();
        fetchHttp(PATH_SETTING_FLOW_ALL_GET, null, updateFlowList);
    });
}


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

function initProducerMaterialflowSelector(data){
    var html=combineProducers(data);
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

