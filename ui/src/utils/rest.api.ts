import { useAuthStore } from '@/utils/auth';
import { extractError } from '@/utils/rest.http';
import { useUserProfileStore } from '@/utils/users';
import axios from 'axios';

const baseUrl = import.meta.env.BASE_URL;

// Move these outside the hook or use a ref if you want
// to share the queue across multiple hook calls
let isRefreshing = false;
let failedQueue = [] as Array<{ resolve: (token: string) => void; reject: () => void }>;

const processQueue = (token: any = null) => {
    failedQueue.forEach((prom) => {
        if (token) {
            prom.resolve(token);
        } else {
            prom.reject();
        }
    });
    failedQueue = [];
};

// by convention, composable function names start with "use"
export function useFetch() {
    const userStore = useUserProfileStore();
    const authStore = useAuthStore();

    const api = axios.create({
        baseURL: baseUrl,
        timeout: 60000
    });

    api.interceptors.request.use((config) => {
        const userInfo = userStore.userInfo;
        if (userInfo?.token) {
            config.headers = config.headers ?? {};
            config.headers.Authorization = userInfo.token;
        }
        return config;
    });

    api.interceptors.response.use(
        (res) => res.data,
        async (error) => {
            const originalRequest = error.config || {};

            if (error.response && error.response.status === 401 && !originalRequest._retry) {
                if (isRefreshing) {
                    // 1. If a login is already in progress, add this request to the queue
                    return new Promise((resolve, reject) => {
                        failedQueue.push({ resolve, reject });
                    })
                        .then((token) => {
                            originalRequest.headers.Authorization = token;
                            return api(originalRequest);
                        })
                        .catch((err) => {
                            return Promise.reject(err);
                        });
                }

                // 2. Mark this request as retrying and lock the refresh process
                originalRequest._retry = true;
                isRefreshing = true;

                try {
                    // 3. Trigger the login dialog
                    await authStore.requireLogin();

                    // 4. Reload store to get the new token
                    const userInfo = userStore.userInfo;
                    const newToken = userInfo?.token;

                    // 5. Release the waiting requests
                    processQueue(newToken);

                    // 6. Retry the original request that triggered the 401
                    originalRequest.headers.Authorization = newToken;
                    return api(originalRequest);
                } catch (e) {
                    processQueue(null); // Reject the queue
                    return Promise.reject(e);
                } finally {
                    isRefreshing = false;
                }
            } else {
                let err = {
                    title: 'Error',
                    description: 'Unknown error'
                };

                if (error.response) {
                    err = extractError(error.response);
                }

                console.error(err.description, err.description, err.title);

                // return Promise.reject(undefined);
                return { data: undefined };
            }
        }
    );

    return api;
}
