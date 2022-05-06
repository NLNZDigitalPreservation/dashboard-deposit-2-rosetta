const modalWhitelist = new bootstrap.Modal(document.getElementById('modal-whitelist-settings'), {keyboard: false});

function showPanelWhitelist(){
      fetchHttp(PATH_SETTING_WHITELIST_ALL_GET, null, updateWhitelist);
      $('.side-panel').hide();
      $('#panel-settings-producer').show();
}

function updateWhitelistList(items){
      var html='';
      for(var i=0; i<items.length; i++){
            var item=items[i];
            html+='<li class="list-group-item d-flex justify-content-between align-items-start">';
            html+='<a href="#" class="titles" data-id="'+item.id+'">';
            html+='<div class="ms-2 me-auto">';
            html+='<div class="fw-bold">'+item.userId+'</div>';
            html+=item.userName;
            html+='</div>';
            html+=' </a>';
            html+='<a href="#" class="actions text-white" data-id="'+item.id+'"><span class="badge bg-danger rounded-pill">Delete</span></a>';
            html+='</li>';
      }
      $('#whitelist-settings-list').html(html);

      $('#whitelist-settings-list .titles').click(function(){
            var id=$(this).attr('data-id');

            fetchHttp(PATH_SETTING_WHITELIST_DETAIL + '?id='+id, null, function(rsp){
                  setValueProducer(rsp);
            });
      });


      $('#whitelist-settings-list .actions').click(function(){
            var id=$(this).attr('data-id');

           fetchHttp(PATH_SETTING_PRODUCER_DELETE + '?id='+id, null, function(rsp){
                  var name=$('#modal-whitelist-settings input[name="username"]').val();
                  toastr.success("Succeed to delete the user:" + name);
                  fetchHttp(PATH_SETTING_WHITELIST_ALL_GET, null, updateWhitelistList);
            });
      });
}


function newWhitelist(){
    var initialSetting={
        id: null,
        userName: '',
        userRole: ''
    }

    setValueProducer(initialSetting);

    modalWhitelist.show();
}

function setValueProducer(data){
      $('#modal-producer-settings input[name="id"]').val(data['id']);
      $('#modal-producer-settings select[name="producer"]').val(data['producerId']);
      $('#modal-producer-settings input[name="depositUserInstitute"]').val(data['depositUserInstitute']);
      $('#modal-producer-settings input[name="depositUserName"]').val(data['depositUserName']);
      $('#modal-producer-settings input[name="depositUserPassword"]').val(data['depositUserPassword']);

      var healthAuditMsg;
      if (!data.id || data.auditRst) {
            healthAuditMsg='OK';
      }else{
            healthAuditMsg='<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + data.auditMsg;
      }
      $('#modal-producer-settings div[name="audit"]').html(healthAuditMsg);
}

function saveProducer(){
      var data={};

      data['id']=$('#modal-producer-settings input[name="id"]').val();
      data['producerId']=$('#modal-producer-settings select[name="producer"] option:selected').attr('value');
      data['producerName']=$('#modal-producer-settings select[name="producer"] option:selected').text();
      data['depositUserInstitute']=$('#modal-producer-settings input[name="depositUserInstitute"]').val();
      data['depositUserName']=$('#modal-producer-settings input[name="depositUserName"]').val();
      data['depositUserPassword']=$('#modal-producer-settings input[name="depositUserPassword"]').val();    

      fetchHttp(PATH_SETTING_PRODUCER_SAVE, data, function(rsp){
            var name=$('#modal-producer-settings select[name="producer"] option:selected').text();
            toastr.success("Succeed to save the producer: " + name);
            modalProducer.hide();
            fetchHttp(PATH_SETTING_PRODUCER_ALL_GET, null, updateProducerList);
      });
}

function initProducerSelector(items){
    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<option value="' + item.id + '">' + item.id + '-' + item.name + '</option>';

        var flowHtml=combineMaterialFlows(item.materialFlows);
        mapMaterialFlows[item.id]=flowHtml;
    }

    $('#producer-setting select[name="producer"]').html(html);
}
