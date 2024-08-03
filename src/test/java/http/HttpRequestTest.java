package http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestTest {
    InputStream is;

    static Stream<Arguments> request() {
        return Stream.of(
                Arguments.arguments("""
                        GET / HTTP/1.1
                        Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
                        Accept-Encoding: gzip, deflate, br, zstd
                        Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
                        Cache-Control: no-cache
                        Connection: keep-alive
                        Host: localhost:8080
                        Pragma: no-cache
                        Sec-Fetch-Dest: document
                        Sec-Fetch-Mode: navigate
                        Sec-Fetch-Site: none
                        Sec-Fetch-User: ?1
                        Upgrade-Insecure-Requests: 1
                        User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36
                        sec-ch-ua: "Not/A)Brand";v="8", "Chromium";v="126", "Google Chrome";v="126"
                        sec-ch-ua-mobile: ?0
                        sec-ch-ua-platform: "macOS"\r\n\r\n
                        """, """
                        GET /create?userId=javajigi&password=password&name=%EB%B0%95%EC%9E%AC%EC%84%B1&email=javajigi%40slipp.net HTTP/1.1
                        Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
                        Accept-Encoding: gzip, deflate, br, zstd
                        Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
                        Cache-Control: no-cache
                        Connection: keep-alive
                        Host: localhost:8080
                        Pragma: no-cache
                        Sec-Fetch-Dest: document
                        Sec-Fetch-Mode: navigate
                        Sec-Fetch-Site: none
                        Sec-Fetch-User: ?1
                        Upgrade-Insecure-Requests: 1
                        User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36
                        sec-ch-ua: "Not/A)Brand";v="8", "Chromium";v="126", "Google Chrome";v="126"
                        sec-ch-ua-mobile: ?0
                        sec-ch-ua-platform: "macOS"\r\n
                        """, """
                        GET /registration HTTP/1.1
                        Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
                        Accept-Encoding: gzip, deflate, br, zstd
                        Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
                        Cache-Control: no-cache
                        Connection: keep-alive
                        Host: localhost:8080
                        Pragma: no-cache
                        Sec-Fetch-Dest: document
                        Sec-Fetch-Mode: navigate
                        Sec-Fetch-Site: none
                        Sec-Fetch-User: ?1
                        Upgrade-Insecure-Requests: 1
                        User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36
                        sec-ch-ua: "Not/A)Brand";v="8", "Chromium";v="126", "Google Chrome";v="126"
                        sec-ch-ua-mobile: ?0
                        sec-ch-ua-platform: "macOS"\r\n\r\n
                        """)
        );
    }

    @ParameterizedTest
    @DisplayName("요청 메소드와 경로 확인")
    @MethodSource("request")
    void checkRequestMapping(String req) throws IOException {
        is = new ByteArrayInputStream(req.getBytes());
        HttpRequest request = HttpRequest.from(is);

        assertEquals("GET", request.getMethod());
        assertEquals("/", request.getPath());
    }

    @Test
    @DisplayName("멀티파트 요청 성능 테스트")
    @Disabled
    void performanceTest(@TempDir Path tempDir) throws IOException {
        long[] fileSizes = {1024 * 1024, 10 * 1024 * 1024, 50 * 1024 * 1024, 100 * 1024 * 1024, 1000 * 1024 * 1024};

        for (long size : fileSizes) {
            java.io.File testFile = createLargeFile(tempDir, size);
            runTest(testFile, tempDir, String.format("File size: %d MB", size / (1024 * 1024)));
        }
    }

    private void runTest(java.io.File file, Path tempDir, String testName) throws IOException {
        System.gc();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.currentTimeMillis();

        // 임시 파일에 멀티파트 요청 쓰기
        java.io.File tempRequestFile = tempDir.resolve("temp_request.dat").toFile();
        String boundary = "----WebKitFormBoundaryABC123";
        writeMultipartRequestToFile(tempRequestFile, boundary, file);

        // 임시 파일에서 HttpRequest 객체 생성
        try (FileInputStream fis = new FileInputStream(tempRequestFile)) {
            HttpRequest request = HttpRequest.from(fis);
            // request 객체 사용 (필요한 경우)
        }

        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.printf("%s - Time: %d ms, Memory used: %d bytes%n",
                testName, (endTime - startTime), (endMemory - startMemory));

        // 임시 파일 삭제
        tempRequestFile.delete();
    }

    private java.io.File createLargeFile(Path tempDir, long size) throws IOException {
        java.io.File file = tempDir.resolve("test_file_" + size + ".bin").toFile();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] buffer = new byte[8192];
            Random random = new Random();
            long bytesWritten = 0;
            while (bytesWritten < size) {
                random.nextBytes(buffer);
                int bytesToWrite = (int) Math.min(buffer.length, size - bytesWritten);
                os.write(buffer, 0, bytesToWrite);
                bytesWritten += bytesToWrite;
            }
        }
        return file;
    }

    private void writeMultipartRequestToFile(java.io.File outputFile, String boundary, java.io.File inputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
             FileInputStream fis = new FileInputStream(inputFile)) {

            // Request Line and Headers
            writer.write("POST /upload HTTP/1.1\r\n");
            writer.write("Host: example.com\r\n");
            writer.write("User-Agent: Performance-Test-Agent/1.0\r\n");
            writer.write("Accept: */*\r\n");
            writer.write("Connection: keep-alive\r\n");
            writer.write("Content-Type: multipart/form-data; boundary=" + boundary + "\r\n");
            writer.write("Content-Length: " + (inputFile.length() + 500) + "\r\n");
            writer.write("\r\n");

            // Multipart body
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"" + inputFile.getName() + "\"\r\n");
            writer.write("Content-Type: application/octet-stream\r\n");
            writer.write("\r\n");
            writer.flush();

            // File content (streamed)
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile, true))) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }

            writer.write("\r\n");
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"description\"\r\n");
            writer.write("\r\n");
            writer.write("This is a test file upload\r\n");
            writer.write("--" + boundary + "--\r\n");
        }
    }
}
