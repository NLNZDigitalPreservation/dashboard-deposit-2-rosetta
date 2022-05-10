class BasicSettings{
    constructor(idPanel, idModal, idContentList, urlGetList, urlSave, urlGetDetail, urlDelete){
        this.idPanel='#'+idPanel;
        this.idModal='#'+idModal;
        this.idContentList='#'+idContentList;
        this.modal=new bootstrap.Modal(document.getElementById(idModal), {keyboard: false});
        this.urlGetList=urlGetList;
        this.urlSave=urlSave;
        this.urlGetDetail=urlGetDetail;
        this.urlDelete=urlDelete;
    }
    popupPanel(){
        this.updatePanelList();
        $('.side-panel').hide();
        $(this.idPanel).show();
        this.popupPanelPost();
    }

    updatePanelList(){
        var that=this;
        fetchHttp(this.urlGetList, null,function(items){
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
                html+='<div class="fw-bold">'+ that.getPanelTitle(item) +'</div>';
                html+=that.getPanelDescription(item);
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
                  that.updatePanelList();
                });
            });
        });
    }

    closePanel(){
        $('.side-panel').hide();
    }

    newSettingItem(){
    	var data=this.getDefaultValue();
    	this.setValue(data);
        this.modal.show();
    }

    saveSettingItem(){
    	var data=this.getInputValue();
    	var that=this;
    	fetchHttp(this.urlSave, data, function(rsp){
          toastr.success("Successfully save the data");
          that.modal.hide();
          that.updatePanelList();
      });
    }

//    combineSelector(items){
//        var html='';
//        for(var i=0; i<items.length; i++){
//            var item=items[i];
//            html+='<option value="' + item.id + '">' + item.id + '-' + item.name + '</option>';
//        }
//        return html;
//    }

    getPanelTitle(item){}
    getPanelDescription(item){}
    popupPanelPost(){}
    getDefaultValue(){}
    setValue(data){}
}


