# Jin Web Framework

Jin is a web framework written in Java. It features a coffee-like API with performance that is up to 1x faster. If you need fun and enjoy, you will love Jin.

## How to Use

### build.gradle
``` groovy
implementation 'com.kimhyun5u.jin:jin:1.0.0'
```

### main.java
``` java
import http.Server;

public class Main {
    public static void main(String[] args) {

        Server server = Server.defaultServer(8080, 10);

        server.get("/", (ctx) -> {
                    ctx.response().setBody("Hello, World!".getBytes());
                }
        );

        server.start();
    }
}
```
