import { reactive } from 'vue';

export const globalSettings = reactive({
    id: -1,
    paused: false,
    pausedStartTime: 0,
    pausedEndTime: 0,
    delays: 60,
    delayUnit: 'second'
});

export const depositAccount = reactive({
    id: -1
});
