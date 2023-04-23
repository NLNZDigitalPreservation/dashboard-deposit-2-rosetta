const STAGES=["INGEST","DEPOSIT","FINALIZE","FINISHED"];
const STATES=["INITIALED","RUNNING","PAUSED", "SUCCEED","FAILED","CANCELED"];
const PAUSE_AVAILABLE_STATUS={
    "RUNNING":["INGEST","FINALIZE"],
    "INITIALED":["DEPOSIT"]
};

const RESUME_AVAILABLE_STATUS={
    "PAUSED":["INGEST","DEPOSIT","FINALIZE"],
};

const RETRY_AVAILABLE_STATUS={
    "FAILED":["DEPOSIT"],
};

const TERMINATE_AVAILABLE_STATUS={
    "SUCCEED":["FINISHED"],
    "FAILED":["INGEST","DEPOSIT","FINALIZE","FINISHED"],
    "CANCELED":["INGEST","DEPOSIT","FINALIZE","FINISHED"],
};

const CANCEL_AVAILABLE_STATUS={
    "INITIALED":["INGEST","DEPOSIT","FINALIZE","FINISHED"],
    "PAUSED":["INGEST","DEPOSIT","FINALIZE","FINISHED"],
    "SUCCEED":["INGEST","DEPOSIT","FINALIZE","FINISHED"],
    "FAILED":["INGEST","DEPOSIT","FINALIZE","FINISHED"],
};

const AVAILABLE_STATUS_MAP={
    "PAUSE":PAUSE_AVAILABLE_STATUS,
    "RESUME":RESUME_AVAILABLE_STATUS,
    "RETRY":RETRY_AVAILABLE_STATUS,
    "TERMINATE":TERMINATE_AVAILABLE_STATUS,
    "CANCEL":CANCEL_AVAILABLE_STATUS,
};

function create_available_status_tips(action){
    action=action.toUpperCase();
    var available_status=AVAILABLE_STATUS_MAP[action];
    if(!available_status){
        return "";
    }
    var html="";
    for(state of STATES){
        var available_stages=[];
        if(available_status.hasOwnProperty(state)){
            available_stages=available_status[state];
        }
        var row="<tr>";
        row+="<th class='bg-info'>" + state + "</th>";
        for(stage of STAGES){
            var col="<td>-</td>";
            if(available_stages.includes(stage)){
                col="<td class='bg-success'>Y</td>";
            }
            row+=col;
        }
        row+="</tr>";

        html+=row;
    }
    return html;
};