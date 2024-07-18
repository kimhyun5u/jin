package http;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {
    private static final int MAX_REQUEST_SIZE = 1024 * 10; // 1MB
    private final String method;
    private final String version;
    private final String path;
    private final Map<String, String> query;
    private final String body;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    private final Map<String, Object> multipartFile;

    public HttpRequest(String method, String version, String body, String path, Map<String, String> query, Map<String, String> headers, Map<String, String> cookies, Map<String, Object> multipartFile) {
        this.method = method;
        this.version = version;
        this.body = body;
        this.path = path;
        this.query = query;
        this.headers = headers;
        this.cookies = cookies;
        this.multipartFile = multipartFile;
    }

    public static HttpRequest from(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[8196];
        int bytesRead;
        int contentLengthPos = -1;
        int contentLength = -1;
        int headerEnd = -1;
        while ((bytesRead = bis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);

            if ((contentLengthPos = findSequence(baos.toByteArray(), "Content-Length:".getBytes(), 0)) != -1) {
                int contentLengthLength = findSequence(baos.toByteArray(), "\r\n".getBytes(), contentLengthPos);
                if (contentLengthLength != -1) {
                    contentLength = Integer.parseInt(new String(Arrays.copyOfRange(baos.toByteArray(), contentLengthPos + "Content-Length:".length(), contentLengthLength)).trim());
                }
            }
            // baos 에 \r\n\r\n이 없으면 계속 읽기
            if ((headerEnd = findSequence(baos.toByteArray(), "\r\n\r\n".getBytes(), 0)) != -1) {
                if (contentLength != -1) {
                    int bodyStart = headerEnd + 4;
                    int bodyEnd = bodyStart + contentLength;
                    if (bodyEnd <= baos.size()) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        byte[] rawData = baos.toByteArray();

        // 헤더와 바디 구분점 계산
        headerEnd = findSequence(rawData, "\r\n\r\n".getBytes(), 0);
        if (headerEnd == -1) {
            throw new IOException("Invalid HTTP request");
        }

        // 헤더 파싱
        String headerStr = new String(rawData, 0, headerEnd, "UTF-8");
        String[] headerLines = headerStr.split("\r\n");

        // 요청 라인 파싱
        String[] requestLines = headerLines[0].split(" ");
        String method = requestLines[0];
        String target = URLDecoder.decode(requestLines[1], "UTF-8");

        // 쿼리 파싱
        Map<String, String> query = new HashMap<>();
        if (target.contains("?")) {
            String[] urlParts = target.split("\\?", 2);
            target = urlParts[0];
            parseQueryString(urlParts[1].getBytes(), query);
        }

        // 헤더 파싱
        Map<String, String> headers = new HashMap<>();
        Map<String, String> cookies = new HashMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            String[] header = headerLines[i].split(":", 2);
            if (header.length == 2) {
                headers.put(header[0].trim(), header[1].trim());
            }
            if ("cookie".equalsIgnoreCase(header[0])) {
                String[] cookieParts = header[1].split(";");
                for (String cookiePart : cookieParts) {
                    String[] keyValue = cookiePart.split("=", 2);
                    if (keyValue.length == 2) {
                        cookies.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
        }

        // 바디 파싱
        byte[] bodyData = Arrays.copyOfRange(rawData, headerEnd + 4, rawData.length);
        String body = "";

        Map<String, Object> multipartData = new HashMap<>();

        // POST 요청 처리
        if ("POST".equalsIgnoreCase(method)) {
            String contentType = headers.getOrDefault("Content-Type", "");
            if (contentType.contains("application/x-www-form-urlencoded")) {
                parseQueryString(bodyData, query);
            }
            // 다른 Content-Type (예: application/json)에 대한 처리는 여기에 추가할 수 있습니다.
            else if (contentType.contains("multipart/form-data")) {
                // 멀티파트 폼 데이터 처리
                String boundary = contentType.split("boundary=")[1];
                multipartData = parseMultipartFormData(bodyData, boundary);
            } else {
                body = new String(bodyData, "UTF-8");
            }
        }

        return new HttpRequest(method, requestLines[2], body, target, query, headers, cookies, multipartData);
    }

    private static int findSequence(byte[] data, byte[] sequence, int start) {
        for (int i = start; i <= data.length - sequence.length; i++) {
            boolean found = true;
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    private static Map<String, Object> parseMultipartFormData(byte[] bodyData, String boundary) throws IOException {
        Map<String, Object> result = new HashMap<>();
        byte[] boundaryBytes = ("\r\n--" + boundary).getBytes("UTF-8");
        int start = 0;

        while (start < bodyData.length) {
            int end = findSequence(bodyData, boundaryBytes, start);
            if (end == -1) {
                break;
            }

            byte[] part = Arrays.copyOfRange(bodyData, start, end);
            processMultipartPart(part, result);

            start = end + boundaryBytes.length;
        }

        return result;
    }

    private static void processMultipartPart(byte[] part, Map<String, Object> result) throws IOException {
        int headerEnd = findSequence(part, "\r\n\r\n".getBytes(), 0);
        if (headerEnd == -1) {
            return;
        }

        String headerStr = new String(part, 0, headerEnd, "UTF-8");
        String[] headerLines = headerStr.split("\r\n");

        String[] disposition = headerLines[1].split(";");
        String name = null;
        String filename = null;
        for (String partDisposition : disposition) {
            String[] keyValue = partDisposition.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("name".equalsIgnoreCase(key)) {
                    name = value.substring(1, value.length() - 1);
                } else if ("filename".equalsIgnoreCase(key)) {
                    filename = value.substring(1, value.length() - 1);
                }
            }
        }

        if (name == null) {
            return;
        }

        byte[] value = Arrays.copyOfRange(part, headerEnd + 4, part.length);
        if (filename != null) {
            result.put(name, new File(filename, value));
        } else {
            result.put(name, new String(value, 0, value.length, "UTF-8"));
        }
    }

    private static void parseQueryString(byte[] bodyData, Map<String, String> query) throws UnsupportedEncodingException {
        String queryString = new String(bodyData);
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                query.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
            } else if (keyValue.length == 1) {
                query.put(keyValue[0], "");
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(headers.get(key));
    }

    public String getRequestLine() {
        return method + " " + path + " " + version;
    }

    public String getQuery(String key) {
        return query.get(key);
    }

    public String getVersion() {
        return version;
    }

    public String getBody() {
        return body;
    }

    public Object getMultipartFile(String key) {
        return multipartFile.get(key);
    }

    public Optional<String> getCookie(String key) {
        return Optional.ofNullable(cookies.get(key));
    }
}
