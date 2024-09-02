<script setup lang="ts">
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineEmits, defineExpose, onMounted, reactive, ref } from 'vue';

const emit = defineEmits(['onProducerSelect']);
const rest: UseFetchApis = useFetch();

const op = ref();
const onRowSelect = (event: any) => {
    op.value.hide();
    emit('onProducerSelect', event.data);
};
const depositAccountId = ref(0);

// const totalRows = ref();
// const rowsPerPage = ref(10);
// const dataListPage = ref();

const dataList = ref();
const filterName = ref();
const pageState = reactive({
    first: 0,
    rows: 10,
    page: 0,
    pageCount: 1
});
const search = async () => {
    const searchCondition = {
        depositAccountId: depositAccountId.value,
        offset: pageState.first,
        limit: pageState.rows,
        name: filterName.value
    };
    const rsp = (dataList.value = await rest.get('/restful/raw/producers'));
    if (!rsp) {
        return;
    }
    pageState.pageCount = rsp.total_record_count;
    dataList.value = rsp.producer;
};

const onPageStateUpdate = (event: any) => {
    pageState.first = event.first;
    pageState.rows = event.rows;
    pageState.page = event.page;
    pageState.pageCount = event.pageCount;
    search();
};

const toggle = (accountId: number) => {
    depositAccountId.value = accountId;
    search();
    op.value.show();
};

onMounted(() => {});

defineExpose({ toggle });
</script>

<template>
    <Popover ref="op" appendTo="body">
        <DataTable
            :value="dataList"
            dataKey="id"
            @row-select="onRowSelect"
            tableStyle="width:50rem"
            showGridlines
            sortField="id"
            :sortOrder="1"
            paginator
            :rows="pageState.rows"
            :totalRecords="pageState.pageCount"
            :rowsPerPageOptions="[10, 50, 100]"
            @page="onPageStateUpdate"
        >
            <template #header>
                <div class="flex flex-wrap items-center justify-between gap-2">
                    <span class="text-xl font-bold">Producers</span>
                    <InputGroup>
                        <InputText v-model="filterName" placeholder="Producer name" />
                        <Button icon="pi pi-search" severity="warn" />
                    </InputGroup>
                </div>
            </template>
            <Column field="id" header="ID"></Column>
            <Column field="name" header="Pruducer Name"></Column>
            <Column field="active" header="Active">
                <template #body="{ data }">
                    <span v-if="data.active" class="pi pi-check"></span>
                    <span v-else class="pi pi-times"></span>
                </template>
            </Column> </DataTable
    ></Popover>
</template>
