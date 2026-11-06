import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Used separately by the server and each client's offline library. */
public final class WorkshopStore {
    private final Path directory;
    public record Entry(String id, MapPackage map) {}

    public WorkshopStore(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        Files.createDirectories(this.directory);
    }

    public synchronized String save(byte[] bytes) throws IOException {
        MapPackage.parse(bytes); // Validate before creating any file.
        String id = MapPackage.hash(bytes);
        Path target = file(id);
        if (Files.exists(target)) {
            if (!Arrays.equals(read(id), bytes)) throw new IOException("Beschädigte vorhandene Karte.");
            return id;
        }
        if (list().size() >= 100) throw new IOException("Der PoC unterstützt maximal 100 Karten pro Speicher.");
        Path temporary = Files.createTempFile(directory, ".upload-", ".tmp");
        try {
            Files.write(temporary, bytes);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, target); }
        } finally { Files.deleteIfExists(temporary); }
        return id;
    }

    public byte[] read(String id) throws IOException {
        byte[] bytes = MapPackage.readFile(file(id));
        if (!MapPackage.hash(bytes).equals(id)) throw new IOException("Prüfsumme der Karte stimmt nicht.");
        return bytes;
    }

    public boolean contains(String id) throws IOException { return Files.isRegularFile(file(id)); }

    public List<Entry> list() throws IOException {
        List<Entry> entries = new ArrayList<>();
        try (var files = Files.list(directory)) {
            for (Path path : files.filter(p -> p.getFileName().toString().matches("[a-f0-9]{64}\\.qmap")).sorted().toList()) {
                String id = path.getFileName().toString().replace(".qmap", "");
                entries.add(new Entry(id, MapPackage.parse(read(id))));
            }
        }
        return List.copyOf(entries);
    }

    private Path file(String id) throws IOException { return directory.resolve(MapPackage.validId(id) + ".qmap"); }
}
