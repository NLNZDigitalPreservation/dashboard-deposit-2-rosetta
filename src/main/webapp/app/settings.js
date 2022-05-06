// class BasicSettings{
// 	constructor(idPanel, idModal, urlGetList, urlSave, urlGetDetail, urlDelete)	{
// 		this.idPanel='#'+idPanel;
// 		this.idModal='#'+idModal;
// 		this.modal=new bootstrap.Modal(document.getElementById(idModal), {keyboard: false});
// 		this.urlGetList=urlGetList;
// 		this.urlSave=urlSave;
// 		this.urlGetDetail=urlGetDetail;
// 		this.urlDelete=urlDelete;
// 	}

// }


function BasicSettings(idPanel, idModal, urlGetList, urlSave, urlGetDetail, urlDelete){
	this.idPanel='#'+idPanel;
	this.idModal='#'+idModal;
	this.modal=new bootstrap.Modal(document.getElementById(idModal), {keyboard: false});
	this.urlGetList=urlGetList;
	this.urlSave=urlSave;
	this.urlGetDetail=urlGetDetail;
	this.urlDelete=urlDelete;
}

BasicSettings.prototype.popupPanel=function(){
	
}