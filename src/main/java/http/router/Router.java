package http.router;

import http.Context;
import http.HttpStatus;
import http.MIME;
import http.handler.Handler;
import utils.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    private static final String ROOT_PATH = "/index.html";

    private final Map<String, Map<String, Handler>> routes;
    private final Map<String, String> staticRoutes;


    public Router() {
        routes = new ConcurrentHashMap<>();
        staticRoutes = new ConcurrentHashMap<>();
    }


    public void addRoute(String method, String path, Handler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public void staticFiles(String path, String staticPath) {
        staticRoutes.put(path, staticPath);
    }

    public Handler getHandlers(String method, String path) {
        Map<String, Handler> methodRoutes = routes.get(method);
        if (methodRoutes != null && methodRoutes.containsKey(path)) {
            return methodRoutes.get(path);
        }

        // 정적 파일 라우트 확인
        for (Map.Entry<String, String> entry : staticRoutes.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                if (path.equals("/")) {
                    path = ROOT_PATH;
                } else if (ResourceResolver.getFileExtension(path).isEmpty()) {
                    path += ROOT_PATH;
                }

                String resourcePath = entry.getValue() + path;

                try {
                    return createStaticFileHandler(resourcePath);
                } catch (Exception e) {
                    logger.error("Failed to load static file {}", resourcePath, e);
                    return null;
                }
            }
        }

        return null;
    }

    private Handler createStaticFileHandler(String path) {
        byte[] file = ResourceResolver.readResourceFileAsBytes(path);
        return (Context ctx) -> {
            ctx.response().setBody(file);
            ctx.response().addHeader("Content-Type", MIME.getMIMEType(ResourceResolver.getFileExtension(path)));
            ctx.response().addHeader("Content-Length", String.valueOf(file.length));
            ctx.response().setStatus(HttpStatus.OK);
        };
    }
}
