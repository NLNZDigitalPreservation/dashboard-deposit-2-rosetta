import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useMaterialFlows = defineStore('MaterialFlows', () => {
    const data = ref([]);
    const filteredData = ref();
    const selectedData = ref([]);
    const rest: UseFetchApis = useFetch();

    const fetchAllData = () => {
        rest.get('/restful/setting/flow/all/get')
            .then((rsp: any) => {
                data.value = rsp;
            })
            .catch((err: any) => {
                console.log(err.message);
            });
    };

    return { data, filteredData, selectedData, fetchAllData };
});
