<script setup>
</script>

<template>
    <div class="card">
        <Toolbar style="padding: 5px; background: linear-gradient(to right, #212529, #212529, #32CD32, #212529);">
            <template #start>
                <div id="toolkit-container-left" class="flex align-items-center gap-2">
                    <Avatar image="/natlib-logo-orange.png" style="width: 32px; height: 32px;" />
                    <InputText placeholder="Filter" type="text" class="w-8rem sm:w-auto" />
                    <Button icon="pi pi-search" label="&nbsp;&nbsp;Search" />
                    <Button icon="pi pi-refresh" label="&nbsp;&nbsp;Reload" />
                    <Button icon="pi pi-download" label="&nbsp;&nbsp;Export Selected Jobs" />
                    <Button icon="pi pi-file-edit" label="&nbsp;&nbsp;Redeposit" />
                </div>
            </template>

            <template #end>
                <div class="flex align-items-center gap-2">
                    <Button label="Share" severity="contrast" size="small" />
                    <Avatar image="https://primefaces.org/cdn/primevue/images/avatar/amyelsner.png" style="width: 32px; height: 32px" />
                </div>
            </template>
        </Toolbar>
    </div>
</template>

<style scoped>
    #toolkit-container-left > * {    
        margin-bottom: 8px;
        margin-left: 1em;
    }

    .p-button-icon::after {
        content: "&nbsp;";
    }

</style>