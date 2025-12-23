package nz.govt.natlib.dashboard.util;

import nz.govt.natlib.dashboard.common.exception.NullParameterException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class DashboardHelper {
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static void assertNotNull(String sName, String sValue) throws NullParameterException {
        if (sValue == null || sValue.trim().isEmpty()) {
            throw new NullParameterException(sName + " can not be empty.");
        }
    }

    public static void assertNotNull(String oName, Object oValue) throws NullParameterException {
        if (oValue == null) {
            throw new NullParameterException(oName + " can not be empty.");
        }
    }

//    public static long localCurrentEpochSecond() {
//        return LocalDateTime.now().toEpochSecond(localZoneOffset());
//    }

    public static long getLocalMilliSeconds(LocalDateTime ldt) {
        return ldt.toInstant(localZoneOffset()).toEpochMilli();
    }

    public static long getLocalCurrentMilliSeconds() {
        return getLocalMilliSeconds(LocalDateTime.now());
    }

    public static LocalDateTime trim2Hour(LocalDateTime ldt) {
        ldt = ldt.withMinute(0);
        ldt = ldt.withSecond(0);
        ldt = ldt.withNano(0);
        return ldt;
    }

    public static ZoneOffset localZoneOffset() {
//        return ZoneOffset.of(TimeZone.getDefault().getID());
        return OffsetDateTime.now().getOffset();
    }

    public static LocalDateTime getLocalDateTimeFromEpochMilliSecond(Long epochMilliSecond) {
        if (isNull(epochMilliSecond)) {
            epochMilliSecond = getLocalCurrentMilliSeconds();
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilliSecond), localZoneOffset());
    }

    public static String epochMilliSecondToFrontendReadableLocalTime(Long epochMilliSecond) {
        if (isNull(epochMilliSecond)) {
            return "";
        }
        LocalDateTime ldt = getLocalDateTimeFromEpochMilliSecond(epochMilliSecond);
        String rst = String.format("%02d/%02d/%04d,%02d:%02d:%02d", ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear(), ldt.getHour(), ldt.getMinute(), ldt.getSecond());
        return rst;
    }

    public static String getUid() {
        return UUID.randomUUID().toString();
    }
}
