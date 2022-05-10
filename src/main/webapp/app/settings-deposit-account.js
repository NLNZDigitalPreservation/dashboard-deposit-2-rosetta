function DepositAccountSetting(){}
DepositAccountSetting.prototype = new BasicSettings('panel-settings-producer','modal-producer-settings', 'producer-settings-list', PATH_SETTING_PRODUCER_ALL_GET, PATH_SETTING_PRODUCER_SAVE, PATH_SETTING_PRODUCER_DETAIL, PATH_SETTING_PRODUCER_DELETE, 'producerId', 'producerName');

DepositAccountSetting.prototype.getDefaultValue=function(){
	var initialSetting={
    id: null,
    depositUserInstitute: '',
    depositUserName: '',
    depositUserPassword: ''
	}
	return initialSetting;
}

DepositAccountSetting.prototype.getInputValue=function(){
	var data={};
	data['id']=$('#modal-producer-settings input[name="id"]').val();
	data['depositUserInstitute']=$('#modal-producer-settings input[name="depositUserInstitute"]').val();
	data['depositUserName']=$('#modal-producer-settings input[name="depositUserName"]').val();
	data['depositUserPassword']=$('#modal-producer-settings input[name="depositUserPassword"]').val();
	return data;
}

DepositAccountSetting.prototype.setValue=function(data){
	$('#modal-producer-settings input[name="id"]').val(data['id']);
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
