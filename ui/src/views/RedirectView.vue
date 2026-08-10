<script setup lang="ts">
import router from '@/router';
import { getAzureAccountInfo } from '@/utils/msal';
import { useSystemInfoStore } from '@/utils/system.info.store';
import { onMounted, ref } from 'vue';

type RedirectState = 'checking' | 'success' | 'error';

const systemInfoStore = useSystemInfoStore();

const state = ref<RedirectState>('checking');
const title = ref('Checking Entra ID sign-in');
const detail = ref('Please wait while we verify the authentication response.');

const sleep = (milliseconds: number) => new Promise((resolve) => window.setTimeout(resolve, milliseconds));

const finish = async (nextPath: string, message: string, redirectDelay = 1200) => {
    detail.value = message;
    await sleep(redirectDelay);
    await router.replace(nextPath);
};

onMounted(async () => {
	try {
		await systemInfoStore.load();

		const systemInfo = systemInfoStore.data;
		if (systemInfo.authMode !== 'entra') {
			state.value = 'error';
			title.value = 'Entra ID sign-in skipped';
			await finish('/home.html', 'This redirect page is only used when Entra ID authentication is enabled.');
			return;
		}

		const accountInfo = await getAzureAccountInfo(systemInfo.entraTenantId, systemInfo.entraClientId, systemInfo.entraRedirectUrl);

		if (!accountInfo) {
			state.value = 'error';
			title.value = 'Entra ID sign-in failed';
			await finish('/login.html', 'No authenticated Entra ID account was found. Please sign in again.');
			return;
		}

		state.value = 'success';
		title.value = 'Entra ID sign-in succeeded';
		const displayName = accountInfo.presentation_name || accountInfo.username || 'your account';
		await finish('/home.html', `Signed in successfully as ${displayName}.`);
	} catch {
		state.value = 'error';
		title.value = 'Entra ID sign-in failed';
		await finish('/login.html', 'The redirect response could not be processed. Please try signing in again.');
	}
});
</script>

<template>
	<div class="bg-surface-50 dark:bg-surface-950 flex min-h-screen min-w-[100vw] items-center justify-center overflow-hidden px-6">
		<div class="w-full max-w-xl rounded-[2rem] border border-surface-200/70 bg-surface-0 px-8 py-10 shadow-2xl dark:border-surface-800 dark:bg-surface-900 sm:px-12">
			<div class="mb-8 flex items-center gap-4">
				<div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-primary-contrast shadow-lg">
					<i v-if="state === 'checking'" class="pi pi-spin pi-spinner text-2xl"></i>
					<i v-else-if="state === 'success'" class="pi pi-check text-2xl"></i>
					<i v-else class="pi pi-exclamation-triangle text-2xl"></i>
				</div>
				<div>
					<div class="text-surface-900 text-2xl font-semibold dark:text-surface-0">{{ title }}</div>
					<div class="text-muted-color mt-1 text-sm font-medium">Verifying the Microsoft Entra ID callback</div>
				</div>
			</div>

			<div class="rounded-2xl border border-dashed border-surface-200 bg-surface-50 px-5 py-4 text-surface-700 dark:border-surface-800 dark:bg-surface-950 dark:text-surface-300">
				{{ detail }}
			</div>
		</div>
	</div>
</template>
