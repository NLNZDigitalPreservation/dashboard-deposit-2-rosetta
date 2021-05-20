package nz.govt.natlib.dashboard.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class DashboardConstants {
    public static final String PATH_ROOT = "/restful";

    public static final String PATH_RAW_MATERIAL_FLOW = PATH_ROOT + "/producer/material-flows";

    public static final String PATH_SETTING_FLOW = PATH_ROOT + "/setting/flow";
    public static final String PATH_SETTING_FLOW_ALL_GET = PATH_SETTING_FLOW + "/all/get";
    public static final String PATH_SETTING_FLOW_DETAIL = PATH_SETTING_FLOW + "/detail";
    public static final String PATH_SETTING_FLOW_SAVE = PATH_SETTING_FLOW + "/save";
    public static final String PATH_SETTING_FLOW_DELETE = PATH_SETTING_FLOW + "/delete";

    public static final String PATH_SETTING_GLOBAL = PATH_ROOT + "/setting/global";
    public static final String PATH_SETTING_GLOBAL_INITIAL = PATH_SETTING_GLOBAL + "/initial";
    public static final String PATH_SETTING_GLOBAL_SAVE = PATH_SETTING_GLOBAL + "/save";
    public static final String PATH_SETTING_GLOBAL_GET = PATH_SETTING_GLOBAL + "/get";
    public static final String PATH_SETTING_GLOBAL_WHITE_USER_SAVE = PATH_SETTING_GLOBAL + "/white/user/save";
    public static final String PATH_SETTING_GLOBAL_WHITE_USER_DELETE = PATH_SETTING_GLOBAL + "/white/user/delete";

    public static final String PATH_DEPOSIT_JOBS = PATH_ROOT + "/deposit-jobs";
    public static final String PATH_DEPOSIT_JOBS_ACTIVE_GET = PATH_DEPOSIT_JOBS + "/active/get";
    public static final String PATH_DEPOSIT_JOBS_ALL_GET = PATH_DEPOSIT_JOBS + "/all/get";
    public static final String PATH_DEPOSIT_JOBS_DETAIL = PATH_DEPOSIT_JOBS + "/details";
    public static final String PATH_DEPOSIT_JOBS_UPDATE = PATH_DEPOSIT_JOBS + "/update";
    public static final String PATH_DEPOSIT_JOBS_NEW = PATH_DEPOSIT_JOBS + "/new";
    public static final String PATH_DEPOSIT_JOBS_SEARCH = PATH_DEPOSIT_JOBS + "/search";

    public static final String PATH_SYSTEM_EVENT = PATH_ROOT + "/system-events";
    public static final String PATH_SYSTEM_EVENT_ALL = PATH_SYSTEM_EVENT + "/all/get";
    public static final String PATH_SYSTEM_EVENT_ACTION = PATH_SYSTEM_EVENT + "/selected/action";

    public static final String PATH_ROOT_AUTH = "/auth";
    public static final String PATH_USER_LOGIN = PATH_ROOT_AUTH + "/login.html";
    public static final String PATH_USER_LOGOUT = PATH_ROOT_AUTH + "/logout.html";
    public static final String PATH_USER_HTML_PASSWORD = PATH_ROOT_AUTH + "password.html";
    public static final String PATH_USER_CHANGE_PASSWORD = PATH_ROOT_AUTH + "password/save";

    public static final String KEY_PDS_HANDLE = "PDS_HANDLE";
    public static final String KEY_USER_INFO = "USER_INFO";

    public static final Map<String, String> MAP_ALL_CONSTANTS = new HashMap<>();

    static {
        Field[] fields = DashboardConstants.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && f.getName().startsWith("PATH")) {
                try {
                    MAP_ALL_CONSTANTS.put(f.getName(), (String)f.get(DashboardConstants.class));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
