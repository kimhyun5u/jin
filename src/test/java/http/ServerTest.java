package http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerTest {

    private static final int THREAD_POOL_SIZE = 10;
    private final int PORT = 9000;
    private Server server;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        server = Server.defaultServer(PORT, THREAD_POOL_SIZE);
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() throws IOException {
        // ExecutorService 종료
        executorService.shutdownNow();

        // 서버 종료
        server.stop();

        // 서버 종료 대기
        while (!server.isClose()) {
        }

    }

    @Test
    @DisplayName("서버 시작 및 요청 처리")
    void testServerStartAndHandleRequest() throws IOException {
        // 테스트용 핸들러 등록
        server.get("/test", ctx -> {
            ctx.response()
                    .setStatus(HttpStatus.OK)
                    .addHeader("Content-Type", "text/plain")
                    .setBody("Test response".getBytes());
        });

        // 별도의 스레드에서 서버 시작
        executorService.execute(() -> {
            server.start();
        });

        // 서버 연결 대기
        while (!server.isConnected()) {
        }

        // HTTP 요청 보내기
        URL url = new URL("http://localhost:" + PORT + "/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 응답 확인
        int responseCode = connection.getResponseCode();
        assertEquals(HttpStatus.OK.getCode(), responseCode);


        // 연결 종료
        connection.disconnect();
    }

    @Test
    @DisplayName("요청 경로 없음")
    void testNotFoundRoute() throws IOException {
        // 별도의 스레드에서 서버 시작
        executorService.execute(() -> {
            server.start();
        });

        while (!server.isConnected()) {
        }

        URL url = new URL("http://localhost:" + PORT + "/nonexistent");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        assertEquals(HttpStatus.NOT_FOUND.getCode(), responseCode);

        connection.disconnect();
    }
}
