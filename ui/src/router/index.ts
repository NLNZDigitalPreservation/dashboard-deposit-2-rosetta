import { createRouter, createWebHistory } from 'vue-router';

export const routes = {
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            children: [
                {
                    path: '',
                    name: 'home',
                    component: () => import('@/views/DepositJobListView.vue')
                },
                {
                    path: '/setting/deposit-account',
                    name: 'deposit-account',
                    component: () => import('@/views/SettingDepositAccountView.vue')
                }
            ]
        }
    ]
};

const router = createRouter(routes);

export default router;
