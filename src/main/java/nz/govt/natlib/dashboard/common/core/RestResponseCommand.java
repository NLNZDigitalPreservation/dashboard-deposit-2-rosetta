package nz.govt.natlib.dashboard.common.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class RestResponseCommand {
    public static final int RSP_SUCCESS = 0;
    public static final int RSP_LOGIN_ERROR = 1001;
    public static final int RSP_LOGOUT_ERROR = 1002;
    public static final int RSP_AUTH_NO_PRIVILEGE = 1003;
    public static final int RSP_AUTH_NEED_INITIAL = 1004;
    public static final int RSP_PROCESS_SET_DUPLICATED = 3000;
    public static final int RSP_PROCESS_SET_NOT_EXIST = 3001;
    public static final int RSP_USER_CONFIDENTIAL_REQUIRED = 4000;
    public static final int RSP_USER_NAME_PASSWORD_ERROR = 4001;
    public static final int RSP_USER_QUERY_ERROR = 4002;
    public static final int RSP_USER_PDS_HANDLE_REQUIRED = 4003;
    public static final int RSP_USER_OTHER_ERROR = 4009;
    public static final int RSP_DEPOSIT_QUERY_ERROR = 4010;
    public static final int RSP_NETWORK_EXCEPTION = 5000;
    public static final int RSP_INVALID_INPUT_PARAMETERS = 5001;
    public static final int RSP_SYSTEM_ERROR = 9999;


    public static final Map<Integer, String> map = new HashMap<>();

    static {
        map.put(RSP_SUCCESS, "OK");
        map.put(RSP_LOGIN_ERROR, "Login failed");
        map.put(RSP_LOGOUT_ERROR, "Logout failed");
        map.put(RSP_AUTH_NO_PRIVILEGE, "No privilege to finish this operation");
        map.put(RSP_PROCESS_SET_DUPLICATED, "Duplicated process setting.");
        map.put(RSP_PROCESS_SET_NOT_EXIST, "Process setting does not exist.");
        map.put(RSP_USER_CONFIDENTIAL_REQUIRED, "User name and password could not be empty.");
        map.put(RSP_USER_NAME_PASSWORD_ERROR, "Incorrect username or password.");
        map.put(RSP_USER_OTHER_ERROR, "Failed to login, unknown error.");
        map.put(RSP_USER_QUERY_ERROR, "Failed to query pds user info.");
        map.put(RSP_USER_PDS_HANDLE_REQUIRED, "PDS Handle could not be empty.");
        map.put(RSP_NETWORK_EXCEPTION, "Failed to connect to Rosetta");
        map.put(RSP_INVALID_INPUT_PARAMETERS, "Invalid input parameters.");
        map.put(RSP_SYSTEM_ERROR, "System error.");
    }

    public static String getRspMsg(int rspCode) {
        return map.get(rspCode);
    }

    private int rspCode = RSP_SUCCESS;
    private String rspMsg = getRspMsg(rspCode);
    private String rspBody;

    public int getRspCode() {
        return rspCode;
    }

    public void setRspCode(int rspCode) {
        this.rspCode = rspCode;
        this.rspMsg = getRspMsg(rspCode);
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public void setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
    }

    public String getRspBody() {
        return rspBody;
    }

    public void setRspBody(Object rspBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.rspBody = objectMapper.writeValueAsString(rspBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
