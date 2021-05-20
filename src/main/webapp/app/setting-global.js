function saveGlobalSetting(){
	var req={};

	req['depositUserInstitute']=$('#global-setting input[name="depositUserInstitute"]').val();
	req['depositUserName']=$('#global-setting input[name="depositUserName"]').val();
	req['depositUserPassword']=$('#global-setting input[name="depositUserPassword"]').val();

	fetchHttp(PATH_SETTING_GLOBAL_SAVE, req, function(rsp){
        initFlowSettings();
        toastr.success("Successfully save the global options.");
    });
}

function saveUser2WhiteList(){
	var req={};
    // req['id']=$('#global-white-list input[name="id"]').val();
	req['userName']=$('#global-white-list input[name="whiteUserName"]').val();
	req['role']=$('#global-white-list select[name="whiteUserRole"] option:selected').attr('value');

	fetchHttp(PATH_SETTING_GLOBAL_WHITE_USER_SAVE, req, function(rsp){
        if (rsp) {
            tableWhiteList.update(rsp);
            toastr.success("Successfully save the user to white list: " +rsp.userName);
        }
    });
}

function deleteUserFromWhiteList(id){
    var req=tableWhiteList.getRowById(id);
	fetchHttp(PATH_SETTING_GLOBAL_WHITE_USER_DELETE, req, function(rsp){
        if (rsp) {
            tableWhiteList.delete(rsp);
            toastr.success("Successfully delete the user from white list: " + rsp.userName);
        }
    });
}

const defHeadersWhiteList=[
    {headerName: "ID", field: "id"},
    {headerName: "User Name", field: "userName"},
    {headerName: "Role", field: "role"},
    {headerName: "Action", field: "id", cellRenderer: function(row){
        return '<a href="#"  onclick="deleteUserFromWhiteList('+row.id+')">Delete</a>';
        }
    }
];

const tableWhiteList=new DashboardTable('grid-global-white-list', defHeadersWhiteList);
