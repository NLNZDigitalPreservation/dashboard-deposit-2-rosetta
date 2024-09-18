<template>
    <div v-if="visibleDrawerMaterialFlow">
        <Toast />
        <ConfirmDialog></ConfirmDialog>
        <Drawer v-model:visible="visibleDrawerMaterialFlow" header="Material Flow Settings" class="!w-full md:!w-80 lg:!w-[50rem]" position="right">
            <DataTable v-model:selection="selectedRow" :value="materialFlowStore.dataList" :metaKeySelection="metaKey" dataKey="id" tableStyle="width:100%" showGridlines sortField="id" :sortOrder="1">
                <Column field="id" header="ID" sortable></Column>
                <Column field="producerName" header="Producer" sortable></Column>
                <Column field="materialFlowName" header="Material Flow" sortable>
                    <template #body="slotProps">
                        {{ slotProps.data.materialFlowName }}
                        <!-- <span v-if="!slotProps.data.enabled" style="display: inline-block; background: #6c757d; color: white; padding: 0 5px; border: 0.1px solid; border-radius: 8px">Disabled</span> -->
                        <Badge v-if="!slotProps.data.enabled" value="Disabled" severity="info"></Badge>
                    </template>
                </Column>
                <Column field="id" header="Action" alignFrozen="right" style="width: 8rem">
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
                        <ToggleSwitch v-model="selectedRow.enabled" inputId="enabledMaterialFlow" />
                        <label for="enabledMaterialFlow" class="ml-2"> If enable depositing for this material flow. </label>
                    </InputGroup>

                    <IconInputGroup :label="'Deposit Account'" :icon="'pi pi-angle-down'">
                        <InputText v-model="depositAccount" @click="opDepositAccount.toggle" readonly style="padding-left: 3px" />
                    </IconInputGroup>

                    <FlatInputGroup>
                        <IconInputGroup label="Producer" icon="pi pi-angle-down">
                            <InputText v-model="selectedRow.producerName" @click="opProducer.toggle" readonly style="padding-left: 3px" />
                        </IconInputGroup>
                        <IconInputGroup label="Material Flow" icon="pi pi-angle-down">
                            <InputText v-model="selectedRow.materialFlowName" @click="opMaterialFlow.toggle" readonly style="padding-left: 3px" />
                        </IconInputGroup>
                    </FlatInputGroup>

                    <FlatInputGroup>
                        <InputGroupAddon>Root Location</InputGroupAddon>
                        <InputText v-model="selectedRow.rootPath" />
                    </FlatInputGroup>
                </Fieldset>

                <Fieldset legend="Advanced Settings">
                    <FlatInputGroup>
                        <InputGroupAddon>Stream Path</InputGroupAddon>
                        <InputText v-model="selectedRow.streamLocation" />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Ingestion Completed File Name </InputGroupAddon>
                        <InputText v-model="selectedRow.injectionCompleteFileName" />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Max Active Days </InputGroupAddon>
                        <InputNumber v-model="selectedRow.maxActiveDays" mode="decimal" fluid />
                        <InputGroupAddon>Max Storage Days </InputGroupAddon>
                        <InputNumber v-model="selectedRow.maxSaveDays" mode="decimal" fluid />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Max Threads: </InputGroupAddon>
                        <InputGroupAddon>Mon</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.mon" mode="decimal" :min="0" :max="128" fluid />
                        <InputGroupAddon>Tue</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.tue" mode="decimal" :min="0" :max="128" fluid />
                        <InputGroupAddon>Wed</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.wed" mode="decimal" :min="0" :max="128" fluid />
                        <InputGroupAddon>Thu</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.thu" mode="decimal" :min="0" :max="128" fluid />
                        <InputGroupAddon>Fri</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.fri" mode="decimal" :min="0" :max="128" fluid />
                        <InputGroupAddon>Sat</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.sat" mode="decimal" :min="0" :max="128" fluid />
                        <InputGroupAddon>Sun</InputGroupAddon>
                        <InputNumber v-model="weeklyThreads.sun" mode="decimal" :min="0" :max="128" fluid />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Deletion Options For Actual Content </InputGroupAddon>
                        <Select v-model="selectedRow.actualContentDeleteOptions" :options="deletionOptions" optionLabel="name" optionValue="code" />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Backup Options For Actual Content </InputGroupAddon>
                        <Select v-model="selectedRow.actualContentBackupOptions" :options="backupOptions" optionLabel="name" optionValue="code" />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Backup Location </InputGroupAddon>
                        <InputText v-model="selectedRow.backupPath" />
                    </FlatInputGroup>
                    <FlatInputGroup>
                        <InputGroupAddon>Sub folders for backup </InputGroupAddon>
                        <Textarea v-model="selectedRow.backupSubFolders" rows="5" cols="120" />
                    </FlatInputGroup>
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
        <Popover ref="opDepositAccount" appendTo="body" style="width: 60rem">
            <DepositAccountSelectForm @onSelected="onDepositAccountSelected" />
        </Popover>

        <Popover ref="opProducer" appendTo="body" style="width: 60rem">
            <RawProducerSelectForm @onSelected="onProducerSelected" :account="selectedRow.depositAccountId" />
        </Popover>

        <Popover ref="opMaterialFlow" appendTo="body" style="width: 60rem">
            <RawMaterialFlowSelectForm @onSelected="onMaterialFlowSelected" :account="selectedRow.depositAccountId" :producer="selectedRow.producerId" />
        </Popover>
    </div>
</template>

<script setup lang="ts">
import { useSettingsDepositAccountStore, useSettingsMaterialFlowStore } from '@/stores/settings';
import type { MaterialFlow } from '@/types/deposit';
import { defineExpose, onMounted, reactive, ref } from 'vue';
import IconInputGroup from '../IconInputGroup.vue';
import DepositAccountSelectForm from './DepositAccountSelectForm.vue';
import RawMaterialFlowSelectForm from './RawMaterialFlowSelectForm.vue';
import RawProducerSelectForm from './RawProducerSelectForm.vue';

const visibleDialogMaterialFlow = ref(false);
const opDepositAccount = ref();
const opProducer = ref();
const opMaterialFlow = ref();

const metaKey = ref(true);

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
    delays: undefined,
    delayUnit: undefined,
    weeklyMaxConcurrency: [1, 1, 1, 1, 1, 1, 1],
    actualContentDeleteOptions: 'notDelete',
    backupEnabled: false,
    actualContentBackupOptions: 'notBackup',
    backupPath: '',
    backupSubFolders: '',
    auditRst: true,
    auditMsg: 'OK'
};
const selectedRow = ref(Object.assign({}, initialData) as MaterialFlow);

const depositAccountStore = useSettingsDepositAccountStore();

const materialFlowStore = useSettingsMaterialFlowStore();

const depositAccount = ref();

const weeklyThreads = reactive({
    mon: 1,
    tue: 1,
    wed: 1,
    thu: 1,
    fri: 1,
    sat: 1,
    sun: 1
});
const deletionOptions = ref([
    { name: 'Not delete', code: 'notDelete' },
    { name: 'Delete when exceeds the MAX Storage Days', code: 'deleteExceedMaxStorageDays' },
    { name: 'Delete instantly when finished', code: 'deleteInstantly' }
]);

const backupOptions = ref([
    { name: 'Not backup', code: 'notBackup' },
    { name: 'Backup the contents without the subfolder name', code: 'backupContentsWithoutSubFolderName' },
    { name: 'Backup the structure of the subfolder', code: 'backupSubFolder' }
]);

const onDepositAccountSelected = (data: any) => {
    opDepositAccount.value.hide();
    if (!data || selectedRow.value.depositAccountId == data.id) {
        return;
    }
    selectedRow.value.depositAccountId = data.id;
    depositAccount.value = data.depositUserInstitute + ' | ' + data.depositUserName;
    selectedRow.value.producerId = '';
    selectedRow.value.producerName = '';
    selectedRow.value.materialFlowId = '';
    selectedRow.value.materialFlowName = '';
};

const onProducerSelected = (data: any) => {
    opProducer.value.hide();
    if (!data || selectedRow.value.producerId == data.id) {
        return;
    }
    selectedRow.value.producerId = data.id;
    selectedRow.value.producerName = data.id + '-' + data.name;
    selectedRow.value.materialFlowId = '';
    selectedRow.value.materialFlowName = '';
};

const onMaterialFlowSelected = (data: any) => {
    opMaterialFlow.value.hide();
    if (!data || selectedRow.value.materialFlowId == data.id) {
        return;
    }
    selectedRow.value.materialFlowId = data.id;
    selectedRow.value.materialFlowName = data.id + '-' + data.name;
};

const onNew = () => {
    depositAccount.value = '';
    selectedRow.value = Object.assign({}, initialData) as MaterialFlow;
    visibleDialogMaterialFlow.value = true;
};
const onDelete = (selectedData: MaterialFlow) => {
    materialFlowStore.deleteConfirm(selectedData);
};
const onEdit = async (selectedData: MaterialFlow) => {
    selectedRow.value = selectedData;
    weeklyThreads.mon = selectedData.weeklyMaxConcurrency[0];
    weeklyThreads.tue = selectedData.weeklyMaxConcurrency[1];
    weeklyThreads.wed = selectedData.weeklyMaxConcurrency[2];
    weeklyThreads.thu = selectedData.weeklyMaxConcurrency[3];
    weeklyThreads.fri = selectedData.weeklyMaxConcurrency[4];
    weeklyThreads.sat = selectedData.weeklyMaxConcurrency[5];
    weeklyThreads.sun = selectedData.weeklyMaxConcurrency[6];
    const data = await depositAccountStore.queryRow(selectedData.depositAccountId);
    depositAccount.value = data.depositUserInstitute + ' | ' + data.depositUserName;
    visibleDialogMaterialFlow.value = true;
};
const onSave = async () => {
    selectedRow.value.weeklyMaxConcurrency = [weeklyThreads.mon, weeklyThreads.tue, weeklyThreads.wed, weeklyThreads.thu, weeklyThreads.fri, weeklyThreads.sat, weeklyThreads.sun];
    const ret = await materialFlowStore.saveRow(selectedRow.value);
    if (ret) {
        visibleDialogMaterialFlow.value = false;
    }
};
onMounted(() => {
    materialFlowStore.queryAllRows();
});
const visibleDrawerMaterialFlow = ref(false);
const toggle = () => {
    visibleDrawerMaterialFlow.value = !visibleDrawerMaterialFlow.value;
};
defineExpose({ toggle });
</script>
