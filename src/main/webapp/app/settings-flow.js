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
  $('#flow-settings select[name="producer"]').html(html);
  $('#flow-settings select[name="producer"]').val(null);
  $('#flow-settings select[name="materialFlow"]').html('');
  $('#flow-settings select[name="materialFlow"]').val(null);
  /**
  var selectedId=$('#flow-settings select[name="producer"] option:selected').val();
  if(selectedId!=null){
    $('#flow-settings select[name="materialFlow"]').html(mapMaterialFlows[selectedId]);
  }else{
    $('#flow-settings select[name="materialFlow"]').html('');
  }
  */


  $('#flow-settings select[name="producer"]').change(function() {
    var selectedId=$('#flow-settings select[name="producer"] option:selected').val();
    var flows=mapMaterialFlows[selectedId];

    console.log(flows);

    $('#flow-settings select[name="materialFlow"]').html(flows);
    $('#flow-settings select[name="materialFlow"]').val(null);
  });
}

//
function set_flow_dropdown_box(producerId){
    var flows=mapMaterialFlows[producerId];
    $('#flow-settings select[name="materialFlow"]').html(flows);
}

class FlowSetting extends BasicSettings{
    getPanelTitle(item){
        return item.id + ' | Producer: ' + item.producerName;
    }

    getPanelDescription(item){
        var desc='Material Flow: ' + item.materialFlowName;
        if(!item.enabled){
            desc += '&nbsp; <span class="badge bg-secondary rounded-pill">Disabled</span>';
        }
        return desc;
    }

    popupPanelPost(){
    	var that=this;
        fetchHttp(PATH_SETTING_DEPOSIT_ACCOUNT_ALL_GET, null, function(items){
            var html='';
            for(var i=0; i<items.length; i++){
                var item=items[i];
                html+='<option value="' + item.id + '">' + item.depositUserInstitute + ' | ' + item.depositUserName + '</option>';
            }
            $('#flow-settings select[name="depositAccount"]').html(html);
            $('#flow-settings select[name="depositAccount"]').val(null);

            // var selectedId=$('#flow-settings select[name="depositAccount"] option:selected').val();
            // fetchHttp(PATH_RAW_PRODUCER_MATERIAL_FLOW+'?depositAccountId='+selectedId, null, initProducerMaterialflowSelector);
        });

        $('#flow-settings select[name="depositAccount"]').change(function() {
            var selectedId=$(this).val();
            fetchHttp(PATH_RAW_PRODUCER_MATERIAL_FLOW+'?depositAccountId='+selectedId, null, initProducerMaterialflowSelector);
        });
    }

    getDefaultValue(){
    	var initialSetting={
        id: null,
        enabled: true,
        depositAccountId: 0,
        producerId: '',
        producerName: '',
        materialFlowId: '',
        materialFlowName: '',
        rootPath: '',
        streamLocation: 'content',
        injectionCompleteFileName: 'ready-for-ingestion-FOLDER-COMPLETED',
        maxActiveDays: 14,
        maxSaveDays: 365,
        weeklyMaxConcurrency: [1,1,1,1,1,1,1],
        actualContentDeleteOptions: 'notDelete',
        actualContentBackupOptions: 'notBackup',
        backupPath: '',
        backupSubFolders: ''
      }
    	return initialSetting;
    }

    getInputValue(){
        var data={};

        // Basic settings
        data['id']=$('#flow-settings input[name="id"]').val();
        data['enabled']=$('#flow-settings input[name="enabled"]').is(':checked');
        data['depositAccountId']=$('#flow-settings select[name="depositAccount"] option:selected').val();
        data['producerId']=$('#flow-settings select[name="producer"] option:selected').attr('value');
        data['producerName']=$('#flow-settings select[name="producer"] option:selected').text();
        data['materialFlowId']=$('#flow-settings select[name="materialFlow"] option:selected').attr('value');
        data['materialFlowName']=$('#flow-settings select[name="materialFlow"] option:selected').text();
        data['rootPath']=$('#flow-settings input[name="rootPath"]').val();

        // Advanced settings
        data['streamLocation']=$('#flow-settings input[name="streamLocation"]').val();
        data['injectionCompleteFileName']=$('#flow-settings input[name="injectionCompleteFileName"]').val();
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

        data['actualContentDeleteOptions']=$('#flow-settings select[name="actualContentDeleteOptions"] option:selected').attr('value');
        
        
        // Backup settings
        data['actualContentBackupOptions']=$('#flow-settings select[name="actualContentBackupOptions"] option:selected').attr('value');
        data['backupPath']=$('#flow-settings input[name="backupPath"]').val();
        data['backupSubFolders']=$('#flow-settings textarea[name="backupSubFolders"]').val();

        return data;
    }

    setValueOfDropdownBox(data){
        if(data['id'] === null){
            $('#flow-settings select[name="depositAccount"]').val(null);
            $('#flow-settings select[name="producer"]').val(null);
            $('#flow-settings select[name="materialFlow"]').val(null);
        }else{
            $('#flow-settings select[name="depositAccount"]').val(data['depositAccountId']);
            fetchHttp(PATH_RAW_PRODUCER_MATERIAL_FLOW+'?depositAccountId='+data['depositAccountId'], null, function(producerDataset){
                var htmlProducers=combineProducers(producerDataset);
                $('#flow-settings select[name="producer"]').html(htmlProducers);
                $('#flow-settings select[name="producer"]').val(data['producerId']);

                var htmlFlows=mapMaterialFlows[data['producerId']];
                $('#flow-settings select[name="materialFlow"]').html(htmlFlows);
                $('#flow-settings select[name="materialFlow"]').val(data['materialFlowId']);
            });
        }
    }

    setValue(data){
        $('#flow-settings input[name="id"]').val(data['id']);
        $('#flow-settings input[name="enabled"]').prop('checked', data['enabled']);
        this.setValueOfDropdownBox(data);
        $('#flow-settings input[name="rootPath"]').val(data['rootPath']);

        $('#flow-settings input[name="streamLocation"]').val(data['streamLocation']);
        $('#flow-settings input[name="injectionCompleteFileName"]').val(data['injectionCompleteFileName']);
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

        $('#flow-settings select[name="actualContentDeleteOptions"]').val(data['actualContentDeleteOptions']);

        //Backup options
        $('#flow-settings select[name="actualContentBackupOptions"]').val(data['actualContentBackupOptions']);
        $('#flow-settings input[name="backupPath"]').val(data['backupPath']);
        $('#flow-settings textarea[name="backupSubFolders"]').val(data['backupSubFolders']);

        var healthAuditMsg;
        if (!data.id || data.auditRst) {
            healthAuditMsg='OK';
        }else{
            healthAuditMsg='<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + data.auditMsg;
        }
        $('#flow-settings div[name="audit"]').html(healthAuditMsg);
    }
}

const settingFlow=new FlowSetting('panel-settings-flow','modal-flow-settings', 'flow-settings-list', PATH_SETTING_FLOW_ALL_GET, PATH_SETTING_FLOW_SAVE, PATH_SETTING_FLOW_DETAIL, PATH_SETTING_FLOW_DELETE);