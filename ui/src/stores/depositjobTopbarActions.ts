import DepositJobSearchDialog from '@/components/jobs/DepositJobSearchDialog.vue';
import { useJobListDTO } from '@/stores/depositjob';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineStore } from 'pinia';
import { useDialog } from 'primevue/usedialog';
import { useToast } from 'primevue/usetoast';
import { ref } from 'vue';

export const useTopbarActions = defineStore('TopbarActions', () => {
    const toast = useToast();
    const dialog = useDialog();
    const jobList = useJobListDTO();
    const rest: UseFetchApis = useFetch();
    const selectedContextRow = ref();

    const onSearch = (searchOptions: any) => {};
    const openSearchDialog = () => {
        const dialogRef = dialog.open(DepositJobSearchDialog, {
            props: {
                header: 'Advanced Filter',
                closable: true,
                style: {
                    width: '75rem'
                },
                modal: true
            },
            onClose: (options: any) => {
                if (options.data.buttonType === 'confirm') {
                    onSearch(options.data);
                }
            }
        });
    };

    return { openSearchDialog };
});
