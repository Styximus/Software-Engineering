import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Desktop Workshop: one remote catalog, one private offline library per client. */
public final class WorkshopUI extends JPanel {
    private final WorkshopClient client;
    private final WorkshopStore library;
    private final JPanel body = WorkshopStyle.panel(new BorderLayout());
    private final JPanel cards = WorkshopStyle.panel(new GridLayout(0, 2, 16, 16));
    private final JPanel details = WorkshopStyle.panel(new BorderLayout(0, 18));
    private final JLabel status = WorkshopStyle.label("Bereit", 12, WorkshopStyle.MUTED);
    private final JLabel count = WorkshopStyle.label("", 12, WorkshopStyle.MUTED);
    private final JTextField search = new JTextField();
    private final JComboBox<String> category = new JComboBox<>();
    private final JButton discover = WorkshopStyle.button("Workshop", "discover", true);
    private final JButton mine = WorkshopStyle.button("Meine Bibliothek", "library", false);
    private final JButton publish = WorkshopStyle.button("Karte veröffentlichen", "publish", false);
    private final JButton refresh = WorkshopStyle.button("Aktualisieren", "refresh", false);
    private final Set<String> installed = new HashSet<>();
    private List<WorkshopClient.Item> remote = List.of(), local = List.of();
    private WorkshopClient.Item selected;
    private boolean localMode, busy, updatingCategories;
    private JButton install;

    public WorkshopUI(WorkshopClient client, WorkshopStore library) {
        this.client = client; this.library = library;
        setLayout(new BorderLayout(0, 0)); setBackground(WorkshopStyle.BG);
        JPanel nav = new JPanel(new BorderLayout()); nav.setBackground(new Color(13, 19, 28));
        nav.setBorder(new EmptyBorder(18, 26, 18, 26));
        nav.add(WorkshopStyle.html("<b>QUIZMASTER</b> &nbsp; / &nbsp; COMMUNITY", 15, WorkshopStyle.TEXT), BorderLayout.WEST);
        JPanel tabs = WorkshopStyle.panel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tabs.add(discover); tabs.add(mine); tabs.add(publish); nav.add(tabs, BorderLayout.EAST);
        add(nav, BorderLayout.NORTH);
        body.setBorder(new EmptyBorder(24, 28, 20, 28)); add(body, BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(12, 28, 16, 28)); add(status, BorderLayout.SOUTH);
        search.setName("search"); search.setFont(WorkshopStyle.font(15, false)); search.setColumns(22);
        search.setBackground(WorkshopStyle.PANEL); search.setForeground(WorkshopStyle.TEXT); search.setCaretColor(WorkshopStyle.ACCENT);
        search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WorkshopStyle.LINE), new EmptyBorder(12, 14, 12, 14)));
        search.getAccessibleContext().setAccessibleName("Karten, Themen oder Autoren suchen");
        category.setName("category"); category.addItem("Alle Themen");
        category.setPreferredSize(new Dimension(150, 42)); category.setFont(WorkshopStyle.font(13, false));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        category.addActionListener(e -> { if (!updatingCategories) filter(); });
        discover.addActionListener(e -> { localMode = false; selected = null; catalog(); reload(); });
        mine.addActionListener(e -> { localMode = true; selected = null; catalog(); loadLocal(); });
        refresh.addActionListener(e -> { if (localMode) loadLocal(); else reload(); });
        publish.addActionListener(e -> chooseUpload());
        catalog(); loadLocal();
    }

    public void reload() {
        work("Verbinde mit " + client.address() + " …", () -> client.list(), result -> {
            remote = result; categories(); filter(); status.setText("Verbunden mit " + client.address() + " · " + remote.size() + " veröffentlichte Karten");
        });
    }

    private void loadLocal() {
        try {
            local = library.list().stream().map(WorkshopClient.Item::local).toList();
            installed.clear(); local.forEach(i -> installed.add(i.id())); categories(); filter();
            if (localMode) status.setText("Lokale Bibliothek · " + local.size() + " Karten · Offline verfügbar");
        } catch (IOException error) { status.setText("Bibliothek nicht lesbar: " + error.getMessage()); }
    }

    private void categories() {
        Object before = category.getSelectedItem(); updatingCategories = true;
        category.removeAllItems(); category.addItem("Alle Themen");
        (localMode ? local : remote).stream().map(WorkshopClient.Item::category).distinct().sorted().forEach(category::addItem);
        category.setSelectedItem(before); if (category.getSelectedIndex() < 0) category.setSelectedIndex(0);
        updatingCategories = false;
    }

    private void catalog() {
        body.removeAll();
        discover.setBackground(localMode ? WorkshopStyle.LINE : WorkshopStyle.ACCENT);
        discover.setForeground(localMode ? WorkshopStyle.TEXT : WorkshopStyle.BG);
        mine.setBackground(localMode ? WorkshopStyle.ACCENT : WorkshopStyle.LINE);
        mine.setForeground(localMode ? WorkshopStyle.BG : WorkshopStyle.TEXT);
        JPanel top = WorkshopStyle.panel(new BorderLayout(0, 22));
        JPanel intro = WorkshopStyle.panel(new BorderLayout(0, 8));
        intro.add(WorkshopStyle.html("<b>" + (localMode ? "Deine nächste Runde." : "Neue Karten. Neue Abenteuer.") + "</b>", 30, WorkshopStyle.TEXT), BorderLayout.NORTH);
        intro.add(WorkshopStyle.label(localMode ? "Installierte Karten bleiben auch ohne Server spielbar." : "Entdecke Karten aus der Community und bring sie direkt in dein Spiel.", 14, WorkshopStyle.MUTED), BorderLayout.CENTER);
        top.add(intro, BorderLayout.NORTH);
        JPanel controls = WorkshopStyle.panel(new BorderLayout(12, 8));
        JPanel searchArea = WorkshopStyle.panel(new BorderLayout(0, 6));
        searchArea.add(WorkshopStyle.label("SUCHE NACH KARTE, THEMA ODER AUTOR", 10, WorkshopStyle.MUTED), BorderLayout.NORTH);
        searchArea.add(search, BorderLayout.CENTER); controls.add(searchArea, BorderLayout.CENTER);
        JPanel filters = WorkshopStyle.panel(new FlowLayout(FlowLayout.RIGHT, 8, 17));
        filters.add(category); filters.add(refresh); controls.add(filters, BorderLayout.EAST);
        controls.add(count, BorderLayout.SOUTH); top.add(controls, BorderLayout.SOUTH);
        body.add(top, BorderLayout.NORTH);
        JPanel center = WorkshopStyle.panel(new BorderLayout(22, 0)); center.setBorder(new EmptyBorder(20, 0, 0, 0));
        JPanel gridHolder = WorkshopStyle.panel(new BorderLayout()); gridHolder.add(cards, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(gridHolder); scroll.setBorder(null);
        WorkshopStyle.scrollStyle(scroll);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        center.add(scroll, BorderLayout.CENTER);
        details.setPreferredSize(new Dimension(300, 0)); center.add(details, BorderLayout.EAST);
        body.add(center, BorderLayout.CENTER); filter(); revalidate(); repaint();
    }

    private void filter() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        String topic = Objects.toString(category.getSelectedItem(), "Alle Themen");
        List<WorkshopClient.Item> matches = (localMode ? local : remote).stream()
            .filter(i -> topic.equals("Alle Themen") || i.category().equals(topic))
            .filter(i -> (i.title() + " " + i.author() + " " + i.category()).toLowerCase(Locale.ROOT).contains(query)).toList();
        cards.removeAll();
        if (selected == null || matches.stream().noneMatch(i -> i.id().equals(selected.id()))) selected = matches.isEmpty() ? null : matches.get(0);
        for (var item : matches) cards.add(new MapCard(item));
        if (matches.isEmpty()) {
            JLabel empty = WorkshopStyle.html(localMode ? "<b>Noch keine passenden Karten.</b><br>Installiere Karten im Workshop oder ändere den Suchfilter." : "<b>Keine Karten gefunden.</b><br>Aktualisiere den Katalog oder ändere den Suchfilter.", 16, WorkshopStyle.MUTED);
            empty.setBorder(new EmptyBorder(24, 10, 24, 10)); cards.add(empty);
        }
        cards.setPreferredSize(new Dimension(400, Math.max(1, (matches.size() + 1) / 2) * 242));
        count.setText(matches.size() + " KARTEN  /  " + (localMode ? "LOKAL INSTALLIERT" : "WORKSHOP") + "  ·  Alphabetisch");
        detail(); cards.revalidate(); cards.repaint();
    }

    private void detail() {
        details.removeAll(); details.setBorder(new EmptyBorder(20, 22, 20, 22));
        details.setOpaque(true); details.setBackground(WorkshopStyle.PANEL);
        if (selected == null) { details.add(WorkshopStyle.html("<b>Dein nächstes Abenteuer</b><br><br>Wähle eine Karte.<br>Hier erfährst du mehr.", 17, WorkshopStyle.MUTED), BorderLayout.NORTH); return; }
        WorkshopClient.Item item = selected;
        JPanel info = WorkshopStyle.panel(new BorderLayout(0, 16));
        info.add(WorkshopStyle.html("<span style='color:#8adebd'>" + WorkshopStyle.escape(item.category().toUpperCase(Locale.ROOT))
            + "</span><h2>" + WorkshopStyle.escape(item.title()) + "</h2>von " + WorkshopStyle.escape(item.author()), 14, WorkshopStyle.TEXT), BorderLayout.NORTH);
        JTextArea description = new JTextArea(item.description() + "\n\n" + item.questions() + " Quizfragen · " + item.size()
            + " Felder\n\nEnthält eine begehbare Karte und einen Quiz-NPC.\n\n"
            + (installed.contains(item.id()) ? "In deiner Bibliothek.\nAuch offline verfügbar." : "Nach der Installation offline spielbar."));
        description.setEditable(false); description.setLineWrap(true); description.setWrapStyleWord(true);
        description.setFont(WorkshopStyle.font(14, false)); description.setForeground(WorkshopStyle.TEXT);
        description.setBackground(WorkshopStyle.PANEL); description.setBorder(null);
        JScrollPane descriptionScroll = new JScrollPane(description); descriptionScroll.setBorder(null);
        WorkshopStyle.scrollStyle(descriptionScroll);
        descriptionScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScroll.getVerticalScrollBar().setUnitIncrement(16);
        info.add(descriptionScroll, BorderLayout.CENTER);
        details.add(info, BorderLayout.CENTER);
        JPanel actions = WorkshopStyle.panel(new GridLayout(0, 1, 0, 10));
        boolean ready = installed.contains(item.id());
        install = WorkshopStyle.button(ready ? "Karte spielen" : "Herunterladen", "install", true);
        install.setEnabled(!busy);
        install.addActionListener(e -> {
            if (installed.contains(item.id())) play(item.id());
            else work("Karte wird heruntergeladen …", () -> client.install(item.id(), library), id -> {
                loadLocal(); status.setText("Installiert: " + item.title() + " · Jetzt spielen oder später in der Bibliothek öffnen.");
            });
        });
        actions.add(install);
        if (ready) {
            JButton export = WorkshopStyle.button("Als Datei exportieren", "export", false);
            export.addActionListener(e -> export(item)); actions.add(export);
        }
        details.add(actions, BorderLayout.SOUTH); details.revalidate(); details.repaint();
    }

    private void chooseUpload() {
        JFileChooser chooser = new JFileChooser("samples");
        chooser.setFileFilter(new FileNameExtensionFilter("QuizMaster-Karten (*.qmap)", "qmap"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path file = chooser.getSelectedFile().toPath();
        try {
            byte[] bytes = MapPackage.readFile(file); MapPackage map = MapPackage.parse(bytes);
            JPanel preview = WorkshopStyle.panel(new BorderLayout(0, 10));
            preview.add(new JLabel("<html><b>" + WorkshopStyle.escape(map.title()) + "</b><br>von " + WorkshopStyle.escape(map.author())
                + "<br>" + map.width() + " × " + map.height() + " Felder · " + map.questions().size() + " Fragen"
                + "<br><br>Für alle Benutzer dieses Servers veröffentlichen?<br>" + WorkshopStyle.escape(client.address()) + "</html>"));
            if (JOptionPane.showConfirmDialog(this, preview, "Karte veröffentlichen", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            work("Karte wird veröffentlicht …", () -> { client.publish(bytes); return client.list(); }, result -> {
                remote = result; localMode = false; selected = null; search.setText(""); categories(); catalog();
                status.setText("Veröffentlicht: " + map.title() + " · Andere Benutzer sehen die Karte nach dem Aktualisieren.");
            });
        } catch (IOException error) { status.setText("Veröffentlichung abgebrochen: " + error.getMessage()); }
    }

    private void export(WorkshopClient.Item item) {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File(item.id().substring(0, 12) + ".qmap"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path target = chooser.getSelectedFile().toPath();
        if (Files.exists(target) && JOptionPane.showConfirmDialog(this, "Datei ersetzen?", "Export", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { Files.write(target, library.read(item.id())); status.setText("Karte exportiert: " + target.getFileName()); }
        catch (IOException error) { status.setText("Export fehlgeschlagen: " + error.getMessage()); }
    }

    private void play(String id) {
        try {
            MapPackage map = MapPackage.parse(library.read(id));
            showContent(new MapUI(map, () -> {
                QuizService service = new QuizService(map.questions()); service.start();
                showContent(new QuizUI(service, map.title(), map.category()));
            }));
            status.setText("Offline-Spiel · " + map.title());
        } catch (IOException error) { status.setText("Karte konnte nicht geöffnet werden: " + error.getMessage()); }
    }

    private void showContent(JPanel panel) {
        body.removeAll(); JButton back = WorkshopStyle.button("← Zur Bibliothek", "back", false);
        back.addActionListener(e -> { localMode = true; selected = null; catalog(); loadLocal(); });
        JPanel bar = WorkshopStyle.panel(new FlowLayout(FlowLayout.LEFT)); bar.add(back);
        body.add(bar, BorderLayout.NORTH); body.add(panel, BorderLayout.CENTER); revalidate(); repaint();
    }

    private <T> void work(String message, Callable<T> task, Consumer<T> done) {
        if (busy) return;
        busy = true; status.setText(message); controls();
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception { return task.call(); }
            protected void done() {
                busy = false;
                try { done.accept(get()); }
                catch (Exception error) {
                    Throwable reason = error.getCause() == null ? error : error.getCause();
                    status.setText(reason.getMessage() != null && reason.getMessage().startsWith("Server ")
                        ? reason.getMessage() : "Server nicht verfügbar oder Anfrage fehlgeschlagen. Bibliothek bleibt offline nutzbar.");
                    status.putClientProperty("html.disable", Boolean.TRUE);
                    status.setToolTipText(reason.toString());
                }
                controls(); detail();
            }
        }.execute();
    }

    private void controls() {
        refresh.setEnabled(!busy); publish.setEnabled(!busy); discover.setEnabled(!busy); mine.setEnabled(!busy);
        if (install != null) install.setEnabled(!busy);
    }
    boolean isBusy() { return busy; }
    int catalogSize() { return remote.size(); }

    private final class MapCard extends JButton {
        private final WorkshopClient.Item item;
        MapCard(WorkshopClient.Item item) {
            this.item = item; setName("map-" + item.id()); setContentAreaFilled(false); setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(item.title() + ", von " + item.author());
            addActionListener(e -> { selected = item; filter(); });
        }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(WorkshopStyle.PANEL); g.fillRoundRect(0, 0, w, h, 16, 16);
            Graphics2D art = (Graphics2D) g.create(); art.setClip(0, 0, w, 115); WorkshopStyle.cover(art, item.category(), w, 115); art.dispose();
            g.setColor(WorkshopStyle.TEXT); g.setFont(WorkshopStyle.font(18, true)); text(g, item.title(), 16, 146, w - 32);
            g.setFont(WorkshopStyle.font(12, false)); g.setColor(WorkshopStyle.MUTED); text(g, "von " + item.author(), 16, 168, w - 32);
            g.setColor(WorkshopStyle.ACCENT); g.setFont(WorkshopStyle.font(11, true));
            text(g, item.category().toUpperCase(Locale.ROOT) + "  ·  " + item.questions() + " FRAGEN", 16, h - 35, w - 32);
            g.setColor(WorkshopStyle.MUTED); g.setFont(WorkshopStyle.font(11, false));
            g.drawString(installed.contains(item.id()) ? "INSTALLIERT · OFFLINE SPIELBAR" : "KARTE & QUIZ · DETAILS ANSEHEN", 16, h - 16);
            if (hasFocus() || selected != null && selected.id().equals(item.id())) {
                g.setStroke(new BasicStroke(2)); g.setColor(WorkshopStyle.ACCENT); g.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
            }
            g.dispose();
        }
        private void text(Graphics2D g, String text, int x, int y, int max) {
            String display = text;
            while (g.getFontMetrics().stringWidth(display) > max && display.length() > 2) display = display.substring(0, display.length() - 2) + "…";
            g.drawString(display, x, y);
        }
    }
}
