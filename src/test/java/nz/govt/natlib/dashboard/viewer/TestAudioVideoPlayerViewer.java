package nz.govt.natlib.dashboard.viewer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAudioVideoPlayerViewer {
    @BeforeAll
    static void init() throws Exception {
//        String proxyHost = "192.168.1.65";
//        String proxyPort = "3082";
//
//        System.setProperty("http.proxyHost", proxyHost);
//        System.setProperty("http.proxyPort", proxyPort);
//        System.setProperty("https.proxyHost", proxyHost);
//        System.setProperty("https.proxyPort", proxyPort);
    }

    @Test
    public void testDoGet() throws InterruptedException {
        AudioVideoPlayerViewer viewer = new AudioVideoPlayerViewer();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse rsp = mock(HttpServletResponse.class);
        when(req.getParameter("dps_pid")).thenReturn("IE40340534");
        when(req.getParameter("dps_dvs")).thenReturn("1601953196138~807");
        Cookie[] reqCookies = new Cookie[0];
        when(req.getCookies()).thenReturn(reqCookies);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(req.getHeader("X-Forwarded-For")).thenReturn(null);
        when(req.getRemoteAddr()).thenReturn("localhost");

        List<String> results = new ArrayList<>();
        String[] wsdl_urls = {"https://wlguatdpsilb.natlib.govt.nz/dpsws/delivery/DeliveryAccessWS?wsdl",
                "http://wlguatrosiapp01.natlib.govt.nz:1801/dpsws/delivery/DeliveryAccessWS?wsdl",
                "http://wlguatrosiapp02.natlib.govt.nz:1801/dpsws/delivery/DeliveryAccessWS?wsdl"};

//        ,
//        "http://wlguatrosiapp01.natlib.govt.nz:1801/dpsws/delivery/DeliveryAccessWS?wsdl",
//                "http://wlguatrosiapp02.natlib.govt.nz:1801/dpsws/delivery/DeliveryAccessWS?wsdl"
        for (String wsdl : wsdl_urls) {
            for (int i = 0; i < 4; i++) {
                viewer.setDELIVERY_WS_WSDL_URL(wsdl);
                String line = wsdl;
                try {
                    viewer.doGet(req, rsp);
                    line += ": Succeed";
                } catch (ServletException | IOException e) {
                    line += ": Failed";
                }
                System.out.println(line);
                results.add(line);
                TimeUnit.SECONDS.sleep(3);
            }
        }
        System.out.println("---------------------------------");
        results.forEach(System.out::println);
        assert true;
    }

}
