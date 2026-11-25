package de.quizmaster.workshop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/** A configured shared catalog or a loopback demo for the single-click desktop launch. */
public final class WorkshopConnection implements AutoCloseable {
    private final WorkshopServer localServer;
    private final WorkshopClient client;
    private final WorkshopStore library;

    public WorkshopConnection(Path directory, String serverUrl, String token) throws IOException {
        library = new WorkshopStore(directory.resolve("library"));
        if (serverUrl != null && !serverUrl.isBlank()) {
            client = new WorkshopClient(serverUrl.strip(), token);
            localServer = null;
            return;
        }
        Path catalogDirectory = directory.resolve("catalog");
        WorkshopStore catalog = new WorkshopStore(catalogDirectory);
        for (String file : List.of("code.qmap", "orbit.qmap", "waldpfad.qmap", "zeitreise.qmap")) {
            try (InputStream input = WorkshopConnection.class.getResourceAsStream("/workshop/" + file)) {
                if (input == null) throw new IOException("Beispielkarte fehlt: " + file);
                byte[] bytes = MapPackage.readBounded(input, MapPackage.MAX_BYTES);
                if (!catalog.contains(MapPackage.hash(bytes))) catalog.save(bytes);
            }
        }
        // Only this application instance should be able to modify its local catalog.
        String localToken = java.util.UUID.randomUUID().toString();
        localServer = new WorkshopServer("127.0.0.1", 0, catalogDirectory, localToken);
        client = new WorkshopClient("http://127.0.0.1:" + localServer.port(), localToken);
    }

    public WorkshopUI view(java.util.function.Consumer<javax.swing.JPanel> navigate, Runnable back) {
        WorkshopUI view = new WorkshopUI(client, library, navigate, back,
                localServer == null ? client.address() : "Lokaler Workshop");
        view.reload();
        return view;
    }

    @Override public void close() {
        if (localServer != null) localServer.close();
    }
}
