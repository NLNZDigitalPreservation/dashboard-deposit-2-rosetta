package nz.govt.natlib.dashboard.rosetta;


import com.exlibris.core.sdk.utils.FSDepositAreaUtil;
import com.exlibris.digitool.exceptions.DigitoolException;
import com.exlibris.dps.deposit.acquiring.AcquiringManager;
import com.exlibris.dps.deposit.acquiring.DPSNFSAdapter;
import com.exlibris.dps.deposit.reporters.ConsoleReporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;

public class TestOriginalDeposit {
    @Test
    public void testOriginalDeposit() throws Exception {
        DPSNFSAdapter nfs = new DPSNFSAdapter();

        File directory = new File("/home/leefr/diagnose/rosetta/R18339571");
        File[] files = directory.listFiles();
        assert files != null;

        HashMap<String, String> params = new HashMap<>();
        params.put(AcquiringManager.USER_NAME_PARAM, "serverside");
        params.put(AcquiringManager.SIP_ID_PARAM, "1990933");
        params.put(AcquiringManager.PRODUCER_GROUP_PARAM, "33905899492");
        params.put(AcquiringManager.PRODUCER_PROFILE_PARAM, "33905845645");
        params.put("directory", directory.getAbsolutePath());
        params.put(AcquiringManager.CONTENT_STRUCTURE_TYPE, "DC");
        params.put("copy_method", "copy");

        ConsoleReporter reporter = new ConsoleReporter();
        FSDepositAreaUtil util = new FSDepositAreaUtil("/tmp");

        nfs.init(params, reporter, 20L, util);
        nfs.acquire();
    }
}
