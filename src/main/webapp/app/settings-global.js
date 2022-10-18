class GlobalSetting{
    constructor(){
        this.idModal='#modal-global-settings';
        this.modal=new bootstrap.Modal(document.getElementById('modal-global-settings'), {keyboard: false});
        this.urlSave='restful/setting/global/save';
        this.urlGetDetail='restful/setting/global/get';
    }
    

    newSettingItem(){
        var data=this.getDefaultValue();
        this.setValue(data);
        this.modal.show();
    }


    showSettingDetail(){
        var that=this;
        fetchHttp(that.urlGetDetail, null, function(rsp){
          that.setValue(rsp);
          that.modal.show();
        });
    }

    saveSettingItem(){
        var data=this.getInputValue();
        var that=this;
        fetchHttp(this.urlSave, data, function(rsp){
          toastr.success("Successfully save the data");
          that.modal.hide();
      });
    }


    getDefaultValue(){
        var initialSetting={
            id: null,
            paused: false,
            pausedStartTime: new Date(),
            pausedEndTime: new Date(),
            delays: 60,
            delayUnit: 'S',
        }
        return initialSetting;
    }

    getInputValue(){
        var data={};
        data['id']=$('#modal-global-settings input[name="id"]').val();
        data['paused']=$('#modal-global-settings input[name="paused"]').is(':checked');
        data['pausedStartTime']=$('#modal-global-settings input[name="pausedStartTime"]').val();
        data['pausedEndTime']=$('#modal-global-settings input[name="pausedEndTime"]').val();
        data['delays']=$('#modal-global-settings input[name="delays"]').val();
        data['delayUnit']='S';
        return data;
    }

    setValue(data){
        $('#modal-global-settings input[name="id"]').val(data['id']);
        $('#modal-global-settings input[name="paused"]').prop('checked', data['paused']);
        $('#modal-global-settings input[name="pausedStartTime"]').val(data['pausedStartTime']);
        $('#modal-global-settings input[name="pausedEndTime"]').val(data['pausedEndTime']);
        $('#modal-global-settings input[name="delays"]').val(data['delays']);
        $('#modal-global-settings select[name="delayUnit"]').val(data['delayUnit']);
    }
}

const modalGlobalSetting = new GlobalSetting();


