<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue";
import { useToast } from 'primevue/usetoast';
import { FilterMatchMode, FilterOperator } from "primevue/api";
import { MaterialFlow, DepositJob } from "@/types/deposit";
import { useJobListDTO, keywords } from "@/stores/depositjob";

const jobList = useJobListDTO();
const selectedJobs = ref([]);
const expandedRowGroups = ref([]);

watch(keywords, async (newValue, oldValue) => {
  jobList.filter(keywords.value);
});

const updateFirst = (firstRow: number) => {
  // console.log("first row="+firstRow);
};

const updateRows = (rows: number) => {
  // console.log("rows="+rows);
};

// const page = (event: any) => {
//   console.log("pages event=" + event);
// };

// const sort = (event: any) => {
//   console.log("sort event=" + event);
// };

onMounted(() => {
  jobList.fetchAllData();
});

const cm = ref();
const toast = useToast();
const selectedContextRow = ref();
const contextMenuModel = ref([
    {label: 'View', icon: 'pi pi-fw pi-search', command: () => viewProduct(selectedContextRow)},
    {label: 'Delete', icon: 'pi pi-fw pi-times', command: () => deleteProduct(selectedContextRow)}
]);
const onRowContextMenu = (event:any) => {
    cm.value.show(event.originalEvent);
};

</script>
<template>
  <ContextMenu ref="cm" :model="contextMenuModel" @hide="selectedContextRow = null" />
  <DataTable
    v-model:expandedRowGroups="expandedRowGroups"
    :value="jobList.listJobsFiltered"
    v-model:selection="selectedJobs"
    contextMenu v-model:contextMenuSelection="selectedContextRow"
    @rowContextmenu="onRowContextMenu"
    dataKey="id"
    size="small"
    rowGroupMode="subheader"
    groupRowsBy="materialFlowName"
    sortMode="single"
    sortField="materialFlowName"
    :sortOrder="1"
    paginator
    :rows="50"
    :rowsPerPageOptions="[20, 50, 100, 200]"
    scrollable
    scrollHeight="100%"
    showGridlines
    stripedRows
    resizableColumns
    columnResizeMode="expand"    
    @sort="sort"
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
      class="font-bold"
    ></Column>
    <Column field="id" header="ID" sortable></Column>
    <Column
      field="injectionTitle"
      header="JobTitle"
      style="min-width: 400px"
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

  <!-- <Paginator
    :rows="3"
    :totalRecords="120"
    :rowsPerPageOptions="[3, 15, 30, 50, 100, 200]"
    @update:first="updateFirst"
    @update:rows="updateRows"
    @page="page"
  /> -->
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
