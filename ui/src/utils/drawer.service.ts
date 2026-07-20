import { reactive, shallowRef } from 'vue';

// The global state of our dynamic drawer
const state = reactive({
    visible: false,
    component: null,
    props: {},
    options: {},
    resolve: null,
    reject: null
} as any);

export const useDrawerService = () => {
    const open = (component: any, options = {} as any) => {
        return new Promise((resolve, reject) => {
            state.component = shallowRef(component);
            state.props = options.props || {};
            state.options = options || {};
            state.resolve = resolve;
            state.reject = reject;
            state.visible = true;
        });
    };

    const close = (data: any) => {
        state.visible = false;
        if (state.resolve) state.resolve(data);
        // Reset state after transition
        setTimeout(() => {
            state.component = null;
            state.props = {};
        }, 300);
    };

    return { state, open, close };
};
