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

//    reqInjectionStorage['id']=$('#modal-flow-setting-pop #injection-location input[name="id"]').val();
    reqInjectionStorage['scanMode']=$('#modal-flow-setting-pop #injection-location select[name="scanMode"] option:selected').attr('value');
    reqInjectionStorage['rootPath']=$('#modal-flow-setting-pop #injection-location input[name="rootPath"]').val();
    reqInjectionStorage['ftpServer']=$('#modal-flow-setting-pop #injection-location input[name="ftpServer"]').val();
    reqInjectionStorage['ftpPort']=$('#modal-flow-setting-pop #injection-location input[name="ftpPort"]').val();
    reqInjectionStorage['ftpUsername']=$('#modal-flow-setting-pop #injection-location input[name="ftpUsername"]').val();
    reqInjectionStorage['ftpPassword']=$('#modal-flow-setting-pop #injection-location input[name="ftpPassword"]').val();

//    reqBackupStorage['id']=$('#modal-flow-setting-pop #backup-location input[name="id"]').val();
    reqBackupStorage['scanMode']=$('#modal-flow-setting-pop #backup-location select[name="scanMode"] option:selected').attr('value');
    reqBackupStorage['rootPath']=$('#modal-flow-setting-pop #backup-location input[name="rootPath"]').val();
    reqBackupStorage['ftpServer']=$('#modal-flow-setting-pop #backup-location input[name="ftpServer"]').val();
    reqBackupStorage['ftpPort']=$('#modal-flow-setting-pop #backup-location input[name="ftpPort"]').val();
    reqBackupStorage['ftpUsername']=$('#modal-flow-setting-pop #backup-location input[name="ftpUsername"]').val();
    reqBackupStorage['ftpPassword']=$('#modal-flow-setting-pop #backup-location input[name="ftpPassword"]').val();

    reqBasic['injectionEndPoint']=reqInjectionStorage;
    reqBasic['backupEndPoint']=reqBackupStorage;

    console.log(reqBasic);

    fetchHttp(PATH_SETTING_FLOW_SAVE, reqBasic, function(rsp){
        tableFlowSettings.update(rsp);
        applyUpdatedSettingFlowToDepositJobHtmls();
        modalFlowSettingPop.hide();
        toastr.success("Successfully save the material flow: " + rsp.name);
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
    var flowSetting={
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

    flowSetting['injectionEndPoint']=reqInjectionStorage;
    flowSetting['backupEndPoint']=reqBackupStorage;

    setValueMaterialFlow(flowSetting, '#modal-flow-setting-pop');

    $('#modal-flow-setting-pop button[name="delete"]').hide();
    modalFlowSettingPop.show();
}

function setValueMaterialFlow(data, module){
    var reqBasic=data, reqInjectionStorage=data.injectionEndPoint, reqBackupStorage=data.backupEndPoint;
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

//    $(module+' #injection-location input[name="id"]').val(reqInjectionStorage['id']);
    $(module+' #injection-location select[name="scanMode"]').val(reqInjectionStorage['scanMode']);
    $(module+' #injection-location input[name="rootPath"]').val(reqInjectionStorage['rootPath']);
    $(module+' #injection-location input[name="ftpServer"]').val(reqInjectionStorage['ftpServer']);
    $(module+' #injection-location input[name="ftpPort"]').val(reqInjectionStorage['ftpPort']);
    $(module+' #injection-location input[name="ftpUsername"]').val(reqInjectionStorage['ftpUsername']);
    $(module+' #injection-location input[name="ftpPassword"]').val(reqInjectionStorage['ftpPassword']);

//    $(module+' #backup-location input[name="id"]').val(reqBackupStorage['id']);
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

// const modalFlowSettingPop = new bootstrap.Modal(document.getElementById('modal-flow-setting-pop'), {keyboard: false});

const defHeadersFlowSettings=[
    {headerName: "ID", field: "id"},
    {headerName: "Name", field: "name", cellRenderer: function(row){
        return '<a href="#"  onclick="editMaterialFlow('+row.id+')">'+row.name+'</a>';
    }},
    {headerName: "Enabled", field: "enabled"},
    {headerName: "Health", field: "auditRst"},
];


const optionFlowSetting={
    extensions: ["table", "wide", "filter"],
    quicksearch: true,
    checkbox: true,
    table: {checkboxColumnIdx: 0, nodeColumnIdx: 1},
    source: [],
    selectMode: 3,
    renderColumns: function(event, treeNode) {
        var nodeData=treeNode.node.data;

        var $tdList = $(treeNode.node.tr).find(">td");

        if (nodeData.contentType && nodeData.contentType!=='unknown') {
            $tdList.eq(2).text(nodeData.contentType);
        }

        if (nodeData.statusCode > 0) {
            $tdList.eq(3).text(nodeData.statusCode);
        }
        
        if (nodeData.contentLength > 0){
            $tdList.eq(4).text(formatContentLength(nodeData.contentLength));
        }

        $tdList.eq(5).text(nodeData.totUrls);
        $tdList.eq(6).text(nodeData.totSuccess);
        $tdList.eq(7).text(nodeData.totFailed);
        $tdList.eq(8).text(formatContentLength(nodeData.totSize));

        $(treeNode.node.tr).attr("key", ""+treeNode.node.key);

        // $(treeNode.node.tr).attr("data", JSON.stringify(nodeData));
        if (nodeData.id > 0) {
            $(treeNode.node.tr).attr("idx", ""+nodeData.id);

            var toBeModifiedNode=gPopupModifyHarvest.gridToBeModified.getNodeByDataId(nodeData.id);
            if (toBeModifiedNode) {
                var classOfTreeRow=gPopupModifyHarvest.getTreeNodeStyle(toBeModifiedNode.option);
                $(treeNode.node.tr.children).addClass(classOfTreeRow);
            }
        }

        // if (nodeData.viewType && nodeData.viewType===2 && nodeData.id===-1){
        //  $(treeNode.node.tr).attr("menu", "folder");
        // }else{
        //  $(treeNode.node.tr).attr("menu", "url");
        // }
    },
    icon: function(event, data){
        return "bi bi-box-seam";
    },
};