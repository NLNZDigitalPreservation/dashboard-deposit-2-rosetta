<script setup lang="ts">
import { useSettingsDepositAccountStore } from '@/stores/settings';
import { useFetch } from '@/utils/rest.api';
import { computed, onMounted, ref } from 'vue';

const emit = defineEmits(['onSelected']);

const rest = useFetch();
const depositAccountStore = useSettingsDepositAccountStore();
const allAccounts = ref([]);

const rows = ref(10);
const first = ref(0);
const totalRecords = ref(0);

const onRowSelect = (event: any) => {
    emit('onSelected', event.data);
};

const curPageRows = computed(() => {
    const dataset = [];
    for (let idx = first.value; idx < first.value + rows.value && idx < allAccounts.value.length; idx++) {
        dataset.push(allAccounts.value[idx]);
    }
    return dataset;
});

const onPage = (event: any) => {
    first.value = event.first;
};

onMounted(async () => {
    allAccounts.value = await depositAccountStore.queryAllRows();
    totalRecords.value = allAccounts.value.length;
});
</script>

<template>
    <DataTable :value="curPageRows" dataKey="id" selectionMode="single" @row-select="onRowSelect" tableStyle="width:100%;" sortField="id" :sortOrder="1" :rows="rows" scrollable scrollHeight="25rem">
        <template #header>
            <div class="flex items-center justify-between w-full">
                <span>DepositAccount</span>
                <Paginator :rows="rows" :totalRecords="totalRecords" :pageLinkSize="3" @page="onPage"></Paginator>
            </div>
        </template>
        <Column field="id" header="ID" sortable></Column>
        <Column field="depositUserInstitute" header="Institute" sortable></Column>
        <Column field="depositUserName" header="User Name" sortable></Column>
        <Column field="auditMsg" header="Audit" sortable> </Column>
    </DataTable>
</template>
