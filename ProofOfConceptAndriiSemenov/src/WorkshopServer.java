import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Small shared server for a trusted demo/LAN. Deploy separately from Swing clients. */
public final class WorkshopServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService workers = Executors.newFixedThreadPool(4);
    private final WorkshopStore store;
    private final String token;

    public WorkshopServer(String bind, int port, Path directory) throws IOException {
        this(bind, port, directory, null);
    }

    public WorkshopServer(String bind, int port, Path directory, String token) throws IOException {
        this.token = token;
        store = new WorkshopStore(directory);
        server = HttpServer.create(new InetSocketAddress(bind, port), 16);
        server.setExecutor(workers);
        server.createContext("/api/maps", this::handle);
        server.start();
    }

    public int port() { return server.getAddress().getPort(); }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            if (token != null && !token.isBlank() && !java.security.MessageDigest.isEqual(
                    ("Bearer " + token).getBytes(StandardCharsets.UTF_8),
                    Objects.toString(exchange.getRequestHeaders().getFirst("Authorization"), "").getBytes(StandardCharsets.UTF_8))) {
                reply(exchange, 401, "Server-Schlüssel fehlt oder ist ungültig."); return;
            }
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.equals("/api/maps") && method.equals("GET")) {
                Properties data = new Properties();
                List<WorkshopStore.Entry> entries = store.list();
                data.setProperty("count", "" + entries.size());
                for (int i = 0; i < entries.size(); i++) {
                    var entry = entries.get(i);
                    var map = entry.map();
                    String p = i + ".";
                    data.setProperty(p + "id", entry.id());
                    data.setProperty(p + "title", map.title());
                    data.setProperty(p + "author", map.author());
                    data.setProperty(p + "category", map.category());
                    data.setProperty(p + "description", map.description());
                    data.setProperty(p + "questions", "" + map.questions().size());
                    data.setProperty(p + "size", map.width() + " × " + map.height());
                }
                reply(exchange, 200, MapPackage.encode(data));
            } else if (path.equals("/api/maps") && method.equals("POST")) {
                byte[] bytes = MapPackage.readBounded(exchange.getRequestBody(), MapPackage.MAX_BYTES);
                String id = store.save(bytes);
                reply(exchange, 201, id.getBytes(StandardCharsets.UTF_8));
            } else if (path.startsWith("/api/maps/") && method.equals("GET")) {
                String id = MapPackage.validId(path.substring("/api/maps/".length()));
                if (!store.contains(id)) reply(exchange, 404, "Karte nicht gefunden.");
                else {
                    exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + id + ".qmap\"");
                    reply(exchange, 200, store.read(id));
                }
            } else { reply(exchange, 405, "Methode oder Route nicht unterstützt."); }
        } catch (IOException | IllegalArgumentException error) {
            reply(exchange, 400, error.getMessage() == null ? "Ungültiges Kartenpaket." : error.getMessage());
        } finally { exchange.close(); }
    }

    private static void reply(HttpExchange e, int status, String text) throws IOException {
        reply(e, status, text.getBytes(StandardCharsets.UTF_8));
    }
    private static void reply(HttpExchange e, int status, byte[] bytes) throws IOException {
        e.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        e.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        e.getResponseHeaders().set("Cache-Control", "no-store");
        e.sendResponseHeaders(status, bytes.length);
        e.getResponseBody().write(bytes);
    }

    @Override public void close() { server.stop(0); workers.shutdownNow(); }

    public static void main(String[] args) throws Exception {
        String bind = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8765;
        Path data = Path.of(args.length > 2 ? args[2] : "data/server");
        if (args.length > 3) {
            WorkshopStore seeds = new WorkshopStore(data);
            try (var files = Files.list(Path.of(args[3]))) {
                for (Path file : files.filter(p -> p.toString().endsWith(".qmap")).sorted().toList())
                    seeds.save(MapPackage.readFile(file));
            }
        }
        WorkshopServer app = new WorkshopServer(bind, port, data, System.getenv("QUIZMASTER_TOKEN"));
        Runtime.getRuntime().addShutdownHook(new Thread(app::close));
        System.out.println("Workshop server: http://" + bind + ":" + app.port());
        System.out.println("Map storage: " + data.toAbsolutePath());
        System.out.println("Trusted demo/LAN server. Stop with Ctrl+C.");
    }
}
