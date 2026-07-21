<script setup lang="ts">
import router from '@/router';
import { useAuthStore } from '@/utils/auth';
import { useUserProfileStore } from '@/utils/users';
import axios from 'axios';
import { ref } from 'vue';

const baseUrl = import.meta.env.BASE_URL;

document.body.contentEditable = 'false';
document.documentElement.contentEditable = 'false';

const userProfile = useUserProfileStore();
const authStore = useAuthStore();

const emit = defineEmits(['cancel', 'save']);

const username = ref();
const password = ref();
const userInfo = ref();
const feedback = ref({
    ok: true,
    title: '',
    detail: ''
});

const msgKey = ref(0);
const login = async () => {
    if (!username.value) {
        feedback.value.ok = false;
        feedback.value.title = 'Error';
        feedback.value.detail = 'The username can Not be empty!';
        return;
    }
    if (!password.value) {
        feedback.value.ok = false;
        feedback.value.title = 'Error';
        feedback.value.detail = 'The password can Not be empty!';
        return;
    }

    feedback.value = await _login();
    msgKey.value += 1;
    if (feedback.value.ok) {
        const path = authStore.currentUrl || '/home.html';
        router.push(path);
    }
};

const _login = async () => {
    const credentials = {
        username: username.value,
        password: password.value
    };

    const rsp = await axios.post(`${baseUrl}/auth/login.json`, credentials);

    const _feedback = {
        ok: true,
        title: '',
        detail: ''
    };

    if (!rsp.status || rsp.status !== 200) {
        const status = rsp.status;
        let statusText = rsp.statusText;
        if (!statusText || statusText.length === 0) {
            if (status === 401) {
                statusText = 'Unknown username or password, please try again.';
            } else {
                statusText = 'Unknown error.';
            }
        }
        _feedback.ok = false;
        _feedback.title = 'Error: ' + status;
        _feedback.detail = statusText;
    } else {
        userInfo.value = rsp.data;
        userProfile.update(userInfo.value);
    }

    return _feedback;
};
</script>

<template>
    <form @submit.prevent="login">
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 1px; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                        <div class="tems-center text-center mb-8">
                            <img src="@/assets/natlib-logo-red.png" width="80" style="display: inline" />
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Welcome to Deposit Dashboard!</div>
                            <span class="text-muted-color font-medium">Sign in to continue</span>
                        </div>

                        <Message v-if="!feedback.ok" severity="error" icon="pi pi-exclamation-triangle">
                            {{ feedback.title + ':' + feedback.detail }}
                        </Message>

                        <div>
                            <label for="username" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">Username</label>
                            <InputText v-model="username" id="username" name="username" type="text" placeholder="User name" class="w-full md:w-[30rem] mb-8" autocomplete="username" />
                            <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Password</label>
                            <Password v-model="password" id="password1" placeholder="Password" :toggleMask="true" class="mb-4" fluid :feedback="false" :inputProps="{ autocomplete: 'current-password' }" />
                            <Button type="submit" label="Sign In" class="mt-8 w-full" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </form>
</template>
<style></style>
