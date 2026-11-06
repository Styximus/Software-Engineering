import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Portable, data-only map format. No Java serialization, scripts or external assets. */
public record MapPackage(String title, String author, String category, String description,
        List<String> tiles, int spawnX, int spawnY, int npcX, int npcY, String npcName,
        List<Question> questions) {
    public static final int MAX_BYTES = 65_536;

    public static byte[] readBounded(InputStream input, int limit) throws IOException {
        byte[] bytes = input.readNBytes(limit + 1);
        if (bytes.length > limit) throw new IOException("Datei ist zu groß (max. " + limit + " Bytes).");
        return bytes;
    }

    public static byte[] readFile(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) { return readBounded(input, MAX_BYTES); }
    }

    public static Properties properties(byte[] bytes) throws IOException {
        Properties data = new Properties();
        try { data.load(new StringReader(new String(bytes, StandardCharsets.UTF_8))); }
        catch (IllegalArgumentException error) { throw new IOException("Ungültiges Dateiformat.", error); }
        return data;
    }

    public static byte[] encode(Properties data) throws IOException {
        StringWriter writer = new StringWriter();
        data.store(writer, "QuizMaster Workshop");
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static MapPackage parse(byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) throw new IOException("Kartenpaket ist zu groß.");
        Properties p = properties(bytes);
        if (!"1".equals(p.getProperty("format"))) throw new IOException("Nur Kartenformat 1 wird unterstützt.");
        String title = text(p, "title", 50), author = text(p, "author", 40);
        String category = text(p, "category", 30), description = text(p, "description", 350);
        int width = number(p, "width", 8, 24), height = number(p, "height", 6, 16);
        List<String> tiles = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            String row = text(p, "row." + y, width);
            if (row.length() != width || !row.matches("[.T~#]+")) throw new IOException("Ungültige Kartenzeile " + y);
            tiles.add(row);
        }
        int sx = number(p, "spawn.x", 0, width - 1), sy = number(p, "spawn.y", 0, height - 1);
        int nx = number(p, "npc.x", 0, width - 1), ny = number(p, "npc.y", 0, height - 1);
        if (tiles.get(sy).charAt(sx) != '.' || tiles.get(ny).charAt(nx) != '.' || sx == nx && sy == ny)
            throw new IOException("Spieler und NPC brauchen verschiedene begehbare Positionen.");
        int count = number(p, "questions", 1, 10);
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String prefix = "q." + i + ".";
            List<String> answers = new ArrayList<>();
            for (int a = 0; a < 4; a++) answers.add(text(p, prefix + "a." + a, 65));
            questions.add(new Question(text(p, prefix + "text", 130), answers,
                number(p, prefix + "correct", 0, 3), text(p, prefix + "explanation", 200)));
        }
        MapPackage map = new MapPackage(title, author, category, description, List.copyOf(tiles),
            sx, sy, nx, ny, text(p, "npc.name", 30), List.copyOf(questions));
        if (!map.reachable()) throw new IOException("Der Quiz-NPC ist vom Startpunkt nicht erreichbar.");
        return map;
    }

    private static String text(Properties p, String key, int max) throws IOException {
        String value = p.getProperty(key, "").strip();
        if (value.isEmpty() || value.length() > max || value.chars().anyMatch(Character::isISOControl))
            throw new IOException("Ungültiges oder fehlendes Feld: " + key);
        return value;
    }

    private static int number(Properties p, String key, int min, int max) throws IOException {
        try {
            int n = Integer.parseInt(p.getProperty(key, ""));
            if (n < min || n > max) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException error) { throw new IOException("Ungültige Zahl: " + key); }
    }

    public int width() { return tiles.get(0).length(); }
    public int height() { return tiles.size(); }
    public boolean walkable(int x, int y) {
        return x >= 0 && y >= 0 && x < width() && y < height() && tiles.get(y).charAt(x) == '.';
    }

    private boolean reachable() {
        boolean[][] visited = new boolean[height()][width()];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{spawnX, spawnY});
        visited[spawnY][spawnX] = true;
        while (!queue.isEmpty()) {
            int[] at = queue.remove();
            if (Math.abs(at[0] - npcX) + Math.abs(at[1] - npcY) <= 1) return true;
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int x = at[0] + d[0], y = at[1] + d[1];
                if (walkable(x, y) && !visited[y][x]) { visited[y][x] = true; queue.add(new int[]{x, y}); }
            }
        }
        return false;
    }

    public static String hash(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    public static String validId(String id) throws IOException {
        if (id == null || !id.matches("[a-f0-9]{64}")) throw new IOException("Ungültige Karten-ID.");
        return id;
    }
}
