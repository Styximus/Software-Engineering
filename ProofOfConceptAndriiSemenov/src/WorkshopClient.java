import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/** Network boundary. Call from a worker, never the Swing event thread. */
public final class WorkshopClient {
    public record Item(String id, String title, String author, String category, String description, int questions, String size) {
        static Item local(WorkshopStore.Entry e) {
            MapPackage m = e.map();
            return new Item(e.id(), m.title(), m.author(), m.category(), m.description(), m.questions().size(), m.width() + " × " + m.height());
        }
    }
    private final URI base;
    private final String token;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();

    public WorkshopClient(String url) {
        this(url, null);
    }
    public WorkshopClient(String url, String token) {
        this.token = token;
        base = URI.create(url.replaceAll("/+$", ""));
        if (!("http".equals(base.getScheme()) || "https".equals(base.getScheme())) || base.getHost() == null
                || base.getRawQuery() != null || base.getFragment() != null || base.getUserInfo() != null
                || !(base.getPath().isEmpty() || base.getPath().equals("/")))
            throw new IllegalArgumentException("Serveradresse muss http(s)://hostname:port sein.");
    }

    public String address() { return base.toString(); }

    public List<Item> list() throws IOException, InterruptedException {
        Properties p = MapPackage.properties(request("/api/maps", null, 200_000));
        List<Item> items = new ArrayList<>();
        try {
            int count = Integer.parseInt(p.getProperty("count"));
            if (count < 0 || count > 100) throw new IllegalArgumentException();
            for (int i = 0; i < count; i++) {
                String key = i + ".";
                int questions = Integer.parseInt(p.getProperty(key + "questions"));
                if (questions < 1 || questions > 10) throw new IllegalArgumentException();
                items.add(new Item(MapPackage.validId(p.getProperty(key + "id")), value(p,key+"title",50),
                    value(p,key+"author",40), value(p,key+"category",30), value(p,key+"description",350),
                    questions, value(p,key+"size",20)));
            }
        } catch (IllegalArgumentException error) { throw new IOException("Ungültiger Serverkatalog.", error); }
        return items.stream().sorted(Comparator.comparing(Item::title)).toList();
    }

    private String value(Properties p, String key, int max) throws IOException {
        String v = p.getProperty(key);
        if (v == null || v.isBlank() || v.length() > max) throw new IOException("Ungültiges Katalogfeld: " + key);
        return v;
    }

    public String publish(byte[] bytes) throws IOException, InterruptedException {
        MapPackage.parse(bytes);
        String id = new String(request("/api/maps", bytes, 1024), StandardCharsets.UTF_8).strip();
        if (!MapPackage.hash(bytes).equals(id)) throw new IOException("Server meldet eine falsche Prüfsumme.");
        return id;
    }

    public String install(String id, WorkshopStore library) throws IOException, InterruptedException {
        byte[] bytes = request("/api/maps/" + MapPackage.validId(id), null, MapPackage.MAX_BYTES);
        if (!MapPackage.hash(bytes).equals(id)) throw new IOException("Download-Prüfsumme stimmt nicht.");
        return library.save(bytes);
    }

    private byte[] request(String path, byte[] upload, int limit) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(base.resolve(path)).timeout(Duration.ofSeconds(12));
        if (token != null && !token.isBlank()) builder.header("Authorization", "Bearer " + token);
        if (upload != null) builder.header("Content-Type", "text/plain; charset=utf-8").POST(HttpRequest.BodyPublishers.ofByteArray(upload));
        HttpResponse<byte[]> response = client.send(builder.build(), info -> {
            // Reject oversized responses while receiving; do not allocate an unbounded body.
            return new LimitedBody(limit);
        });
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new IOException("Server " + response.statusCode() + ": " + new String(response.body(), StandardCharsets.UTF_8));
        return response.body();
    }

    private static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final java.util.concurrent.CompletableFuture<byte[]> future = new java.util.concurrent.CompletableFuture<>();
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final int limit;
        private java.util.concurrent.Flow.Subscription subscription;
        LimitedBody(int limit) { this.limit = limit; }
        public java.util.concurrent.CompletionStage<byte[]> getBody() { return future; }
        public void onSubscribe(java.util.concurrent.Flow.Subscription s) { subscription = s; s.request(1); }
        public void onNext(List<java.nio.ByteBuffer> buffers) {
            for (var buffer : buffers) {
                if (bytes.size() + buffer.remaining() > limit) {
                    subscription.cancel(); future.completeExceptionally(new IOException("Serverantwort zu groß.")); return;
                }
                byte[] part = new byte[buffer.remaining()]; buffer.get(part); bytes.writeBytes(part);
            }
            subscription.request(1);
        }
        public void onError(Throwable error) { future.completeExceptionally(error); }
        public void onComplete() { future.complete(bytes.toByteArray()); }
    }
}
