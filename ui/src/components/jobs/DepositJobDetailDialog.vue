<script setup lang="ts">
import { ref, reactive, inject, onMounted } from 'vue';
import PropertyField from './PropertyField.vue';

const dialogRef:any = inject('dialogRef');


const job=ref();

const auditMessage=reactive ({
    content: "OK",
    severity: "success"
});

onMounted(() => {
    const params = dialogRef.value.data; // {user: 'primetime'}
    job.value=params.job;
})

</script>

<template>
    <div>
        <Message :severity="auditMessage.severity">{{ auditMessage.content }}</Message>

        <Fieldset class="flex flex-col" legend="Basic Properties">
            <PropertyField key="Job Title" :value="job.title"></PropertyField>
        </Fieldset>

        <Fieldset legend="SIP Properties">
            
        </Fieldset>
    </div>  
</template>


<style scoped>

</style>