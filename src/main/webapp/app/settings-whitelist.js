Whitelist=function(){}
Whitelist.prototype = new BasicSettings('panel-settings-whitelist','modal-whitelist-settings', 'whitelist-settings-list', PATH_SETTING_WHITELIST_ALL_GET, PATH_SETTING_WHITELIST_SAVE, PATH_SETTING_WHITELIST_DETAIL, PATH_SETTING_WHITELIST_DELETE, 'whitelistId', 'whitelistName');

Whitelist.prototype.getDefaultValue=function(){
	var initialSetting={
    id: null,
    whiteUserName: '',
    whiteUserRole: 'normal'
  }
	return initialSetting;
}

Whitelist.prototype.getInputValue=function(){
	var data={};
	data['id']=$('#modal-whitelist-settings input[name="id"]').val();
	data['whiteUserName']=$('#modal-whitelist-settings input[name="whiteUserName"]').val();
	data['whiteUserRole']=$('#modal-whitelist-settings select[name="whiteUserRole"] option:selected').attr('value');
	return data;
}

Whitelist.prototype.setValue=function(data){
	$('#modal-whitelist-settings input[name="id"]').val(data['id']);

  $('#modal-whitelist-settings select[name="whiteUserRole"]').val(data['whiteUserRole']);
  $('#modal-whitelist-settings input[name="whiteUserName"]').val(data['whiteUserName']);
  $('#modal-whitelist-settings input[name="depositUserName"]').val(data['depositUserName']);
  $('#modal-whitelist-settings input[name="depositUserPassword"]').val(data['depositUserPassword']);

  var healthAuditMsg;
  if (!data.id || data.auditRst) {
    healthAuditMsg='OK';
  }else{
    healthAuditMsg='<i class="bi bi-exclamation-triangle-fill text-danger">&nbsp;</i>' + data.auditMsg;
  }
  $('#modal-whitelist-settings div[name="audit"]').html(healthAuditMsg);
}

