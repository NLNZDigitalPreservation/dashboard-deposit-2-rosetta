package nz.govt.natlib.dashboard.rest;


import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Base64;

public class TestRestApi {
    private static final String SERVER_URL = "https://wlguatdpsilb.natlib.govt.nz";

    @Test
    public void testGetAPI() {
        String urlAddress = SERVER_URL + "/rest/v0/producers";
        BufferedReader reader = null;
        try {
            URL url = new URL(urlAddress);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setRequestProperty("Authorization", "Basic " + encodeCredential());
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            String response = out.toString();
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String encodeCredential() {
//        byte[] credential = "UtainaDeposit-institutionCode-INS00:Password01".getBytes();
        byte[] credential = "serverside-institutionCode-INS00:ServerSide1234".getBytes();
        String encodedCredential = Base64.getEncoder().encodeToString(credential);
        System.out.println(encodedCredential);
        return encodedCredential;
    }


}
