package http.router;

import http.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final Map<String, Map<String, Handler>> routes;
    private final Map<String, Handler> staticRoutes;


    public Router() {
        routes = new ConcurrentHashMap<>();
        staticRoutes = new ConcurrentHashMap<>();
    }


    public void addRoute(String method, String path, Handler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public void staticFiles(String path, Handler staticHandler) {
        staticRoutes.put(path, staticHandler);
    }

    public Handler getHandlers(String method, String path) {
        Map<String, Handler> methodRoutes = routes.get(method);
        if (methodRoutes != null && methodRoutes.containsKey(path)) {
            return methodRoutes.get(path);
        }

        // 정적 파일 라우트 확인
        for (Map.Entry<String, Handler> entry : staticRoutes.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                String resourcePath = entry.getValue() + path;

                try {
                    return entry.getValue();
                } catch (Exception e) {
                    logger.error("Failed to load static file {}", resourcePath, e);
                    return null;
                }
            }
        }

        return null;
    }
}
