package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

class HttpResponseTest {

    private static final int ITERATIONS = 100000;
    private static final String TEST_DATA = "Hello, World! This is a test of IO performance.";

    @Test
    @DisplayName("BufferedOutputStream을 사용한 경우와 사용하지 않은 경우의 성능 비교")
    void testBufferedIOStreamPerformance() throws IOException {
        // 1. BufferedOutputStream을 사용한 경우
        HttpResponse bufferedResponse = new HttpResponse(new ByteArrayOutputStream());
        setResponse(bufferedResponse);
        long bufferedStart = System.nanoTime();

        String statusLine = String.format("%s %d %s", "HTTP 1.1", 201, "Created");
        BufferedOutputStream os = new BufferedOutputStream(bufferedResponse.getOutputStream());
        os.write(statusLine.getBytes());

        for (Map.Entry<String, String> header : bufferedResponse.getHeaders().entrySet()) {
            String headerLine = header.getKey() + ": " + header.getValue() + " \r\n";
            os.write(headerLine.getBytes());
        }

        os.write("\r\n".getBytes());

        if (bufferedResponse.getBody() != null) {
            os.write(bufferedResponse.getBody());
        }

        os.flush();

        long bufferedEnd = System.nanoTime();
        long bufferedDuration = bufferedEnd - bufferedStart;
//        같은 테스트에서 실행하면 뒤에 실행되는 response 가 무조건 빠름 JVM cache 의 원인으로 파악됨
//        // 2. BufferedOutputStream을 사용하지 않은 경우
//        HttpResponse response = new HttpResponse(new ByteArrayOutputStream());
//        setResponse(response);
//        long unbufferedStart = System.nanoTime();
//
//        String statusLine2 = String.format("%s %d %s", "HTTP 1.1", 201, "Created");
//        OutputStream os2 = response.getOutputStream();
//        os2.write(statusLine2.getBytes());
//
//        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
//            String headerLine = header.getKey() + ": " + header.getValue() + " \r\n";
//            os2.write(headerLine.getBytes());
//        }
//
//        os2.write("\r\n".getBytes());
//
//        if (response.getBody() != null) {
//            os2.write(response.getBody());
//        }
//
//        os2.flush();
//
//        long unbufferedEnd = System.nanoTime();
//        long unbufferedDuration = unbufferedEnd - unbufferedStart;

        // 결과 출력
        System.out.println("Buffered Duration: " + bufferedDuration / 100000 + "  ms");
//        System.out.println("Unbuffered Duration: " + unbufferedDuration + " ns");
//
//        // 버퍼를 사용한 경우가 더 빠른지 확인
//        assertTrue(bufferedDuration < unbufferedDuration,
//                "Buffered output should be faster than unbuffered output");
    }

    @Test
    void testIOStreamPerformance() throws IOException {
        // 2. BufferedOutputStream을 사용하지 않은 경우
        HttpResponse response = new HttpResponse(new ByteArrayOutputStream());
        setResponse(response);
        long unbufferedStart = System.nanoTime();

        String statusLine2 = String.format("%s %d %s", "HTTP 1.1", 201, "Created");
        OutputStream os2 = response.getOutputStream();
        os2.write(statusLine2.getBytes());

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            String headerLine = header.getKey() + ": " + header.getValue() + " \r\n";
            os2.write(headerLine.getBytes());
        }

        os2.write("\r\n".getBytes());

        if (response.getBody() != null) {
            os2.write(response.getBody());
        }

        os2.flush();

        long unbufferedEnd = System.nanoTime();
        long unbufferedDuration = unbufferedEnd - unbufferedStart;
        System.out.println("Unbuffered Duration: " + unbufferedDuration / 100000 + " ms");
    }

    void setResponse(HttpResponse response) {
        // 상태 코드 설정 (사용자 생성 성공을 가정)
        response.setStatus(HttpStatus.CREATED);

        // 헤더 설정
        response.addHeader("Content-Type", "application/json");
        response.addHeader("Location", "http://localhost:8080/users/1"); // 새로 생성된 리소스의 위치
        response.addHeader("Connection", "keep-alive");

        // CORS 헤더 (필요한 경우)
        response.addHeader("Access-Control-Allow-Origin", "http://localhost:8080");

        // 보안 헤더
        response.addHeader("X-Content-Type-Options", "nosniff");
        response.addHeader("X-Frame-Options", "DENY");
        response.addHeader("X-XSS-Protection", "1; mode=block");

        // 응답 본문 설정
        String responseBody = "{\"status\":\"success\",\"message\":\"User created successfully\",\"userId\":\"1\"}";
        response.setBody(responseBody.getBytes());

        // Content-Length 헤더 설정
        response.addHeader("Content-Length", String.valueOf(responseBody.getBytes().length));
    }
}
