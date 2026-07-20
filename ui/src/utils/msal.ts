import { type AccountInfo, PublicClientApplication } from '@azure/msal-browser';

const instances = new Map<string, PublicClientApplication>();

function buildKey(tenantId: string, clientId: string, redirectUrl: string): string {
    return `${tenantId}|${clientId}|${redirectUrl}`;
}

function buildMsalConfig(tenantId: string, clientId: string, redirectUrl: string) {
    return {
        auth: {
            clientId,
            authority: `https://login.microsoftonline.com/${tenantId}`,
            redirectUri: redirectUrl || window.location.origin
        },
        cache: {
            cacheLocation: 'localStorage' as const
        }
    };
}

export async function getMsalInstance(tenantId: string, clientId: string, redirectUrl: string): Promise<PublicClientApplication | null> {
    if (!clientId?.trim()) {
        return null;
    }

    const key = buildKey(tenantId, clientId, redirectUrl);

    let app = instances.get(key);

    if (app) {
        return app;
    }

    app = new PublicClientApplication(buildMsalConfig(tenantId, clientId, redirectUrl));

    await app.initialize();
    await app.handleRedirectPromise();

    let account = app.getActiveAccount();

    if (!account) {
        const accounts = app.getAllAccounts();
        if (accounts.length > 0) {
            account = accounts[0];
            app.setActiveAccount(account);
        }
    }

    instances.set(key, app);

    return app;
}

export async function getActiveAccount(tenantId: string, clientId: string, redirectUrl: string): Promise<AccountInfo | null> {
    const app = await getMsalInstance(tenantId, clientId, redirectUrl);

    if (!app) {
        return null;
    }

    let account = app.getActiveAccount();

    if (!account) {
        const accounts = app.getAllAccounts();

        if (accounts.length > 0) {
            account = accounts[0];
            app.setActiveAccount(account);
        }
    }

    return account;
}

export async function getAzureAccountInfo(tenantId: string, clientId: string, redirectUrl: string) {
    const account = await getActiveAccount(tenantId, clientId, redirectUrl);

    if (!account) {
        return null;
    }

    const claims: any = account.idTokenClaims || {};
    const username = account.username || claims.preferred_username || '';

    return {
        azureAccountId: account.homeAccountId,
        azureLocalAccountId: account.localAccountId,
        azureTenantId: account.tenantId || claims.tid || '',
        azureUsername: username,
        presentation_name: account.name || claims.name || '',
        email: claims.email || username,
        username
    };
}
