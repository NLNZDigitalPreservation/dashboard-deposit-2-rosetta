<script setup lang="ts">
import { ref, reactive, inject, onMounted} from 'vue';
import PropertyField from './PropertyField.vue';

const dialogRef:any = inject('dialogRef');

// const job=reactive({
//     id: 0,
//     injectionTitle: "",
//     injectionPath: "",
//     materialFlowName: "",
//     initialTime: "",
//     latestTime: "",
//     fileCount: 0,
//     fileSize: 0,
//     stage: "",
//     state: "",
//     depositStartTime: "",
//     depositEndTime: "",
//     sipID: "",
//     sipModule: "",
//     sipStage: "",
//     sipStatus: "",
//     resultMessage: "",
// });
const job=ref();
job.value={};

const auditMessage=reactive ({
    content: "OK",
    severity: "success"
});


onMounted(() => {
    const params = dialogRef.value.data; // {user: 'primetime'}
    const data=params.job;
    if(!data){
        console.log("No job data transfered to the details page");
        return;
    }

    // job.id=data.id;
    // job.injectionTitle=data.injectionTitle;
    // job.injectionPath=data.injectionPath;
    // job.materialFlowName=data.materialFlowName;
    // job.initialTime=data.initialTime;
    // job.latestTime=data.latestTime;
    // job.fileCount=data.fileCount;
    // job.fileSize=data.fileSize;
    // job.stage=data.stage;
    // job.state=data.state;
    // job.depositStartTime=data.depositStartTime;
    // job.depositEndTime=data.depositEndTime;
    // job.sipID=data.sipID;
    // job.sipModule=data.sipModule;
    // job.sipStage=data.sipStage;
    // job.sipStatus=data.sipStatus;
    // job.resultMessage=data.resultMessage;

    job.value=data;
});

</script>

<template>
    <div>
        <Message :severity="auditMessage.severity" :closable="false">{{ auditMessage.content }}</Message>

        <Fieldset class="flex flex-col" legend="Basic Properties">
            <PropertyField title="Job Title" :value="job.injectionTitle"></PropertyField>
        </Fieldset>

        <Fieldset legend="SIP Properties">
            
        </Fieldset>
    </div>  
</template>


<style scoped>

</style>