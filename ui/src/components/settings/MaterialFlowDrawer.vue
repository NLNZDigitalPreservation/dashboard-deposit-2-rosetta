<template>
    <div v-if="visibleDrawerMaterialFlow">
        <Toast />
        <ConfirmDialog></ConfirmDialog>
        <Drawer v-model:visible="visibleDrawerMaterialFlow" header="Material Flow Settings" class="!w-full md:!w-80 lg:!w-[50rem]" position="right">
            <DataTable v-model:selection="materialFlows.selectedRow" :value="materialFlows.dataList" :metaKeySelection="metaKey" dataKey="id" @rowSelect="onRowSelect" tableStyle="width:100%" showGridlines sortField="id" :sortOrder="1">
                <Column field="id" header="ID" sortable>
                    <!-- <template #body="{ data }">
                    <Button as="a" :label="data.id" @click="onEdit(data)" text />
                </template> -->
                </Column>
                <Column field="producerName" header="Producer" sortable></Column>
                <Column field="materialFlowName" header="Material Flow" sortable>
                    <!-- <template #body="{ data }">
                    <a @click.stop.prevent="onEdit(data)" href="#">
                        {{ data.materialFlowName }}
                        <Badge v-if="!data.enabled" value="Disabled" severity="contrast"></Badge>
                    </a>
                </template> -->
                </Column>
                <Column field="auditMsg" header="Audit" sortable> </Column>
                <Column field="id" header="Action" alignFrozen="right">
                    <template #body="{ data }">
                        <Button icon="pi pi-pen-to-square" @click="onEdit(data)" text />
                        <Button icon="pi pi-trash" severity="danger" @click="onDelete(data)" text />
                    </template>
                </Column>
            </DataTable>

            <template #footer>
                <Fluid>
                    <Button label="New" icon="pi pi-plus" @click="onNew()" raised />
                </Fluid>
            </template>
        </Drawer>

        <Dialog v-model:visible="visibleDialogMaterialFlow" modal header="Edit Profile" :style="{ width: '75rem' }">
            <template #header>
                <div class="inline-flex items-center justify-center gap-2">
                    <span class="font-bold whitespace-nowrap">Material Flow Setting: {{ selectedRow.id }}</span>
                </div>
            </template>
            <Fluid>
                <Fieldset legend="Basic Settings">
                    <InputGroup class="mt-2 mb-2">
                        <ToggleSwitch v-model="materialFlows.enabled" inputId="enabledMaterialFlow" />
                        <label for="enabledMaterialFlow" class="ml-2"> If enable depositing for this material flow. </label>
                    </InputGroup>

                    <InputGroup class="mt-2 mb-2">
                        <InputGroupAddon>ID</InputGroupAddon>
                        <InputText v-model="selectedRow.id" disabled="true" />
                    </InputGroup>
                    <InputGroup>
                        <InputGroupAddon>Producer</InputGroupAddon>
                        <InputText placeholder="Keyword" />
                        <Button icon="pi pi-sort-down-fill" @click="opProducer.toggle(selectedRow)" />
                    </InputGroup>
                </Fieldset>

                <Fieldset legend="Health Audit">
                    <Message v-if="selectedRow.auditRst" severity="success" :closable="false">{{ selectedRow.auditMsg }}</Message>
                    <Message v-if="!selectedRow.auditRst" severity="warn" :closable="false">{{ selectedRow.auditMsg }}</Message>
                </Fieldset>
            </Fluid>
            <template #footer>
                <Button label="Save" @click="onSave()" autofocus />
                <Button label="Cancel" outlined @click="visibleDialogMaterialFlow = false" autofocus />
            </template>
        </Dialog>

        <RawProducerComponent ref="opProducer" />
    </div>
</template>

<script setup lang="ts">
import { useSettingsMaterialFlowStore } from '@/stores/settings';
import { defineExpose, reactive, ref } from 'vue';
import RawProducerComponent from './RawProducerComponent.vue';

const visibleDialogMaterialFlow = ref(false);
const opProducer = ref();

const materialFlows = useSettingsMaterialFlowStore();
materialFlows.init();
materialFlows.queryAllRows();

const enabled = ref(true);
const row = reactive({
    id: undefined,
    enabled: true,
    depositAccountId: 0,
    materialFlowId: '',
    materialFlowName: '',
    producerId: '',
    producerName: '',
    rootPath: '',
    streamLocation: 'content',
    injectionCompleteFileName: '',
    maxActiveDays: 60,
    maxSaveDays: 30,
    delays: null,
    delayUnit: null,
    weeklyMaxConcurrency: [1, 1, 1, 1, 1, 1, 1],
    actualContentDeleteOptions: 'notDelete',
    backupEnabled: false,
    actualContentBackupOptions: 'notBackup',
    backupPath: '',
    backupSubFolders: '',
    auditRst: true,
    auditMsg: 'OK'
});
const initialData = {
    id: undefined,
    enabled: true,
    depositAccountId: 0,
    materialFlowId: '',
    materialFlowName: '',
    producerId: '',
    producerName: '',
    rootPath: '',
    streamLocation: 'content',
    injectionCompleteFileName: '',
    maxActiveDays: 60,
    maxSaveDays: 30,
    delays: null,
    delayUnit: null,
    weeklyMaxConcurrency: [1, 1, 1, 1, 1, 1, 1],
    actualContentDeleteOptions: 'notDelete',
    backupEnabled: false,
    actualContentBackupOptions: 'notBackup',
    backupPath: '',
    backupSubFolders: '',
    auditRst: true,
    auditMsg: 'OK'
};
const selectedRow = ref(Object.assign({}, initialData));
const metaKey = ref(true);

const onNew = () => {
    selectedRow.value = Object.assign({}, initialData);
    visibleDialogMaterialFlow.value = true;
};
const onDelete = (selectedData: any) => {
    materialFlows.deleteConfirm(selectedData);
};
const onEdit = (selectedData: any) => {
    selectedRow.value = selectedData;
    visibleDialogMaterialFlow.value = true;
};
const onSave = async () => {
    const ret = await materialFlows.saveRow(selectedRow.value);
    if (ret) {
        visibleDialogMaterialFlow.value = false;
    }
};
const onRowSelect = (event: any) => {
    selectedRow.value = event.data;
};

const visibleDrawerMaterialFlow = ref(false);
const toggle = () => {
    visibleDrawerMaterialFlow.value = !visibleDrawerMaterialFlow.value;
};
defineExpose({ toggle });
</script>
