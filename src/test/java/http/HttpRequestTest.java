package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

}
