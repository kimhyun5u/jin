package http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private OutputStream outputStream;
    private String version;
    private int statusCode;
    private String statusMsg;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        this.headers = new HashMap<>();
    }

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.version = "HTTP/1.1";
        this.headers = new HashMap<>();
    }

    public HttpResponse setStatus(HttpStatus status) {
        this.statusCode = status.getCode();
        this.statusMsg = status.getMessage();
        return this;
    }

    public HttpResponse addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpResponse setVersion(String version) {
        this.version = version;
        return this;
    }

    public HttpResponse setBody(byte[] body) {
        this.body = body;
        this.headers.put("Content-Length", String.valueOf(body.length));
        return this;
    }

    public void send() throws IOException {
        String statusLine = String.format("%s %d %s\r%n", version, statusCode, statusMsg);
        BufferedOutputStream os = new BufferedOutputStream(outputStream);
        os.write(statusLine.getBytes());

        for (Map.Entry<String, String> header : headers.entrySet()) {
            String headerLine = header.getKey() + ": " + header.getValue() + " \r\n";
            os.write(headerLine.getBytes());
        }

        os.write("\r\n".getBytes());

        if (body != null) {
            os.write(body);
        }

        os.flush();
    }

    public byte[] getBody() {
        return this.body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    protected OutputStream getOutputStream() {
        return outputStream;
    }

    protected Map<String, String> getHeaders() {
        return headers;
    }
}
