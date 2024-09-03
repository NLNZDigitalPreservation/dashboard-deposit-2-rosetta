package nz.govt.natlib.dashboard.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class DashboardConstants {
    public static final String PATH_ROOT = "/restful";

    public static final String PATH_RAW_PRODUCER_MATERIAL_FLOW = PATH_ROOT + "/raw/producer-materialflows";
    public static final String PATH_RAW_PRODUCERS = PATH_ROOT + "/raw/producers";
    public static final String PATH_RAW_PRODUCER_PROFILE = PATH_ROOT + "/raw/producer-profile";
    public static final String PATH_RAW_MATERIAL_FLOWS = PATH_ROOT + "/raw/materialflows";

    public static final String PATH_SETTING_DEPOSIT_ACCOUNT = PATH_ROOT + "/setting/deposit-account";
    public static final String PATH_SETTING_DEPOSIT_ACCOUNT_ALL_GET = PATH_SETTING_DEPOSIT_ACCOUNT + "/all/get";
    public static final String PATH_SETTING_DEPOSIT_ACCOUNT_DETAIL = PATH_SETTING_DEPOSIT_ACCOUNT + "/detail";
    public static final String PATH_SETTING_DEPOSIT_ACCOUNT_SAVE = PATH_SETTING_DEPOSIT_ACCOUNT + "/save";
    public static final String PATH_SETTING_DEPOSIT_ACCOUNT_DELETE = PATH_SETTING_DEPOSIT_ACCOUNT + "/delete";

    public static final String PATH_SETTING_DEPOSIT_ACCOUNT_REFRESH = PATH_SETTING_DEPOSIT_ACCOUNT + "/refresh";

    public static final String PATH_SETTING_FLOW = PATH_ROOT + "/setting/flow";
    public static final String PATH_SETTING_FLOW_ALL_GET = PATH_SETTING_FLOW + "/all/get";
    public static final String PATH_SETTING_FLOW_DETAIL = PATH_SETTING_FLOW + "/detail";
    public static final String PATH_SETTING_FLOW_SAVE = PATH_SETTING_FLOW + "/save";
    public static final String PATH_SETTING_FLOW_DELETE = PATH_SETTING_FLOW + "/delete";

    public static final String PATH_SETTING_WHITELIST = PATH_ROOT + "/setting/whitelist";
    public static final String PATH_SETTING_WHITELIST_ALL_GET = PATH_SETTING_WHITELIST + "/all/get";
    public static final String PATH_SETTING_WHITELIST_DETAIL = PATH_SETTING_WHITELIST + "/detail";
    public static final String PATH_SETTING_WHITELIST_SAVE = PATH_SETTING_WHITELIST + "/save";
    public static final String PATH_SETTING_WHITELIST_DELETE = PATH_SETTING_WHITELIST + "/delete";

    public static final String PATH_SETTING_GLOBAL = PATH_ROOT + "/setting/global";
    public static final String PATH_SETTING_GLOBAL_INITIAL = PATH_SETTING_GLOBAL + "/initial";
    public static final String PATH_SETTING_GLOBAL_SAVE = PATH_SETTING_GLOBAL + "/save";
    public static final String PATH_SETTING_GLOBAL_GET = PATH_SETTING_GLOBAL + "/get";

    public static final String PATH_DEPOSIT_JOBS = PATH_ROOT + "/deposit-jobs";
    public static final String PATH_DEPOSIT_JOBS_ACTIVE_GET = PATH_DEPOSIT_JOBS + "/active/get";
    public static final String PATH_DEPOSIT_JOBS_ALL_GET = PATH_DEPOSIT_JOBS + "/all/get";
    public static final String PATH_DEPOSIT_JOBS_DETAIL = PATH_DEPOSIT_JOBS + "/details";
    public static final String PATH_DEPOSIT_JOBS_UPDATE = PATH_DEPOSIT_JOBS + "/update";
    public static final String PATH_DEPOSIT_JOBS_NEW = PATH_DEPOSIT_JOBS + "/new";
    public static final String PATH_DEPOSIT_JOBS_SEARCH = PATH_DEPOSIT_JOBS + "/search";
    public static final String PATH_DEPOSIT_JOBS_EXPORT_DATA = PATH_DEPOSIT_JOBS + "/export-data";
    public static final String PATH_JOBS_ACTIVE_LIST = PATH_DEPOSIT_JOBS + "/jobs/active/list";

    public static final String PATH_DEPOSIT_JOBS_REDEPOSIT = PATH_DEPOSIT_JOBS + "/redeposit";

    public static final String PATH_SYSTEM_EVENT = PATH_ROOT + "/system-events";
    public static final String PATH_SYSTEM_EVENT_ALL = PATH_SYSTEM_EVENT + "/all/get";
    public static final String PATH_SYSTEM_EVENT_ACTION = PATH_SYSTEM_EVENT + "/selected/action";


    //Will bypass the authentication filter
    public static final String PATH_ROOT_AUTH = "/auth";
    public static final String PATH_USER_LOGIN_API = PATH_ROOT_AUTH + "/login.json";
    public static final String PATH_USER_LOGOUT_API = PATH_ROOT_AUTH + "/logout.json";
    public static final String PATH_USER_LOGIN_HTML = "/login.html";
    public static final String PATH_USER_INDEX_HTML = "/index.html";

    public static final String KEY_PDS_HANDLE = "PDS_HANDLE";
    public static final String KEY_USER_INFO = "USER_INFO";

    public static final Map<String, String> MAP_ALL_CONSTANTS = new HashMap<>();

    static {
        Field[] fields = DashboardConstants.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && f.getName().startsWith("PATH")) {
                try {
                    MAP_ALL_CONSTANTS.put(f.getName(), (String) f.get(DashboardConstants.class));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
