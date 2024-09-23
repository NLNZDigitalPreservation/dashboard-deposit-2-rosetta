import { useUserProfileStore } from '@/stores/users';
import { defineStore } from 'pinia';
import { useToast } from 'primevue/usetoast';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

// export const useTimeoutStore = defineStore('TimeoutStore', () => {
//     const timeoutHandler = [] as any[];
//     const sleep = (ms: number) =>
//         new Promise((r) => {
//             const handler = setTimeout(r, ms);
//             timeoutHandler.push(handler);
//             return handler;
//         });
//     const cancel = () => {
//         while (timeoutHandler.length > 0) {
//             const handler = timeoutHandler.pop();
//             clearTimeout(handler);
//             console.log('canceled a timer');
//         }
//     };
//     return { sleep, cancel };
// });

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export const useLoginStore = defineStore('LoginStore', () => {
    const toast = useToast();
    const userProfile = useUserProfileStore();
    const isAuthenticating = ref(false);
    const startLogin = () => {
        if (!isAuthenticating.value) {
            isAuthenticating.value = true;
        }
    };

    const authenticate = async (username: string, password: string) => {
        const credentials = JSON.stringify({
            username: username,
            password: password
        });
        const rsp = await fetch('/auth/login.json', {
            method: 'POST',
            redirect: 'error',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: credentials
        });

        if (!rsp.ok) {
            let status = rsp.status;
            let statusText = rsp.statusText;
            if (!statusText || statusText.length === 0) {
                if (status === 401) {
                    statusText = 'Unknown username or password, please try again.';
                } else {
                    statusText = 'Unknown error.';
                }
            }
            toast.add({ severity: 'error', summary: 'Error: ' + status, detail: statusText, life: 30000 });
            return;
        }

        const token = await rsp.json();
        userProfile.setToken(username, token);

        isAuthenticating.value = false;
    };

    const visibleLoginWindow = computed(() => {
        return isAuthenticating.value;
    });

    return { startLogin, authenticate, visibleLoginWindow, isAuthenticating };
});

export interface UseFetchApis {
    // methods
    get: (path: string) => any;
    post: (path: string, payload: any) => any;
    put: (path: string, payload: any) => any;
    delete: (path: string, payload: any) => any;
    patch: (path: string, payload: any) => any;
    head: (path: string) => any;
    options: (path: string, payload: any) => any;
}

// by convention, composable function names start with "use"
export function useFetch() {
    // state encapsulated and managed by the composable
    // const dialog = useDialog();
    const toast = useToast();

    const shell: UseFetchApis = {
        // method
        get: setMethod('GET'),
        put: setMethod('PUT'),
        post: setMethod('POST'),
        delete: setMethod('DELETE'),
        patch: setMethod('PATCH'),
        head: setMethod('HEAD'),
        options: setMethod('OPTIONS')
    };

    function setMethod(methodValue: HttpMethod) {
        return async (path: string, payload: any = null) => {
            const loginStore = useLoginStore();
            const userProfile = useUserProfileStore();
            const router = useRouter();

            let ret = null;

            const isFinished = ref(false);

            //Retry until it's finished. If the login session is expired, it can be run 2 rounds
            while (!isFinished.value) {
                // Waiting until the authentication is finished
                while (loginStore.isAuthenticating) {
                    await sleep(1000);
                }

                const reqOptions: RequestInit = {
                    method: methodValue,
                    credentials: 'same-origin',
                    redirect: 'error',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: userProfile.token
                    }
                };
                if (payload) {
                    reqOptions.body = JSON.stringify(payload);
                }

                const rsp = await fetch('/deposit-dashboard' + path, reqOptions);
                if (rsp.status === 401) {
                    loginStore.startLogin();
                    continue;
                }

                if (rsp.ok) {
                    if (path.includes('export-data')) {
                        ret = await rsp.blob();
                    } else {
                        ret = await rsp.json();
                    }
                } else {
                    let error = await rsp.text();
                    if (!error || error.length === 0) {
                        error = 'Unknown error: ' + rsp.status;
                    }
                    console.error(rsp.status + ' : ' + error);
                    toast.add({ severity: 'error', summary: 'Error: ' + rsp.status, detail: error, life: 30000 });
                    ret = error;
                }
                isFinished.value = true;
            }
            return ret;
        };
    }

    // expose managed state as return value
    return shell;
}
