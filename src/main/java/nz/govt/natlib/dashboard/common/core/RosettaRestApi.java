package nz.govt.natlib.dashboard.common.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

public class RosettaRestApi {
    private static final Logger log = LoggerFactory.getLogger(RosettaRestApi.class);
    private final String rosettaRestApiUrl;
    private String cookie = "";

    public RosettaRestApi(String rosettaRestApiUrl) {
        this.rosettaRestApiUrl = rosettaRestApiUrl;
    }

    private String getBasicAuthenticationHeader(EntityDepositAccountSetting depositAccount) {
        String credential = String.format("%s-institutionCode-%s:%s", depositAccount.getDepositUserName(), depositAccount.getDepositUserInstitute(), depositAccount.getDepositUserPassword());
        return "Basic " + Base64.getEncoder().encodeToString(credential.getBytes());
    }

    public String fetch(EntityDepositAccountSetting depositAccount, String method, String path, Object reqBody) throws Exception {
        String json;
        if (reqBody == null) {
            json = "{}";
        } else {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(reqBody);
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder().method(method, HttpRequest.BodyPublishers.ofString(json)).uri(new URI(this.rosettaRestApiUrl + path)).header("Authorization", this.getBasicAuthenticationHeader(depositAccount)).header("Accept", "application/json").header("Content-Type", "application/json").header("Cookie", this.cookie).build();
        HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (rsp.statusCode() != 200) {
            String err = String.format("Failed to request: %s, error: %s. Account: %s", path, rsp.body(), depositAccount);
            log.error(err);
            throw new Exception(err);
        }

        List<String> cookies = rsp.headers().allValues("Set-Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            this.cookie = String.join(";", cookies);
        }

        //Parse json
        return rsp.body();
    }
}
