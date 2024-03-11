package nz.govt.natlib.dashboard.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestCustomizedPdsClient {
    @Test
    public void testGetHandler() throws URISyntaxException, IOException, InterruptedException {
        String reqBody = "func=login&institute=INS00&bor_id=NLZNDashboard&bor_verification=Password01";
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .uri(new URI("https://slbpdstest.natlib.govt.nz/pds"))
                .build();

        HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println(rsp.statusCode());
        System.out.println(rsp.body());
    }
}
