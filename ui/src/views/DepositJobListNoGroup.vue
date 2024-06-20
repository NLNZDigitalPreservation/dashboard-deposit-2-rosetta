<script setup lang="ts">
import { ref, computed, onMounted, onBeforeMount } from 'vue';
import { FilterMatchMode, FilterOperator } from 'primevue/api';
import {MaterialFlow, DepositJob} from '@/types/deposit';
import {depositJobList, jobFilters, formatDatetimeFromEpochMilliSeconds} from '@/stores/depositjob';
import { type UseFetchApis, useFetch } from '@/utils/rest.api'

const rest: UseFetchApis = useFetch()
const selectedJobs=ref([]);
const expandedRowGroups=ref([]);

const formatMaterialFlowGroup=(data:any)=>{
    if(!data || !data.appliedFlowSetting || !data.appliedFlowSetting.materialFlowName){
        return "Unknown"
    }
    
    return data.appliedFlowSetting.materialFlowName;
}

const getProgressBarClass=(data:any)=>{
    if(data.stage==='FINISHED' && data.state==='CANCELED'){
        return "abnormal-progressbar";
    }else{
        return "";
    }
}

const calcProgressPercent=(data:any)=>{
    const stage=data.stage;
    const state=data.state;
    
    let percent=0;
    if (stage==='INGEST') {
        percent=0;
    }else if(stage==='DEPOSIT'){
        percent=33.33;
    }else if(stage==='FINALIZE'){
        percent=66.77;
    }else{
        percent=100;
    }

    var percentState=0.0;
    if (state==='INITIALED') {
        percentState=0.1;
    }else if(state==='RUNNING' || state==='PAUSED'){
        percentState=0.5;
    }else{
        percentState=1.0;
    }

    percent+=percentState*33.33;

    if (percent>99) {
        percent=100;
    }

    // return percent.toFixed(0);
    // return parseInt(percent.toFixed(2));
    return Math.round(percent);
}

const fetchActiveDepositJobs=()=>{
    rest.get('/restful/deposit-jobs/jobs/active/list').then((data: any) => {
        depositJobList.value=data;
    }).catch((err: any) => {
      console.log(err.message)
    });
}

onBeforeMount(() => {   
    
});

onMounted(() => {
    fetchActiveDepositJobs();
});
</script>
<template>
    <!-- v-model:filters="filters" -->
    <!-- :globalFilterFields="['id', 'injectionTitle', 'appliedFlowSetting.materialFlowName', 'stage', 'state', 'sipID']" -->
    <!-- filterDisplay="menu" -->

   <DataTable   :value="depositJobList"  v-model:selection="selectedJobs" 
                dataKey="id" size="small"
                sortMode="single" sortField="id" :sortOrder="1"
                paginator :rows="50" :rowsPerPageOptions="[15, 30, 50, 100, 200]"
                scrollable scrollHeight="100%"
                :filters="jobFilters"
                filterDisplay="menu"
                :globalFilterFields="['injectionTitle','appliedFlowSetting.materialFlowName']"
                showGridlines
                >
        <Column selectionMode="multiple" headerStyle="width: 3rem" frozen class="font-bold"></Column>
        <Column field="id" header="ID" frozen sortable></Column>
        <Column field="appliedFlowSetting.materialFlowName" header="MaterialFlow" frozen sortable></Column>
        <Column field="injectionTitle" header="JobTitle" style="min-width: 400px" frozen sortable>
            <template #filter="{ filterModel }">
                <InputText v-model="filterModel.value" type="text" class="p-column-filter" placeholder="Search by title" />
            </template>
        </Column>
        <Column field="stage" header="Stage" sortable></Column>
        <Column field="state" header="State" sortable></Column>
        <Column field="initialTime" header="JobInitialTime" style="min-width: 200px" sortable>
            <template #body="{ data }">
                <span>{{ formatDatetimeFromEpochMilliSeconds(data.initialTime) }}</span>
            </template>
        </Column>
        <Column field="latestTime" header="LatestUpdateTime" style="min-width: 200px" sortable>
            <template #body="{ data }">
                <span>{{ formatDatetimeFromEpochMilliSeconds(data.latestTime) }}</span>
            </template>
        </Column>

        <Column field="progress" header="Progress" sortable>
            <template #body="{ data }">
                <ProgressBar :value="calcProgressPercent(data)" v-bind:class="getProgressBarClass(data)"></ProgressBar>
            </template>
        </Column>

        <Column field="fileCount" header="NumOfFiles" sortable></Column>
        <Column field="fileSize" header="SizeOfFiles" sortable></Column>
        
        <Column field="depositStartTime" header="DepositStartTime" style="min-width: 200px" sortable>
            <template #body="{ data }">
                <span>{{ formatDatetimeFromEpochMilliSeconds(data.depositStartTime) }}</span>
            </template>
        </Column>
        <Column field="depositEndTime" header="DepositEndTime" style="min-width: 200px" sortable>
            <template #body="{ data }">
                <span>{{ formatDatetimeFromEpochMilliSeconds(data.depositEndTime) }}</span>
            </template>
        </Column>

        <Column field="sipID" header="SipId" sortable></Column>
        <Column field="sipModule" header="SipModule" sortable></Column>
        <Column field="sipStage" header="SipStage" sortable></Column>
        <Column field="sipStatus" header="SipStatus" sortable></Column>
    </DataTable>
</template>

<style>
.p-datatable-wrapper{
    height:calc(100vh - 135px);
}

/* tbody{
    font-size: 1rem;
} */

.abnormal-progressbar .p-progressbar-value{
    background: gray;
}
</style>