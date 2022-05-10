function BasicSettings(idPanel, idModal, idContentList, urlGetList, urlSave, urlGetDetail, urlDelete, keyObjId, keyObjName){
	this.idPanel='#'+idPanel;
	this.idModal='#'+idModal;
	this.idContentList='#'+idContentList;
	this.modal=new bootstrap.Modal(document.getElementById(idModal), {keyboard: false});
	this.urlGetList=urlGetList;
	this.urlSave=urlSave;
	this.urlGetDetail=urlGetDetail;
	this.urlDelete=urlDelete;
	this.keyObjId=keyObjId;
	this.keyObjName=keyObjName;
}

BasicSettings.prototype.popupPanel=function(){
	fetchHttp(this.urlGetList, null, this.updatePanelList);
	$('.side-panel').hide();
	$(this.idPanel).show();
	this.popupPanelPost();
}

BasicSettings.prototype.updatePanelList=function(items){
    var that=this;

    if(items.length==0){
        $(that.idContentList).html('<h5>Please add the first item here.</h5>');
        return;
    }

    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<li class="list-group-item d-flex justify-content-between align-items-start">';
        html+='<a href="#" class="titles" data-id="'+item.id+'">';
        html+='<div class="ms-2 me-auto">';
        html+='<div class="fw-bold">'+item[that.keyObjId]+'</div>';
        html+=item[that.keyObjName];
        html+='</div>';
        html+=' </a>';
        html+='<a href="#" class="actions text-white" data-id="'+item.id+'"><span class="badge bg-danger rounded-pill">Delete</span></a>';
        html+='</li>';
    }
    $(that.idContentList).html(html);

    $(that.idContentList + ' .titles').click(function(){
        var id=$(this).attr('data-id');
        fetchHttp(that.urlGetDetail + '?id='+id, null, function(rsp){
          that.setValue(rsp);
          that.modal.show();
        });
    });

    $(that.idContentList + ' .actions').click(function(){
       var id=$(this).attr('data-id');
       fetchHttp(that.urlDelete + '?id='+id, null, function(rsp){
              toastr.success("Succeed to delete the row");
              fetchHttp(that.urlGetList, null, that.updatePanelList);
        });
    });
}

BasicSettings.prototype.popupPanelPost=function(){}

BasicSettings.prototype.closePanel=function(){
    $('.side-panel').hide();
}

BasicSettings.prototype.newSettingItem=function(){
	var data=this.getDefaultValue();
	this.setValue(data);
    this.modal.show();
}

BasicSettings.prototype.getDefaultValue=function(){}
BasicSettings.prototype.setValue=function(data){}

BasicSettings.prototype.saveSettingItem=function(){
	var data=this.getInputValue();
	var that=this;
	fetchHttp(this.urlSave, data, function(rsp){
      toastr.success("Successfully save the data");
      that.modal.hide();
      fetchHttp(that.urlGetList, null, that.updatePanelList);
  });
}

BasicSettings.prototype.combineSelector=function(items){
    var html='';
    for(var i=0; i<items.length; i++){
        var item=items[i];
        html+='<option value="' + item.id + '">' + item.id + '-' + item.name + '</option>';
    }
    return html;
}
