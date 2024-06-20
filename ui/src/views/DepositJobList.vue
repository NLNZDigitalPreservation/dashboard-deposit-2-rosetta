<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue";
import { FilterMatchMode, FilterOperator } from "primevue/api";
import { MaterialFlow, DepositJob } from "@/types/deposit";
import { useJobListDTO, keywords } from "@/stores/depositjob";

const jobList = useJobListDTO();
const selectedJobs = ref([]);
const expandedRowGroups = ref([]);

watch(keywords, async (newValue, oldValue) => {
  jobList.filter(keywords.value);
});

onMounted(() => {
    jobList.fetchAllData();
});
</script>
<template>
  <DataTable
    v-model:expandedRowGroups="expandedRowGroups"
    :value="jobList.listJobsFiltered"
    v-model:selection="selectedJobs"
    dataKey="id"
    size="small"
    rowGroupMode="subheader"
    groupRowsBy="materialFlowName"
    sortMode="single"
    sortField="materialFlowName"
    :sortOrder="1"
    paginator
    :rows="50"
    :rowsPerPageOptions="[15, 30, 50, 100, 200]"
    scrollable
    scrollHeight="100%"
    showGridlines
  >
    <Column
      field="materialFlowName"
      header="MaterialFlow"
      frozen
      class="font-bold"
    ></Column>
    <Column
      selectionMode="multiple"
      headerStyle="width: 3rem"
      frozen
      class="font-bold"
    ></Column>
    <Column field="id" header="ID" frozen sortable></Column>
    <Column
      field="injectionTitle"
      header="JobTitle"
      style="min-width: 400px"
      frozen
      sortable
    ></Column>
    <Column field="stage" header="Stage" sortable></Column>
    <Column field="state" header="State" sortable></Column>
    <Column
      field="initialTime"
      header="JobInitialTime"
      style="min-width: 200px"
      sortable
    ></Column>
    <Column
      field="latestTime"
      header="LatestUpdateTime"
      style="min-width: 200px"
      sortable
    ></Column>
    <Column field="progress" header="Progress" sortable>
      <template #body="{ data }">
        <ProgressBar
          :value="data.progress"
          v-bind:class="data.progressClassName"
        ></ProgressBar>
      </template>
    </Column>
    <Column field="fileCount" header="NumOfFiles" sortable></Column>
    <Column field="fileSize" header="SizeOfFiles" sortable></Column>
    <Column
      field="depositStartTime"
      header="DepositStartTime"
      style="min-width: 200px"
      sortable
    ></Column>
    <Column
      field="depositEndTime"
      header="DepositEndTime"
      style="min-width: 200px"
      sortable
    ></Column>
    <Column field="sipID" header="SipId" sortable></Column>
    <Column field="sipModule" header="SipModule" sortable></Column>
    <Column field="sipStage" header="SipStage" sortable></Column>
    <Column field="sipStatus" header="SipStatus" sortable></Column>

    <template #groupheader="slotProps">
      <div class="flex align-items-center gap-2 font-bold">
        <i class="pi pi-folder-open"></i>
        <span>{{ slotProps.data.materialFlowName }}</span>
      </div>
    </template>
  </DataTable>
</template>

<style>
.p-datatable-wrapper {
  height: calc(100vh - 135px);
}

/* tbody{
    font-size: 1rem;
} */

.abnormal-progressbar .p-progressbar-value {
  background: gray;
}
</style>
