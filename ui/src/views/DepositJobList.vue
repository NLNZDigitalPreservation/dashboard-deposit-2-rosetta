

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import {MaterialFlow, DepositJob} from '@/types/deposit';
import {depositJobList, formatDatetimeFromEpochMilliSeconds} from '@/stores/depositjob';
import { type UseFetchApis, useFetch } from '@/utils/rest.api'

const rest: UseFetchApis = useFetch()

onMounted(() => {
    rest.get('/restful/deposit-jobs/active/get').then((data: any) => {
      console.log(data)
      depositJobList.value = data
    }).catch((err: any) => {
      console.log(err.message)
    });
});
</script>
<template>
   <DataTable :value="depositJobList" rowGroupMode="subheader" groupRowsBy="representative.name" sortMode="single" sortField="representative.name" :sortOrder="1" scrollable scrollHeight="400px">
        <Column field="appliedFlowSetting.materialFlowName" header="MaterialFlow"></Column>
        <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
        <Column field="id" header="ID" style="min-width: 90px"></Column>
        <Column field="injectionTitle" header="JobTitle" style="min-width: 485px"></Column>
        <Column field="stage" header="Stage" style="min-width: 485px"></Column>
        <Column field="state" header="State" style="min-width: 485px"></Column>
        <!-- <Column field="state" header="Status" style="min-width: 180px">
            <template #body="{ data }">
                <span>{{ data.stage }} - {{ data.state }}</span>
            </template>
        </Column> -->
        <Column field="initialTime" header="JobInitialTime" style="min-width: 200px">
            <template #body="{ data }">
                <span>{{ formatDatetimeFromEpochMilliSeconds(data.initialTime) }}</span>
            </template>
        </Column>
        <Column field="latestTime" header="LatestUpdateTime" style="min-width: 200px">
            <template #body="{ data }">
                <span>{{ formatDatetimeFromEpochMilliSeconds(data.latestTime) }}</span>
            </template>
        </Column>

        <template #groupheader="slotProps">
            <div class="flex align-items-center gap-2">
                <!-- <img :alt="slotProps.data.representative.name" :src="'/demo/images/avatar/' + slotProps.data.representative.image" width="32" style="vertical-align: middle" /> -->
                <span>{{ slotProps.data.appliedFlowSetting.materialFlowName }}</span>
            </div>
        </template>
        <!-- <template #groupfooter="slotProps">
            <td style="text-align: right" class="text-bold pr-6">Total Customers: {{ calculateCustomerTotal(slotProps.data.representative.name) }}</td>
        </template> -->
    </DataTable>
</template>