package nz.govt.natlib.dashboard.common.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

public class RosettaRestApi {
    private static final Logger log = LoggerFactory.getLogger(RosettaRestApi.class);

    private final static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }
            }
    };


    private final SSLContext sslContext;

    private final String rosettaRestApiUrl;
    private String cookie = "";

    static {
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
    }


    public RosettaRestApi(String rosettaRestApiUrl) {
        this.rosettaRestApiUrl = rosettaRestApiUrl;
        try {
            this.sslContext = SSLContext.getInstance("TLS");
            this.sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
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

//        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> rsp;
        HttpClient client = HttpClient.newBuilder().sslContext(this.sslContext).build();
        HttpRequest req = HttpRequest.newBuilder().method(method, HttpRequest.BodyPublishers.ofString(json)).uri(new URI(this.rosettaRestApiUrl + path)).header("Authorization", this.getBasicAuthenticationHeader(depositAccount)).header("Accept", "application/json").header("Content-Type", "application/json").header("Cookie", this.cookie).build();
        rsp = client.send(req, HttpResponse.BodyHandlers.ofString());

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
