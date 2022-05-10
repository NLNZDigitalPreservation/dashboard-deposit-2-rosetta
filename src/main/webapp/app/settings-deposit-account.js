function DepositAccountSetting(){}
DepositAccountSetting.prototype = new BasicSettings('panel-settings-deposit-account','modal-deposit-account-settings', 'deposit-account-settings-list', PATH_SETTING_DEPOSIT_ACCOUNT_ALL_GET, PATH_SETTING_DEPOSIT_ACCOUNT_SAVE, PATH_SETTING_DEPOSIT_ACCOUNT_DETAIL, PATH_SETTING_DEPOSIT_ACCOUNT_DELETE, 'id', 'depositUserName');

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
	data['id']=$('#modal-deposit-account-settings input[name="id"]').val();
	data['depositUserInstitute']=$('#modal-deposit-account-settings input[name="depositUserInstitute"]').val();
	data['depositUserName']=$('#modal-deposit-account-settings input[name="depositUserName"]').val();
	data['depositUserPassword']=$('#modal-deposit-account-settings input[name="depositUserPassword"]').val();
	return data;
}

DepositAccountSetting.prototype.setValue=function(data){
    $('#modal-deposit-account-settings input[name="id"]').val(data['id']);
    $('#modal-deposit-account-settings input[name="depositUserInstitute"]').val(data['depositUserInstitute']);
    $('#modal-deposit-account-settings input[name="depositUserName"]').val(data['depositUserName']);
    $('#modal-deposit-account-settings input[name="depositUserPassword"]').val(data['depositUserPassword']);

    var healthAuditMsg;
    if (!data.id || data.auditRst) {
        healthAuditMsg='OK';
    }else{
        healthAuditMsg='<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + data.auditMsg;
    }
    $('#modal-deposit-account-settings div[name="audit"]').html(healthAuditMsg);
}
