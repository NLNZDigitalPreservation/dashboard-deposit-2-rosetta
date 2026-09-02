<script setup lang="ts">
import { useFetch } from '@/utils/rest.api';
import { ref } from 'vue';

const emit = defineEmits(['onSelected']);
const props = defineProps<{
    account?: number;
    producer?: string;
}>();
const rest = useFetch();
const rows = ref(10);
const first = ref(0);
const page = ref(0);
const totalRecords = ref(0);
const filterName = ref();
const curPageRows = ref([]);

const onRowSelect = (event: any) => {
    emit('onSelected', event.data);
};

const _search = () => {
    if (!props.account || props.account < 0) {
        console.error('Invalid account: ' + props.account);
        return;
    }

    if (!props.producer || props.producer.length == 0) {
        console.error('Invalid producer: ' + props.producer);
        return;
    }

    const searchCondition = {
        depositAccountId: props.account,
        producerId: props.producer,
        offset: page.value,
        limit: rows.value,
        name: filterName.value
    };

    rest.post('/restful/raw/materialflows', searchCondition).then((datasets: any) => {
        if (!datasets) {
            console.error('Can not get raw meterial flows: ' + datasets);
            return;
        }

        if (datasets && datasets.total_record_count) {
            totalRecords.value = datasets.total_record_count;
        } else {
            totalRecords.value = 0;
        }
        if (datasets && datasets.profile_material_flow) {
            curPageRows.value = datasets.profile_material_flow;
        } else {
            curPageRows.value = [];
        }
    });
};

const onSearch = () => {
    first.value = 0;
    page.value = 0;
    totalRecords.value = 0;
    curPageRows.value = [];
    _search();
};

const onPage = (event: any) => {
    page.value = event.page;
    _search();
};
_search();
</script>

<template>
    <DataTable :value="curPageRows" dataKey="id" selectionMode="single" @row-select="onRowSelect" tableStyle="width:100%;" sortField="id" :sortOrder="1" :rows="rows" scrollable scrollHeight="25rem">
        <template #header>
            <div class="flex items-center justify-between w-full">
                <InputGroup style="width: 16rem">
                    <InputText v-model="filterName" placeholder="MaterialFlow name" />
                    <Button icon="pi pi-search" @click="onSearch" />
                </InputGroup>
                <span>Records: {{ totalRecords }}</span>
                <Paginator v-model:first="first" :rows="rows" :totalRecords="totalRecords" :pageLinkSize="3" @page="onPage"></Paginator>
            </div>
        </template>
        <Column field="id" header="ID"></Column>
        <Column field="name" header="MaterialFlow Name"></Column>
    </DataTable>
</template>
