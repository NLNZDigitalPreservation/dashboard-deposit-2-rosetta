function ProducerSetting(){}
ProducerSetting.prototype = new BasicSettings('panel-settings-producer','modal-producer-settings', 'producer-settings-list', PATH_SETTING_PRODUCER_ALL_GET, PATH_SETTING_PRODUCER_SAVE, PATH_SETTING_PRODUCER_DETAIL, PATH_SETTING_PRODUCER_DELETE, 'producerId', 'producerName');
ProducerSetting.prototype.popupPanelPost=function(){
	var currentInstance=this;
	fetchHttp(PATH_RAW_PRODUCER, null, function(rsp){
		var html=currentInstance.combineSelector(rsp);
    $('#producer-setting select[name="producer"]').html(html);
	});
}

ProducerSetting.prototype.getDefaultValue=function(){
	var initialSetting={
    id: null,
    producerId: '',
    producerName: '',
    depositUserInstitute: '',
    depositUserName: '',
    depositUserPassword: ''
	}
	return initialSetting;
}

ProducerSetting.prototype.getInputValue=function(){
	var data={};
	data['id']=$('#modal-producer-settings input[name="id"]').val();
	data['producerId']=$('#modal-producer-settings select[name="producer"] option:selected').attr('value');
	data['producerName']=$('#modal-producer-settings select[name="producer"] option:selected').text();
	data['depositUserInstitute']=$('#modal-producer-settings input[name="depositUserInstitute"]').val();
	data['depositUserName']=$('#modal-producer-settings input[name="depositUserName"]').val();
	data['depositUserPassword']=$('#modal-producer-settings input[name="depositUserPassword"]').val();   
	return data;
}

ProducerSetting.prototype.setValue=function(data){
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
