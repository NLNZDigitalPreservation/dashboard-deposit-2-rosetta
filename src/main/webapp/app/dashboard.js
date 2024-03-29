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

    if(response.hasOwnProperty('rspCode')){
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
    }else{
        callback(response);
    }
  }).catch((e) => {
    toastr.error(e);
  });
}

const gridOptions={
    // scrollbarWidth: 10,
    suppressRowClickSelection: true,
    // rowSelection: 'single',
    rowSelection: 'multiple',
    defaultColDef: {
        resizable: true,
        filter: true,
        sortable: true
    },
    // domLayout: 'autoHeight',
    rowData: [],
    getRowId: function(data){
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
                        toastr.warning("Please select one row!");
                        return;
                    }
                    that.funcRowSelected(rows[0]);
                }
            }
        });
        this.grid=new agGrid.createGrid(gridContainer, Object.assign(this.customizedGridOptions, gridOptions));
    }

    getSelectedRow(){
        var rows=this.grid.getSelectedRows();
        if(!rows || rows.length === 0 || rows.length > 1){
            return;
        }
        return rows[0];
    }

    getSelectedRows(){
        var rows=this.grid.getSelectedRows();
        if(!rows || rows.length === 0){
            return;
        }
        return rows;
    }

    getAllNodes(){
        var nodes=[];
        this.grid.forEachNode(function(node, index){
            nodes.push(node);
        });
        return nodes;
    }

    getAllRows(){
        var data=[];
        this.grid.forEachNode(function(node, index){
            data.push(node.data);
        });
        return data;
    }

    getNodeById(id){
        return this.grid.getRowNode(parseInt(id));
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
        return this.grid.getDisplayedRowAtIndex(index);
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
        this.grid.applyTransaction({add: dataset});
        this.redrawRows();
    }

    update(dataset){
        this.grid.applyTransaction({update: dataset});
        this.redrawRows();
    }

    removeAll(){
        this.grid.setGridOption('rowData', []);
        this.redrawRows();
    }

    removeByDataSet(dataset){
        this.grid.applyTransaction({remove: dataset});
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
        this.grid.deselectAll();
        this.redrawRows();
    }

    filter(keyWord){
        this.grid.setQuickFilter(keyWord);
    }

    setRowData(dataset){
        this.grid.setGridOption('rowData', dataset);
        this.redrawRows();
    }

    redrawRows(selectedData){
        this.grid.redrawRows(true);

        var selectedRow=this.getSelectedRow();
//        if (tableFlowSettings && selectedRow) {
//            var selectedNode=tableFlowSettings.getRowById(selectedRow.id);
//            this.selectRow(selectedNode);
//        }else{
//            this.selectRow();
//        }
        this.selectRow();
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

    exportDataCsv(params){
        this.grid.exportDataAsCsv(params);
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


class TreeGrid{
    constructor(container, options, contextMenuItems){
        var hierarchyTreeInstance=this;
        this.isTree=true;
        this.dataset=[];
        this.container=container;
        this.options=options;
        if(contextMenuItems!=null){
            $.contextMenu({
                selector: this.container + ' tr', 
                trigger: 'right',
                reposition: true,
                callback: function(key, options) {
                    var treeNodeKey=$(this).attr('key');
                    var treeNode=$.ui.fancytree.getTree(hierarchyTreeInstance.container).getNodeByKey(treeNodeKey);
                    var nodeData=hierarchyTreeInstance._getDataFromNode(treeNode);
                    contextMenuCallback(key, nodeData, hierarchyTreeInstance, gPopupModifyHarvest);
                },
                items: contextMenuItems
            });
        }
        // popupModifyViews[container]=this;
    }

    _getDataFromNode(treeNode){
        var nodeData=treeNode.data;
        nodeData.url=treeNode.title;
        nodeData.folder=treeNode.folder;
        return nodeData;
    }

    draw(dataset){
        dataset=formatLazyloadData(dataset);
        if($.ui.fancytree.getTree(this.container)){
            $.ui.fancytree.getTree(this.container).destroy();
        }

        this.options.source=dataset;
        $(this.container).fancytree(this.options);
    }

    drawWithDomain(dataset){
        if($.ui.fancytree.getTree(this.container)){
            $.ui.fancytree.getTree(this.container).destroy();
        }

        this.options.source=dataset;
        $(this.container).fancytree(this.options);
    }

    //Sort the domain names
    sortDomainByNames(dataset){
        for(var i=0; i<dataset.length; i++){
            for(var j=i+1; j<dataset.length; j++){
                if(dataset[i].title.localeCompare(dataset[j].title) > 0){
                    var c=dataset[i];
                    dataset[i]=dataset[j];
                    dataset[j]=c;
                }
            }
        }
    }

    setRowData(dataset){
        if($.ui.fancytree.getTree(this.container)){
            $.ui.fancytree.getTree(this.container).destroy();
        }
        this.formatDataForTreeGrid(dataset);
        this.options.source=dataset;
        $(this.container).fancytree(this.options);
    }

    formatDataForTreeGrid(dataset){
      if (!dataset) {return;}
      for(var i=0;i<dataset.length;i++){
        var e=dataset[i];
        e.title=e.url;
        delete e['lazy'];
        delete e['outlinks'];
        this.formatDataForTreeGrid(e.children);
      }
    }

    getSelectedNodes(){
        var selData=[];
        var selNodes = $.ui.fancytree.getTree(this.container).getSelectedNodes();
        for(var i=0; i<selNodes.length; i++){
            var treeNode=selNodes[i];
            var nodeData=this._getDataFromNode(treeNode);
            if ((this.container==='#hierachy-tree-url-names' && treeNode.folder!=null && treeNode.folder===false)
            || (this.container==='#hierachy-tree-harvest-struct' && (!treeNode.folder || treeNode.folder===false))) {
                selData.push(nodeData);
            }
            // selData.push(selNodes[i].data);
            // $.ui.fancytree.getTree(this.container).applyCommand('indent', selNodes[i]);
        }

        // console.log(selData);
        return selData;
    }

    getAllNodes(){
        var dataset=[];
        var rootNode= $.ui.fancytree.getTree(this.container).getRootNode();
        this._walkAllNodes(dataset, rootNode);
        return dataset;
    }

    _walkAllNodes(dataset, treeNode){
        var nodeData=this._getDataFromNode(treeNode);

        if ((this.container==='#hierachy-tree-url-names' && treeNode.folder!=null && treeNode.folder===false)
            || (this.container==='#hierachy-tree-harvest-struct' && treeNode.folder!==null)) {
            dataset.push(nodeData);
        }

        var childrenNodes=treeNode.children;
        if (!childrenNodes) {
            return;
        }
        for(var i=0; i<childrenNodes.length; i++){
            this._walkAllNodes(dataset, childrenNodes[i]);
        }
    }

    clearAll(){
        if($.ui.fancytree.getTree(this.container)){
            $.ui.fancytree.getTree(this.container).destroy();
        }
    }

    filter(match){
        var tree=$.ui.fancytree.getTree(this.container);
        var filterFunc = tree.filterNodes; //tree.filterBranches
        var option={
                autoApply: false,   // Re-apply last filter if lazy data is loaded
                autoExpand: true, // Expand all branches that contain matches while filtered
                counter: true,     // Show a badge with number of matching child nodes near parent icons
                fuzzy: false,      // Match single characters in order, e.g. 'fb' will match 'FooBar'
                hideExpandedCounter: true,  // Hide counter badge if parent is expanded
                hideExpanders: false,       // Hide expanders if all child nodes are hidden by filter
                highlight: true,   // Highlight matches by wrapping inside <mark> tags
                leavesOnly: false, // Match end nodes only
                nodata: true,      // Display a 'no data' status node if result is empty
                mode: "hide"       //dimm or hide Grayout unmatched nodes (pass "hide" to remove unmatched node instead)
        }
        var n = filterFunc.call(tree, match, option);
        console.log("filter out: "+n);
    }
}