package nz.govt.natlib.dashboard.util;

import org.junit.jupiter.api.Test;

public class TestDashboardHelper {
    @Test
    public void testGetCurrentHour() {
        long currentTime1 = System.currentTimeMillis();
        long currentTime2 = DashboardHelper.getLocalCurrentMilliSeconds();
        assert currentTime2 - currentTime1 < 60;
        assert currentTime2 - currentTime1 >= 0;
    }
}
