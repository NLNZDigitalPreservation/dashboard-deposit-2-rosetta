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
                    component: () => import('@/components/settings/DepositAccountDrawer.vue')
                    // component: () => import('@/views/SettingDepositAccountView.vue')
                },
                {
                    path: '/setting/material-flow',
                    name: 'material-flow',
                    component: () => import('@/views/SettingMaterialFlowView.vue')
                }
            ]
        }
    ]
};

const router = createRouter(routes);

export default router;
