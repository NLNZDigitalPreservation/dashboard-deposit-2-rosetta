import { routes } from '@/router';
import { defineStore } from 'pinia';
import { useToast } from 'primevue/usetoast';
import * as XLSX from 'xlsx';

export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export const formatDate = (timestamp: number) => {
    const value = new Date(timestamp);
    return value.toLocaleDateString(undefined, {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};

export const formatDatetime = (timestamp: number) => {
    const value = new Date(timestamp);
    return value.toLocaleString();
};

export const filterRoutePathByName = (routeName: string) => {
    let path = routes.routes[0].path;
    for (let i = 0; i < routes.routes[0].children.length; i++) {
        const r = routes.routes[0].children[i];
        if (r.name === routeName) {
            return path + '/' + r.path;
        }
    }
    return undefined;
};

export const getRouteURLByName = (routeName: string, params: any = undefined) => {
    let path = filterRoutePathByName(routeName);
    if (!path) {
        return '/';
    }

    if (!params) {
        return path;
    }

    for (const key in params) {
        const value = params[key];
        path = path.replace(':' + key, value);
    }
    return path;
};

export const getRootUrl = () => {
    const { origin, pathname } = window.location;
    const appPath = import.meta.env.BASE_URL;
    return `${origin}${appPath}`;
};

export const camelCaseToTitleCase = (s: string) => {
    const result = s.replace(/([A-Z])/g, ' $1');
    return result.charAt(0).toUpperCase() + result.slice(1);
};

const K = 1024,
    M = K * 1024,
    G = M * 1024;
export const formatContentLength = (l: number) => {
    if (l > G) {
        return Math.round(l / G) + 'GB';
    } else if (l > M) {
        return Math.round(l / M) + 'MB';
    } else if (l > K) {
        return Math.round(l / K) + 'KB';
    } else {
        return l + 'B';
    }
};

export const dateTimeString2Object = (strDateTime: string) => {
    const d = new Date(strDateTime);
    return d;
};

export const padZero = (num: number | string, places: number) => {
    return String(num).padStart(places, '0');
};
export const dataTimeObject2String = (objDateTime: Date) => {
    const year = objDateTime.getFullYear();
    const month = padZero(objDateTime.getMonth() + 1, 2);
    const date = padZero(objDateTime.getDate(), 2);
    const hours = padZero(objDateTime.getHours(), 2);
    const minutes = padZero(objDateTime.getMinutes(), 2);
    const seconds = padZero(objDateTime.getSeconds(), 2);

    const ret = year + '-' + month + '-' + date + 'T' + hours + ':' + minutes + ':' + seconds;
    return ret;
};

export const useToastStore = defineStore('ToastStore', () => {
    const _toast = useToast();
    const success = (msg: string) => {
        _toast.add({ severity: 'success', summary: 'Success', detail: msg, life: 5000 });
    };
    const error = (msg: string) => {
        console.error(msg);
        _toast.add({ severity: 'error', summary: 'Error', detail: msg, life: 15000 });
    };
    const warn = (msg: string) => {
        console.warn(msg);
        _toast.add({ severity: 'warn', summary: 'Warning', detail: msg, life: 10000 });
    };
    const info = (msg: string) => {
        _toast.add({ severity: 'info', summary: 'Info', detail: msg, life: 3000 });
    };
    return { success, error, warn, info };
});

export const isNull = (obj: any) => {
    if (obj === undefined || obj === null) {
        return true;
    } else {
        return false;
    }
};

export const isEmpty = (obj: any) => {
    if (obj === undefined || obj === null) {
        return true;
    }
    if (obj.length == 0) {
        return true;
    }
    return false;
};

export const saveExcel = (data: any[], sheetName: string, customizedHeaders: any = undefined) => {
    const safeData = Array.isArray(data) ? data : [];
    const normalizedCustomHeaders = Array.isArray(customizedHeaders) ? customizedHeaders.map((header) => String(header)) : [];
    const headerSet = new Set<string>(normalizedCustomHeaders);
    if (isNull(customizedHeaders)) {
        for (const row of safeData) {
            if (!row || typeof row !== 'object') {
                continue;
            }
            for (const key of Object.keys(row)) {
                headerSet.add(key);
            }
        }
    }

    const headers = Array.from(headerSet);
    const worksheet = XLSX.utils.json_to_sheet(safeData, {
        header: headers,
        skipHeader: false
    });

    // Auto-size columns using the longest header/value length per column.
    if (headers.length > 0) {
        worksheet['!cols'] = headers.map((header) => {
            let maxLength = String(header).length;

            for (const row of safeData) {
                const value = row?.[String(header)];
                const text = isNull(value) ? '' : String(value);
                if (text.length > maxLength) {
                    maxLength = text.length;
                }
            }

            return { wch: Math.min(Math.max(maxLength + 2, 10), 80) };
        });
    }

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);

    const timeString = dataTimeObject2String(new Date());
    XLSX.writeFile(workbook, `${sheetName}-${timeString}.xlsx`);
};
