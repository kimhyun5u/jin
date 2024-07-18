package http.router;

import http.handler.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RouterTest {
    Router r;

    @BeforeEach
    void setUp() {
        r = new Router();
    }

    @Test
    @DisplayName("루트 등록 및 핸들러 확인 테스트")
    void testAddRouteAndGetHandler() {
        Handler handler = ctx -> {
        };
        r.addRoute("GET", "/test", handler);

        Handler result = r.getHandlers("GET", "/test");
        assertSame(handler, result);
    }

    @Test
    @DisplayName("없는 루트 핸들러 테스트")
    void testGetHandlerForNonExistentRoute() {
        Handler result = r.getHandlers("GET", "/test");
        assertNull(result);
    }
}
