package http;

import http.handler.Handler;
import http.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final int port;
    private final int threadPoolSize;
    private final ExecutorService threadPool;
    private final Router router;
    private ServerSocket serverSocket;
    String notFoundHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>404 Not Found</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f0f0f0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                    }
                    .container {
                        background-color: white;
                        padding: 2rem;
                        border-radius: 10px;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
                        text-align: center;
                    }
                    h1 {
                        color: #4362d0;
                    }
                    p {
                        color: #34495e;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>404 Not Found</h1>
                    <p>The page you are looking for doesn't exist or has been moved.</p>
                    <p>Please check the URL or go back to the <a href="/">homepage</a>.</p>
                </div>
            </body>
            </html>
            """;

    private Server(int port, int threadPoolSize) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
        this.router = new Router();
    }

    public void get(String path, Handler handler) {
        addRoute("GET", path, handler);
    }

    public void post(String path, Handler handler) {
        addRoute("POST", path, handler);
    }

    public void staticFiles(String path, String staticPath) {
        router.staticFiles(path, staticPath);
    }

    public static Server defaultServer(int port, int threadPoolSize) {
        return new Server(port, threadPoolSize);
    }
    private void addRoute(String method, String path, Handler handler) {
        router.addRoute(method, path, handler);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            Runtime.getRuntime().addShutdownHook(new Thread(threadPool::shutdown));

            // init MIME
            MIME.init();

            logger.info("Listening for connection on port {} ....", port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleRequest(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Error Starting Server", e);
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (clientSocket; var input = clientSocket.getInputStream(); var output = clientSocket.getOutputStream()) {
            HttpRequest req = HttpRequest.from(input);
            HttpResponse res = new HttpResponse(output);

            Context ctx = new Context(req, res);

            Handler handler = router.getHandlers(req.getMethod(), req.getPath());

            logger.info(req.getRequestLine());

            if (handler != null) {
                handler.handle(ctx);
            } else {
                res.setStatus(HttpStatus.NOT_FOUND);
                res.addHeader("Content-Type", "text/html");
                res.setBody(notFoundHtml.getBytes());
            }

            res.send();
        } catch (IOException e) {
            logger.error("Error Handling Request", e);
        }
    }

    public boolean isConnected() {
        return serverSocket != null && serverSocket.isBound();
    }

    public boolean isClose() {
        return serverSocket != null || serverSocket.isClosed();
    }

    public void stop() throws IOException {
        serverSocket.close();
        threadPool.shutdown();
    }
}
