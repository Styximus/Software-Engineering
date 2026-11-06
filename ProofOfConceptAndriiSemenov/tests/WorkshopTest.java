import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Real HTTP integration: publisher -> shared server -> second client -> offline map -> quiz. */
public final class WorkshopTest {
    private static int checks;
    public static void main(String[] args) throws Exception {
        Path temporary = Files.createTempDirectory(Path.of("out"), "workshop-test-");
        WorkshopServer server = null;
        try {
            Path serverData = temporary.resolve("server");
            WorkshopStore seed = new WorkshopStore(serverData);
            try (var files = Files.list(Path.of("samples/catalog"))) {
                for (Path path : files.filter(p -> p.toString().endsWith(".qmap")).toList()) seed.save(MapPackage.readFile(path));
            }
            server = new WorkshopServer("127.0.0.1", 0, serverData);
            String url = "http://127.0.0.1:" + server.port();
            WorkshopClient alice = new WorkshopClient(url), bob = new WorkshopClient(url);
            WorkshopStore libraryA = new WorkshopStore(temporary.resolve("alice"));
            WorkshopStore libraryB = new WorkshopStore(temporary.resolve("bob"));
            check(alice.list().size() == 4, "Shared catalog contains four seed maps");
            byte[] upload = MapPackage.readFile(Path.of("samples/upload/kuestenpfad.qmap"));
            MapPackage map = MapPackage.parse(upload);
            check(map.width() == 16 && map.questions().size() == 2, "Map payload includes terrain and quiz");
            String id = alice.publish(upload);
            check(bob.list().stream().anyMatch(i -> i.id().equals(id)), "Second client sees published map over HTTP");
            check(alice.publish(upload).equals(id) && alice.list().size() == 5, "Duplicate upload is idempotent");
            bob.install(id, libraryB);
            check(!libraryA.contains(id) && libraryB.contains(id), "Client libraries are independent");
            check(Arrays.equals(upload, libraryB.read(id)), "Downloaded bytes and hash match upload");
            check(new WorkshopStore(temporary.resolve("bob")).list().size() == 1, "Library persists across restart");
            Properties bad = MapPackage.properties(upload); bad.setProperty("format", "2");
            byte[] badVersion = MapPackage.encode(bad);
            rejects(() -> alice.publish(badVersion), "Reject unsupported format");
            var rawHttp = java.net.http.HttpClient.newHttpClient();
            var invalidUpload = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url + "/api/maps"))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(badVersion)).build();
            check(rawHttp.send(invalidUpload, java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode() == 400,
                "Server independently validates uploads");
            var oversizedUpload = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url + "/api/maps"))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(new byte[MapPackage.MAX_BYTES + 1])).build();
            check(rawHttp.send(oversizedUpload, java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode() == 400,
                "Server independently bounds upload size");
            rejects(() -> libraryB.save(new byte[MapPackage.MAX_BYTES + 1]), "Reject oversized package");
            rejects(() -> bob.install("../escape", libraryB), "Reject path traversal");
            rejects(() -> bob.install("a".repeat(64), libraryB), "Missing map reports HTTP failure");
            bad = MapPackage.properties(upload); bad.setProperty("npc.x", "0");
            final byte[] blocked = MapPackage.encode(bad);
            rejects(() -> alice.publish(blocked), "NPC cannot stand on water");
            Properties trapped = MapPackage.properties(upload);
            trapped.setProperty("row.3", "~.T............~");
            trapped.setProperty("row.4", "~T.T........#..~");
            trapped.setProperty("row.5", "~.T#...........~");
            rejects(() -> MapPackage.parse(MapPackage.encode(trapped)), "Unreachable NPC rejected");
            check(bob.list().size() == 5 && libraryB.list().size() == 1, "Rejected uploads preserve existing maps");

            WorkshopStore uiLibrary = new WorkshopStore(temporary.resolve("ui-library"));
            WorkshopUI ui = edt(() -> { WorkshopUI panel = new WorkshopUI(bob, uiLibrary); panel.setSize(1280, 860); panel.reload(); return panel; });
            waitIdle(ui);
            check(edt(ui::catalogSize) == 5, "Swing loads shared catalog asynchronously");
            Path screenshots = args.length > 0 ? Path.of(args[0]) : null;
            shot(ui, screenshots, "01-workshop");
            edt(() -> { ((JTextField) find(ui, "search")).setText("Küsten"); return null; });
            check(edt(() -> find(ui, "map-" + id) != null), "Search finds uploaded map");
            edt(() -> { click(ui, "install"); return null; }); waitIdle(ui);
            check(uiLibrary.contains(id), "UI download writes local package");
            edt(() -> { click(ui, "library"); return null; });
            shot(ui, screenshots, "02-library");
            edt(() -> { click(ui, "install"); return null; });
            MapUI mapUI = edt(() -> findType(ui, MapUI.class));
            check(mapUI != null, "Play opens downloaded map");
            edt(() -> {
                int x = mapUI.playerX();
                mapUI.movePlayer(-1, 0); mapUI.movePlayer(-1, 0);
                check(mapUI.playerX() == 1, "Water blocks player");
                mapUI.movePlayer(1, 0); check(mapUI.playerX() == x, "Move back to spawn");
                check(!mapUI.nearNpc(), "NPC interaction requires proximity");
                mapUI.movePlayer(1, 0); mapUI.movePlayer(1, 0);
                check(mapUI.nearNpc(), "Walking reaches package NPC");
                mapUI.movePlayer(1, 0); check(mapUI.playerX() == 4, "NPC tile blocks movement");
                return null;
            });
            shot(ui, screenshots, "03-map");
            edt(() -> { click(ui, "interact"); return null; });
            check(edt(() -> findType(ui, QuizUI.class) != null), "NPC opens package quiz");
            shot(ui, screenshots, "04-quiz");
            edt(() -> {
                for (Question question : map.questions()) {
                    click(ui, "answer-" + question.correctIndex()); click(ui, "primary"); click(ui, "primary");
                }
                check(((JLabel) find(ui, "score")).getText().equals("2 / 2"), "Downloaded quiz awards its own points");
                return null;
            });
            shot(ui, screenshots, "05-result");
            edt(() -> { click(ui, "back"); ((JTextField) find(ui, "search")).setText("zzzz"); return null; });
            shot(ui, screenshots, "06-empty-search");
            server.close(); server = null;
            edt(() -> { ((JTextField) find(ui, "search")).setText(""); click(ui, "discover"); return null; });
            waitIdle(ui);
            edt(() -> { click(ui, "library"); click(ui, "install"); return null; });
            check(edt(() -> findType(ui, MapUI.class) != null), "Installed map plays while server is offline");
            shot(ui, screenshots, "07-offline-map");
            try (WorkshopServer restarted = new WorkshopServer("127.0.0.1", 0, serverData)) {
                check(new WorkshopClient("http://127.0.0.1:" + restarted.port()).list().size() == 5, "Server catalog survives restart");
            }
            try (WorkshopServer secured = new WorkshopServer("127.0.0.1", 0, serverData, "integration-test-key")) {
                String secureUrl = "http://127.0.0.1:" + secured.port();
                rejects(() -> new WorkshopClient(secureUrl).list(), "Missing shared key rejected");
                check(new WorkshopClient(secureUrl, "integration-test-key").list().size() == 5, "Shared key grants access");
            }
            System.out.println("PASS: " + checks + " Workshop checks (real HTTP, two clients, persistence, map and quiz UI).");
        } finally {
            if (server != null) server.close();
            // Only this test's freshly allocated directory is removed.
            try (var paths = Files.walk(temporary)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }

    private static void waitIdle(WorkshopUI ui) throws Exception {
        long end = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(18);
        while (edt(ui::isBusy) && System.nanoTime() < end) Thread.sleep(20);
        check(!edt(ui::isBusy), "Background request completes");
    }
    private static <T> T edt(Callable<T> action) throws Exception {
        AtomicReference<T> value = new AtomicReference<>(); AtomicReference<Exception> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> { try { value.set(action.call()); } catch (Exception error) { failure.set(error); } });
        if (failure.get() != null) throw failure.get(); return value.get();
    }
    private static Component find(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container c) { Component found = find(c, name); if (found != null) return found; }
        }
        return null;
    }
    private static <T> T findType(Container parent, Class<T> type) {
        if (type.isInstance(parent)) return type.cast(parent);
        for (Component child : parent.getComponents()) if (child instanceof Container c) {
            T found = findType(c, type); if (found != null) return found;
        }
        return null;
    }
    private static void click(Container root, String name) {
        JButton b = (JButton) find(root, name); check(b != null && b.isEnabled(), "Action available: " + name); b.doClick(0);
    }
    private static void shot(JPanel panel, Path directory, String name) throws Exception {
        if (directory == null) return;
        edt(() -> {
            layout(panel); layout(panel); // Scroll panes establish their viewport in the first pass.
            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics(); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            panel.printAll(g); g.dispose(); Files.createDirectories(directory);
            ImageIO.write(image, "png", directory.resolve(name + ".png").toFile()); return null;
        });
    }
    private static void layout(Container c) { c.doLayout(); for (Component child : c.getComponents()) if (child instanceof Container p) layout(p); }
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    private static void rejects(Throwing action, String message) throws Exception {
        try { action.run(); } catch (IOException expected) { checks++; return; } throw new AssertionError(message);
    }
    private interface Throwing { void run() throws Exception; }
}
