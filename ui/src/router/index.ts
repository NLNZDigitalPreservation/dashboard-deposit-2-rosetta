import { useAuthStore } from '@/utils/auth';
import { useSystemInfoStore } from '@/utils/system.info.store';
import { useUserProfileStore } from '@/utils/users';
import { createRouter, createWebHistory } from 'vue-router';

export const routes = {
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            children: [
                {
                    path: '/',
                    name: 'index',
                    component: () => import('@/views/MainView.vue')
                },
                {
                    path: '/home.html',
                    name: 'home',
                    component: () => import('@/views/MainView.vue')
                }
            ]
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/'
        }
    ]
};

const router = createRouter(routes);

router.beforeEach(async (to) => {
    const systemInfoStore = useSystemInfoStore();
    const userProfileStore = useUserProfileStore();
    const authStore = useAuthStore();

    await systemInfoStore.load();
    await userProfileStore.load();

    const isAuthenticated = await authStore.isAuthenticated();
    if (!isAuthenticated) {
        await authStore.requireLogin();
    }
});

export default router;
