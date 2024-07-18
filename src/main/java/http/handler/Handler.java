package http.handler;


import http.Context;

@FunctionalInterface
public interface Handler {
    void handle(Context ctx);
}
