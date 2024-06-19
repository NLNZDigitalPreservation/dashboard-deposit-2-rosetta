import { ref } from 'vue';
import { defineStore } from 'pinia';
import { FilterMatchMode, FilterOperator } from 'primevue/api';

export const depositJobList=ref([]);
export const jobFilters=ref();

export const formatDatetimeFromEpochMilliSeconds=(epochMilliSecond:any)=>{
    if(!epochMilliSecond || epochMilliSecond === 0){
        return 'NA';
    }
    
    var date=new Date(epochMilliSecond);
    return date.toLocaleString();
}

