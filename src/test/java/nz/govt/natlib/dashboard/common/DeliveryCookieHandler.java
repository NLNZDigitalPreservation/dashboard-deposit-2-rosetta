package nz.govt.natlib.dashboard.common;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeliveryCookieHandler extends CookieHandler {
    final ConcurrentHashMap<String, List<String>> cookies;

    public DeliveryCookieHandler(ConcurrentHashMap<String, List<String>> map) {
        this.cookies = map;
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        return cookies;
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {

    }
}
