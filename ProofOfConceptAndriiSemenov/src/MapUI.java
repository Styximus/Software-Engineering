import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Plays the installed map's terrain and NPC coordinates, not a hard-coded preview. */
public final class MapUI extends JPanel {
    private final MapPackage map;
    private int x, y;
    private final JButton interact = WorkshopStyle.button("Mit NPC sprechen [E]", "interact", true);
    private final JLabel hint = WorkshopStyle.label("", 14, WorkshopStyle.MUTED);

    public MapUI(MapPackage map, Runnable openQuiz) {
        this.map = map; x = map.spawnX(); y = map.spawnY();
        setLayout(new BorderLayout(0, 18)); setBackground(WorkshopStyle.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));
        JPanel header = WorkshopStyle.panel(new BorderLayout());
        header.add(WorkshopStyle.html("<b>" + WorkshopStyle.escape(map.title()) + "</b> · " + WorkshopStyle.escape(map.author()), 22, WorkshopStyle.TEXT), BorderLayout.WEST);
        header.add(WorkshopStyle.label("LOKAL INSTALLIERTE KARTE", 11, WorkshopStyle.ACCENT), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(new Board(), BorderLayout.CENTER);
        JPanel footer = WorkshopStyle.panel(new BorderLayout(15, 10));
        footer.add(hint, BorderLayout.CENTER); footer.add(interact, BorderLayout.EAST);
        footer.add(WorkshopStyle.label("Pfeiltasten / WASD: bewegen     E / Leertaste: sprechen     Wasser, Bäume und Felsen blockieren den Weg.", 12, WorkshopStyle.MUTED), BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        interact.addActionListener(e -> { if (nearNpc()) openQuiz.run(); });
        bind("LEFT", () -> movePlayer(-1, 0)); bind("A", () -> movePlayer(-1, 0));
        bind("RIGHT", () -> movePlayer(1, 0)); bind("D", () -> movePlayer(1, 0));
        bind("UP", () -> movePlayer(0, -1)); bind("W", () -> movePlayer(0, -1));
        bind("DOWN", () -> movePlayer(0, 1)); bind("S", () -> movePlayer(0, 1));
        bind("E", () -> interact.doClick(0)); bind("SPACE", () -> interact.doClick(0));
        update();
    }
    private void bind(String key, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() { public void actionPerformed(ActionEvent e) { action.run(); } });
    }
    void movePlayer(int dx, int dy) {
        int nextX = x + dx, nextY = y + dy;
        if (map.walkable(nextX, nextY) && !(nextX == map.npcX() && nextY == map.npcY())) { x = nextX; y = nextY; }
        update();
    }
    boolean nearNpc() { return Math.abs(x - map.npcX()) + Math.abs(y - map.npcY()) == 1; }
    int playerX() { return x; }
    int playerY() { return y; }
    private void update() {
        interact.setEnabled(nearNpc());
        hint.setText(nearNpc() ? map.npcName() + " wartet auf dich. Starte das Quiz!" : "Gehe zu " + map.npcName() + " (goldene Figur), um das Quiz zu starten.");
        hint.putClientProperty("html.disable", Boolean.TRUE);
        repaint();
    }
    private final class Board extends JPanel {
        Board() { setOpaque(false); }
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.max(1, Math.min((getWidth() - 32) / map.width(), (getHeight() - 32) / map.height()));
            g.translate((getWidth() - size * map.width()) / 2, (getHeight() - size * map.height()) / 2);
            for (int row = 0; row < map.height(); row++) for (int col = 0; col < map.width(); col++) {
                char tile = map.tiles().get(row).charAt(col);
                g.setColor(tile == '~' ? new Color(48, 92, 119) : new Color(81 + (row + col) % 2 * 5, 120, 92));
                g.fillRect(col * size, row * size, size, size);
                if (tile == 'T') {
                    g.setColor(new Color(32, 76, 58)); g.fillOval(col * size + size / 6, row * size + size / 8, size * 2 / 3, size * 3 / 4);
                } else if (tile == '#') {
                    g.setColor(new Color(135, 144, 142)); g.fillRoundRect(col * size + 5, row * size + 7, size - 10, size - 14, 10, 10);
                } else if (tile == '~') {
                    g.setColor(new Color(82, 137, 163)); g.drawLine(col * size + 8, row * size + size / 2, (col + 1) * size - 8, row * size + size / 2);
                }
            }
            pawn(g, map.npcX(), map.npcY(), size, new Color(245, 200, 103), "NPC");
            pawn(g, x, y, size, new Color(213, 244, 246), "DU");
            g.dispose();
        }
        private void pawn(Graphics2D g, int px, int py, int s, Color color, String tag) {
            g.setColor(new Color(0, 0, 0, 60)); g.fillOval(px * s + s / 5, py * s + s * 2 / 3, s * 3 / 5, s / 4);
            g.setColor(color); g.fillOval(px * s + s / 3, py * s + s / 8, s / 3, s / 3);
            g.fillRoundRect(px * s + s / 4, py * s + s / 2, s / 2, s / 3, 8, 8);
            g.setFont(WorkshopStyle.font(10, true)); g.setColor(WorkshopStyle.BG);
            g.drawString(tag, px * s + s / 4, py * s + s * 3 / 4);
        }
    }
}
