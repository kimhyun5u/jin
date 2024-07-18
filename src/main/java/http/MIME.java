package http;

import java.util.HashMap;
import java.util.Map;

public class MIME {
    protected static final Map<String, String> types = new HashMap<>();

    private MIME() {
    }

    public static void init() {
        types.put("jpeg", "image/jpeg");
        types.put("jpg", "image/jpg");
        types.put("png", "image/png");
        types.put("html", "text/html");
        types.put("js", "application/javascript");
        types.put("css", "text/css");
        types.put("ico", "image/x-icon");
        types.put("svg", "image/svg+xml");
    }

    public static String getMIMEType(final String ext) {
        return types.get(ext);
    }
}
