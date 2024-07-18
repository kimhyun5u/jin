package http;

public class Context {
    private final HttpRequest request;
    private final HttpResponse response;

    public Context(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpRequest request() {
        return request;
    }

    public HttpResponse response() {
        return response;
    }
}
