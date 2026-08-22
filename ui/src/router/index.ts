import { PublicClientApplication } from '@azure/msal-browser';
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

const msalConfig = {
    auth: {
        clientId: '',
        authority: 'https://login.microsoftonline.com/',
        redirectUri: 'https://dps.uat.natlib.govt.nz/depdash/redirect.html'
    },
    cache: {
        cacheLocation: 'sessionStorage'
    }
};
const msalInstance = new PublicClientApplication(msalConfig);
await msalInstance.initialize();

router.beforeEach(async (to) => {
    // const systemInfoStore = useSystemInfoStore();
    // await systemInfoStore.load();

    // const userProfileStore = useUserProfileStore();
    // await userProfileStore.load();

    const publicRoutes = ['/login.html', '/redirect.html'];
    if (publicRoutes.includes(to.path)) {
        return;
    }

    // const authStore = useAuthStore();
    // const isAuthenticated = await authStore.isAuthenticated();
    // if (!isAuthenticated) {
    //     await authStore.logout();
    // }
    // await authStore.requireLogin();

    try {
        // Check if user is already authenticated
        const accounts = msalInstance.getAllAccounts();
        if (accounts.length > 0) {
            return; // User is logged in, proceed
        }

        await msalInstance.loginRedirect({
            scopes: ['openid', 'profile', 'email']
        });
    } catch (error) {
        console.error('Login failed:', error);
    }
});

export default router;
