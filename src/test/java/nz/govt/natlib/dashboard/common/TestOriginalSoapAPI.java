package nz.govt.natlib.dashboard.common;


import com.exlibris.dps.DepositWebServices;
import com.exlibris.dps.DepositWebServices_Service;
import com.exlibris.dps.sdk.pds.HeaderHandlerResolver;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

public class TestOriginalSoapAPI {
    private static final String DEPOSIT_WSDL_URL = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/DepositWebServices?wsdl";

    @Test
    public void testDepositSoapAPI() throws MalformedURLException {
        DepositWebServices_Service depWS = new DepositWebServices_Service(new URL(DEPOSIT_WSDL_URL), new QName("http://dps.exlibris.com/", "DepositWebServices"));
        depWS.setHandlerResolver(new HeaderHandlerResolver("NLZNDashboard", "Password01", "INS00"));

        String producerId = "6355932079";
        String materialflowId = "31072513794";
        String subDirectoryName = "/d/ndha/dd";
        String depositSetId = "";

        // 5. Submit Deposit
        DepositWebServices depositWebServices = depWS.getDepositWebServicesPort();
        String retval = depositWebServices.submitDepositActivity(null, materialflowId, subDirectoryName, producerId, depositSetId);
        System.out.println("Submit Deposit Result: " + retval);
    }
}
