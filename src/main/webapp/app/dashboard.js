function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

var retryUrl, retryReq, retryCallback;
function fetchHttp(reqUrl, req, callback){
  $('#popup-window-loading').show();
  var reqBodyPayload="{}";
  if(req !== null){
    reqBodyPayload = JSON.stringify(req);
  }
  console.log('Fetch url:' + reqUrl);

  fetch(reqUrl, {
    method: 'POST',
    credentials: 'include',
    redirect: 'follow',
    headers: {'Content-Type': 'application/json'},
    body: reqBodyPayload
  }).then((response) => {
    $('#popup-window-loading').hide();
    if (response.status===200) {
        return response.json();
    }else{
        throw response.status + ": System Exception";
    }
  }).then((response) => {
    console.log(response);
    if(!response){
        return;
    }

    if(response.rspCode===0){
        var payload=JSON.parse(response.rspBody);
        callback(payload);
    }else if(response.rspCode===1001){
        retryUrl=reqUrl;
        retryReq=req;
        retryCallback=callback;
        popupSSOLoginWindow();
    }
    else{
        toastr.error(response.rspCode + ": " + response.rspMsg);
    }
    
  }).catch((e) => {
    toastr.error(e);
  });
}

const gridOptions={
    // scrollbarWidth: 10,
    suppressRowClickSelection: false,
    rowSelection: 'single',
    defaultColDef: {
        resizable: true,
        filter: true,
        sortable: true
    },
    // domLayout: 'autoHeight',
    rowData: [],
    getRowNodeId: function(data){
        return data.id;
    },
};

class CustomizedAgGrid{
    constructor(gridContainer, customizedGridOptions, funcRowSelected){
        this.funcRowSelected=funcRowSelected;
        var that=this;
        this.customizedGridOptions=Object.assign(customizedGridOptions, {onSelectionChanged: function(){
                if (that.funcRowSelected) {
                    var rows=this.api.getSelectedRows();
                    if(!rows || rows.length === 0 || rows.length > 1){
                        toastr.warning("Please select one row!")
                        return;
                    }
                    that.funcRowSelected(rows[0]);
                }
            }
        });
        this.grid=new agGrid.Grid(gridContainer, Object.assign(this.customizedGridOptions, gridOptions));
    }

    getSelectedRow(){
        var rows=this.grid.gridOptions.api.getSelectedRows();
        if(!rows || rows.length === 0 || rows.length > 1){
            return;
        }
        return rows[0];
    }

    getSelectedRows(){
        var rows=this.grid.gridOptions.api.getSelectedRows();
        if(!rows || rows.length === 0){
            return;
        }
        return rows;
    }

    getAllNodes(){
        var nodes=[];
        this.grid.gridOptions.api.forEachNode(function(node, index){
            nodes.push(node);
        });
        return nodes;
    }

    getAllRows(){
        var data=[];
        this.grid.gridOptions.api.forEachNode(function(node, index){
            data.push(node.data);
        });
        return data;
    }

    getNodeById(id){
        return this.grid.gridOptions.api.getRowNode(parseInt(id));
    }

    getRowById(id){
        var node=this.getNodeById(id);
        if (node) {
            return node.data;
        }else{
            return null;
        }
    }

    getNodeByIndex(index){
        return this.grid.gridOptions.api.getDisplayedRowAtIndex(index);
    }

    getRowByIndex(index){
        var node=this.getNodeByIndex(index);
        if (node) {
            return node.data;
        }else{
            return null;
        }
    }

    add(dataset){
        this.grid.gridOptions.api.applyTransaction({add: dataset});
        this.redrawRows();
    }

    update(dataset){
        this.grid.gridOptions.api.applyTransaction({update: dataset});
        this.redrawRows();
    }

    removeAll(){
        this.grid.gridOptions.api.setRowData([]);
        this.redrawRows();
    }

    removeByDataSet(dataset){
        this.grid.gridOptions.api.applyTransaction({remove: dataset});
        this.redrawRows();
    }

    removeById(id){
        var data=this.getRowById(id);
        if(data){
            var dataset=[];
            dataset.push(data);
            this.removeByDataSet(dataset);
        }
    }

    deselectAll(){
        this.grid.gridOptions.api.deselectAll();
        this.redrawRows();
    }

    filter(keyWord){
        this.grid.gridOptions.api.setQuickFilter(keyWord);
    }

    setRowData(dataset){
        this.grid.gridOptions.api.setRowData(dataset);
        this.redrawRows();
    }

    redrawRows(selectedData){
        this.grid.gridOptions.api.redrawRows(true);

        var selectedRow=this.getSelectedRow();
        if (selectedRow) {
            var selectedNode=gridMaterialFlow.getNodeById(selectedRow.id);
            this.selectRow(selectedNode);
        }else{
            this.selectRow();
        }
    }

    selectRow(selectedNode){
        if (!this.funcRowSelected) {
            return;
        }

        if (!selectedNode) {
            selectedNode=this.getNodeByIndex(0);
        }

        if (selectedNode) {
            selectedNode.selectThisNode(true);
            this.funcRowSelected(selectedNode.data);
        } else{
            this.funcRowSelected(null);
        }
    }
}

function formatDatetimeFromEpochMilliSeconds(epochMilliSecond){
    if(!epochMilliSecond || epochMilliSecond === 0){
        return 'NA';
    }
    
    var date=new Date(epochMilliSecond);
    return date.toLocaleString();
}

const K=1024, M=K*1024, G=M*1024;
function formatContentLength(l){
  if(l>G){
    return Math.round(l/G)+'GB';
  }else if(l>M){
    return Math.round(l/M)+'MB';
  }else if(l>K){
    return Math.round(l/K)+'KB';
  }else{
    return l+'B';
  }
}


class DashboardTable{
    constructor(tableId, headers){
        this.tableId='#'+tableId;
        this.headers=headers;
        this.dataset=[];
    }

    getRowById(id){
        for (var i = this.dataset.length - 1; i >= 0; i--) {
            var curRow=this.dataset[i];
            if(id===curRow.id){
                return curRow;
            }
        }
        return null;
    }
    
    add(row){
        for (var i = this.dataset.length - 1; i >= 0; i--) {
            var curRow=this.dataset[i];
            if(row.id===curRow.id){
                index=i;
                break;
            }
        }

        if (index > -1) {
            toastr.error('Failed to add duplicate row: '+row.id);
            console.log(row);
            return;
        }
        this.dataset.push(row);
        this.setRowData(this.dataset);
    }

    update(row){
        var index=-1;
        for (var i = this.dataset.length - 1; i >= 0; i--) {
            var curRow=this.dataset[i];
            if(row.id===curRow.id){
                index=i;
                break;
            }
        }
        if (index > -1) {
            this.dataset[index]=row;
        }else{
            this.dataset.push(row);
        }
        this.setRowData(this.dataset);
    }

    delete(target){
        var targetId=target.id;
        if (!targetId) {
            targetId=parseInt(target);
        }
        var index=-1;
        for (var i = this.dataset.length - 1; i >= 0; i--) {
            var curRow=this.dataset[i];
            if(targetId===curRow.id){
                index=i;
                break;
            }
        }
        if (index > -1) {
            this.dataset.splice(index, 1);
        }

        this.setRowData(this.dataset);
    }

    setRowData(dataset){
        this.dataset=dataset;
        var html='<table class="table table-fixed table-bordered bg-light">';
        html+='<thead>';
        html+='<tr>';

        for(var i=0; i<this.headers.length; i++){
            var th=this.headers[i];
            html+='<th scope="col">'+th.headerName+'</th>';
        }
        html+='</tr>';
        html+='</thead>';

        html+='<tbody>';
        for(var k=0; k<dataset.length; k++){
            var row=dataset[k];
            html+='<tr>';

            for(var i=0; i<this.headers.length; i++){
                var th=this.headers[i];
                var renderedCellData;
                if(th.cellRenderer){
                    renderedCellData=th.cellRenderer(row);
                }else{
                    renderedCellData=row[th.field];
                }
                html+='<td>'+renderedCellData+'</td>';
            }
            html+='</tr>';
        }
        html+='</tbody>';
        html+='</table>';

        $(this.tableId).html(html);

        html='';
    }

}
