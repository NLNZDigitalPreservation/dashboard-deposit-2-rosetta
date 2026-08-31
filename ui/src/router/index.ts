import { useAuthStore } from '@/utils/auth';
import { useMsalStore } from '@/utils/msal';
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
                    path: '/redirect.html',
                    name: 'redirect',
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
    const publicRoutes = ['/login.html'];
    if (publicRoutes.includes(to.path)) {
        return true; // Allow navigation to public routes
    }
    if (to.path === '/redirect.html') {
        const msalStore = useMsalStore();
        await msalStore.login();
        // Handle redirect promise to set the active account after login
        await msalStore.handleRedirectPromise();
    } else {
        const authStore = useAuthStore();
        const isLogin = await authStore.isLogin();
        if (!isLogin) {
            await authStore.requireLogin(true);
        }
    }
});

export default router;
