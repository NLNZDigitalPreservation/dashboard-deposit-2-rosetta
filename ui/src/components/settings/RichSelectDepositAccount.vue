<script setup lang="ts">
import { useSettingsDepositAccountStore } from '@/stores/settings';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineEmits, onMounted, ref } from 'vue';

const emit = defineEmits(['onSelected']);
const rows = ref(10);
const rest: UseFetchApis = useFetch();
const depositAccountStore = useSettingsDepositAccountStore();
depositAccountStore.queryAllRows();

const onRowSelect = (event: any) => {
    emit('onSelected', event.data);
};

onMounted(() => {});
</script>

<template>
    <Toolbar>
        <template #start> <span>DepositAccount</span> </template>
        <template #end> <Paginator :rows="rows" :totalRecords="12000" :pageLinkSize="3"></Paginator> </template>
    </Toolbar>

    <div class="mt-5">
        <DataTable :value="depositAccountStore.dataList" dataKey="id" selectionMode="single" @row-select="onRowSelect" tableStyle="width:100%;" sortField="id" :sortOrder="1" :rows="rows" scrollable scrollHeight="25rem">
            <Column field="id" header="ID" sortable></Column>
            <Column field="depositUserInstitute" header="Institute" sortable></Column>
            <Column field="depositUserName" header="User Name" sortable></Column>
            <Column field="auditMsg" header="Audit" sortable> </Column>
        </DataTable>
    </div>
</template>
