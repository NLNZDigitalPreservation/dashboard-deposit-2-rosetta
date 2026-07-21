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
                    name: 'root',
                    component: () => import('@/views/MainView.vue')
                },
                {
                    path: '/index.html',
                    name: 'index',
                    component: () => import('@/views/MainView.vue')
                },
                {
                    path: '/home.html',
                    name: 'home',
                    component: () => import('@/views/MainView.vue')
                },
                {
                    path: '/login.html',
                    name: 'login',
                    component: () => import('@/views/LoginView.vue')
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
    await systemInfoStore.load();

    const userProfileStore = useUserProfileStore();
    await userProfileStore.load();

    if (to.path === '/login.html') {
        return;
    }

    const authStore = useAuthStore();
    const isAuthenticated = await authStore.isAuthenticated();
    if (!isAuthenticated) {
        await authStore.logout();
    }
});

export default router;
