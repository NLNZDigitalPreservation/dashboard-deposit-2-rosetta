//Producer selector
const gridProducers=agGrid.createGrid(document.querySelector('#dropdown-grid-producers'),  {
      columnDefs: [
          {headerName:"ProducerID", field:"id", width:250},
          {headerName:"Name", field:"name", width:1000},
          {headerName:"Active", field:"active", width:100, pinned: 'right' },
      ],
      rowSelection: 'single',
      onSelectionChanged: onSelectionChangedProducer,
      rowData: []
});

function onSelectionChangedProducer(){
    const selectedRows = gridProducers.getSelectedRows();
    console.log(selectedRows);
    if(!selectedRows || selectedRows.length < 1){
        return;
    }
    var selNode=selectedRows[0];
    var depositAccountId=$('#flow-settings select[name="depositAccount"]').val();
    fetchHttp(PATH_RAW_MATERIAL_FLOWS+'?depositAccountId=' + depositAccountId+'&producerId='+selNode['id'], null, function(dataset){
        gridMaterialFlows.setGridOption('rowData', dataset);
        gridMaterialFlows.redrawRows(true);
    });

    $('#input-producer').val(selNode['id'] + ' | ' + selNode['name']);
    $('#input-materialflow').val('');
}

$('#filter-producer').on('input', function(){
    var val=$(this).val();
    gridProducers.setQuickFilter(val);
});

//Material flow selector
const gridMaterialFlows=agGrid.createGrid(document.querySelector('#dropdown-grid-materialflows'),  {
      columnDefs: [
          {headerName:"MaterialFlowID", field:"id", width:250},
          {headerName:"Name", field:"name", width:1000},
      ],
      rowSelection: 'single',
      onSelectionChanged: onSelectionChangedMaterialFlow,
      rowData: []
});

function onSelectionChangedMaterialFlow(){
    const selectedRows = gridMaterialFlows.getSelectedRows();
    console.log(selectedRows);
    if(!selectedRows || selectedRows.length < 1){
        return;
    }
    var selNode=selectedRows[0];
    $('#input-materialflow').val(selNode['id'] + ' | ' + selNode['name']);
}

$('#filter-materialflow').on('input', function(){
    var val=$(this).val();
    gridMaterialFlows.setQuickFilter(val);
});

//Refresh the deposit account: getting the related producers
function refreshTheDepositAccount(){
    var depositAccountId=$('#flow-settings select[name="depositAccount"] option:selected').val();
    if(!depositAccountId){
        alert('Please select a Deposit Account');
        return;
    }

    $('.spinner-container').css('visibility','visible');
    fetchHttp(PATH_SETTING_DEPOSIT_ACCOUNT_REFRESH+'?id='+depositAccountId, null, function(depositAccount){
         fetchHttp(PATH_RAW_PRODUCERS+'?depositAccountId=' + depositAccountId, null, function(dataset){
            gridProducers.setGridOption('rowData', dataset);
            gridProducers.redrawRows(true);
        });
        $('.spinner-container').css('visibility','hidden');
    });
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
            $('#input-producer').val('');
            $('#input-materialflow').val('');
            var depositAccountId=$(this).val();
            fetchHttp(PATH_RAW_PRODUCERS+'?depositAccountId=' + depositAccountId, null, function(dataset){
                gridProducers.setGridOption('rowData', dataset);
                gridProducers.redrawRows(true);
            });
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
        var producerItems=$('#flow-settings input[name="producer"]').val().split('|');
        if(producerItems.length==2){
            data['producerId']=producerItems[0].trim();
            data['producerName']=$('#flow-settings input[name="producer"]').val();
        }
        var materialFlowItems=$('#flow-settings input[name="materialFlow"]').val().split('|');
        if(materialFlowItems.length==2){
            data['materialFlowId']=materialFlowItems[0].trim();
            data['materialFlowName']=$('#flow-settings input[name="materialFlow"]').val();
        }

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
            $('#flow-settings select[name="depositAccount"]').val('');
            $('#flow-settings input[name="producer"]').val('');
            $('#flow-settings input[name="materialFlow"]').val('');
        }else{
            $('#flow-settings select[name="depositAccount"]').val(data['depositAccountId']);
            var depositAccountId=data['depositAccountId'];
            fetchHttp(PATH_RAW_PRODUCERS+'?depositAccountId=' + depositAccountId, null, function(dataset){
                gridProducers.setGridOption('rowData', dataset);
                gridProducers.redrawRows(true);
            });
            $('#flow-settings input[name="producer"]').val(data['producerId'] + ' | ' + data['producerName']);
            $('#flow-settings input[name="materialFlow"]').val(data['materialFlowId'] + ' | ' + data['materialFlowName']);
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