import { useAuthStore } from '@/utils/auth';
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
                },
                {
                    path: '/redirect.html',
                    name: 'redirect',
                    component: () => import('@/views/RedirectView.vue')
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
    const publicRoutes = ['/login.html', '/redirect.html'];
    if (publicRoutes.includes(to.path)) {
        return;
    }

    const authStore = useAuthStore();
    await authStore.tryLogin();
});

export default router;
