<template>
    <div v-if="visibleDrawerMaterialFlow">
        <Toast />
        <ConfirmDialog></ConfirmDialog>
        <Drawer v-model:visible="visibleDrawerMaterialFlow" header="Material Flow Settings" class="!w-full md:!w-80 lg:!w-[50rem]" position="right">
            <DataTable v-model:selection="flow.selectedRow" :value="flow.dataList" :metaKeySelection="metaKey" dataKey="id" tableStyle="width:100%" showGridlines sortField="id" :sortOrder="1">
                <Column field="id" header="ID" sortable></Column>
                <Column field="producerName" header="Producer" sortable></Column>
                <Column field="materialFlowName" header="Material Flow" sortable></Column>
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
                    <span class="font-bold whitespace-nowrap">Material Flow Setting: {{ flow.id }}</span>
                </div>
            </template>

            <!-- <IconField>
                <InputText v-model="flow.id" />
                <InputIcon class="pi pi-spin pi-spinner" />
            </IconField> -->

            <Fieldset legend="Basic Settings">
                <!-- <div class="grid grid-cols-12 gap-2 inline-flex items-center justify-content-center">
                        <div class="col-span-12 md:col-span-2">
                            <FlatInputGroup>
                                <InputGroupAddon>ID</InputGroupAddon>
                                <InputText v-model="flow.id" readonly />
                            </FlatInputGroup>
                        </div>

                        <div class="col-span-12 md:col-span-10">
                            <InputGroup class="mt-2 mb-2">
                                <ToggleSwitch v-model="flow.enabled" inputId="enabledMaterialFlow" />
                                <label for="enabledMaterialFlow" class="ml-2"> If enable depositing for this material flow. </label>
                            </InputGroup>
                        </div>
                    </div> -->
                <InputGroup class="mt-2 mb-2">
                    <ToggleSwitch v-model="flow.enabled" inputId="enabledMaterialFlow" />
                    <label for="enabledMaterialFlow" class="ml-2"> If enable depositing for this material flow. </label>
                </InputGroup>

                <IconInputGroup :label="'Deposit Account'" :icon="'pi pi-angle-down'">
                    <InputText v-model="depositAccount" @click="opDepositAccount.toggle" readonly />
                </IconInputGroup>

                <FlatInputGroup>
                    <IconInputGroup label="Producer" icon="pi pi-angle-down">
                        <InputText v-model="flow.producerName" @click="opDepositAccount.toggle" readonly />
                    </IconInputGroup>
                    <IconInputGroup label="Material Flow" icon="pi pi-angle-down">
                        <InputText v-model="flow.materialFlowName" @click="opDepositAccount.toggle" readonly />
                    </IconInputGroup>
                </FlatInputGroup>

                <FlatInputGroup>
                    <InputGroupAddon>Root Location</InputGroupAddon>
                    <InputText v-model="flow.rootPath" />
                </FlatInputGroup>
            </Fieldset>

            <Fieldset legend="Advanced Settings">
                <FlatInputGroup>
                    <InputGroupAddon>Stream Path</InputGroupAddon>
                    <InputText v-model="flow.streamLocation" />
                </FlatInputGroup>
                <FlatInputGroup>
                    <InputGroupAddon>Ingestion Completed File Name </InputGroupAddon>
                    <InputText v-model="flow.injectionCompleteFileName" />
                </FlatInputGroup>
                <FlatInputGroup>
                    <InputGroupAddon>Max Active Days </InputGroupAddon>
                    <InputText v-model="flow.maxActiveDays" />
                    <InputGroupAddon>Max Storage Days </InputGroupAddon>
                    <InputText v-model="flow.maxSaveDays" />
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
                    <Select v-model="flow.actualContentDeleteOptions" :options="deletionOptions" optionLabel="name" optionValue="code" />
                </FlatInputGroup>
                <FlatInputGroup>
                    <InputGroupAddon>Backup Options For Actual Content </InputGroupAddon>
                    <Select v-model="flow.actualContentBackupOptions" :options="backupOptions" optionLabel="name" optionValue="code" />
                </FlatInputGroup>
                <FlatInputGroup>
                    <InputGroupAddon>Backup Location </InputGroupAddon>
                    <InputText v-model="flow.backupPath" />
                </FlatInputGroup>
                <FlatInputGroup>
                    <InputGroupAddon>Sub folders for backup </InputGroupAddon>
                    <Textarea v-model="flow.backupSubFolders" rows="5" cols="120" />
                </FlatInputGroup>
            </Fieldset>

            <Fieldset legend="Health Audit">
                <Message v-if="flow.auditRst" severity="success" :closable="false">{{ flow.auditMsg }}</Message>
                <Message v-if="!flow.auditRst" severity="warn" :closable="false">{{ flow.auditMsg }}</Message>
            </Fieldset>

            <template #footer>
                <Button label="Save" @click="onSave()" autofocus />
                <Button label="Cancel" outlined @click="visibleDialogMaterialFlow = false" autofocus />
            </template>
        </Dialog>
        <Popover ref="opDepositAccount" appendTo="body" style="width: 60rem">
            <RichSelectDepositAccount @onSelected="onDepositAccountSelected" />
        </Popover>

        <RichSelectProducer ref="opProducer" />
    </div>
</template>

<script setup lang="ts">
import { useSettingsDepositAccountStore, useSettingsMaterialFlowStore } from '@/stores/settings';
import { defineExpose, reactive, ref } from 'vue';
import IconInputGroup from '../IconInputGroup.vue';

const visibleDialogMaterialFlow = ref(false);
const opDepositAccount = ref();
const opProducer = ref();
const metaKey = ref(true);
const selectOptions = ref();

const depositAccountStore = useSettingsDepositAccountStore();

const flow = useSettingsMaterialFlowStore();
flow.init();
flow.queryAllRows();

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
    if (!data) {
        return;
    }
    flow.depositAccountId = data.id;
    depositAccount.value = data.depositUserInstitute + ' | ' + data.depositUserName;
    flow.producerId = '';
    flow.producerName = '';
    flow.materialFlowId = '';
    flow.materialFlowName = '';
};

const onNew = () => {
    flow.init();
    visibleDialogMaterialFlow.value = true;
};
const onDelete = (selectedData: any) => {
    flow.deleteConfirm(selectedData);
};
const onEdit = (selectedData: any) => {
    flow.setData(selectedData);
    visibleDialogMaterialFlow.value = true;
};
const onSave = async () => {
    const ret = await flow.saveRow();
    if (ret) {
        visibleDialogMaterialFlow.value = false;
    }
};

const visibleDrawerMaterialFlow = ref(false);
const toggle = () => {
    visibleDrawerMaterialFlow.value = !visibleDrawerMaterialFlow.value;
};
defineExpose({ toggle });
</script>
