package de.quizmaster;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Local assets and tokens from Figma YE8YbEpodG8RoXhazn7OVf, NPC dialog 3:2570. */
final class QuizTheme {
    static final Color CREAM = Color.decode("#fff9ea"), INK = Color.decode("#253d34"), GREEN = Color.decode("#28725c");
    static final Color MUTED = Color.decode("#6d7b6d"), LINE = Color.decode("#d8ddc9"), SOFT = Color.decode("#e4edda");
    static final Color FIELD = Color.decode("#f7f0dc"), ERROR = Color.decode("#934a31");
    private static final java.util.Map<String, Font> FONTS = new HashMap<>();
    private static final java.util.Map<String, BufferedImage> IMAGES = new HashMap<>();

    static Font font(String name, int size) {
        return FONTS.computeIfAbsent(name, key -> {
            try (InputStream stream = resource("fonts/" + key + ".ttf")) {
                return Font.createFont(Font.TRUETYPE_FONT, stream);
            } catch (Exception error) { throw new IllegalStateException("Schrift fehlt: " + key, error); }
        }).deriveFont((float) size);
    }
    static Font body(int size) { return font("NunitoSans-Regular", size); }
    static Font bold(int size) { return font("NunitoSans-ExtraBold", size); }
    static Font heading(int size) { return font("Fraunces-SemiBold", size); }
    static InputStream resource(String name) throws IOException {
        InputStream input = QuizTheme.class.getResourceAsStream("/ui/" + name);
        if (input == null) throw new IOException("Ressource fehlt: " + name);
        return input;
    }
    static BufferedImage image(String name) {
        return IMAGES.computeIfAbsent(name, key -> {
            try (InputStream input = resource("figma/" + key + ".png")) {
                BufferedImage image = ImageIO.read(input);
                if (image == null) throw new IOException("Ungültige Bilddatei");
                return image;
            } catch (IOException error) { throw new IllegalStateException("Bild fehlt: " + key, error); }
        });
    }
    static Icon icon(String name, int width, int height) {
        BufferedImage image = image(name);
        return new Icon() {
            public int getIconWidth() { return width; }
            public int getIconHeight() { return height; }
            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(image, x, y, width, height, null); g.dispose();
            }
        };
    }
    static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text); label.putClientProperty("html.disable", true);
        label.setFont(font); label.setForeground(color); return label;
    }
    static JTextArea text(String value, Font font, Color color) {
        JTextArea area = new JTextArea(value); area.setFont(font); area.setForeground(color); area.setOpaque(false);
        area.setEditable(false); area.setFocusable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setBorder(null); return area;
    }
    static JPanel clear(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    static JButton button(String text, String name, boolean primary) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = primary ? GREEN : FIELD;
                if (!isEnabled()) background = LINE;
                else if (getModel().isRollover()) background = primary ? GREEN.darker() : SOFT;
                g.setColor(background); g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
                g.setColor(hasFocus() ? Color.decode("#ba8c3b") : primary ? GREEN.darker() : LINE);
                g.setStroke(new BasicStroke(hasFocus() ? 3 : 1)); g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 22, 22);
                super.paintComponent(g); g.dispose();
            }
        };
        b.setUI(new BasicButtonUI() {
            @Override protected void paintText(Graphics graphics, AbstractButton button, Rectangle bounds, String text) {
                if (button.isEnabled()) { super.paintText(graphics, button, bounds, text); return; }
                graphics.setColor(MUTED);
                graphics.drawString(text, bounds.x, bounds.y + graphics.getFontMetrics().getAscent());
            }
        }); b.setName(name); b.setFont(bold(16));
        b.setForeground(primary ? CREAM : INK); b.setOpaque(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 20, 12, 20)); b.setPreferredSize(new Dimension(210, 52));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    static void styleScrollPane(JScrollPane pane) {
        JScrollBar bar = pane.getVerticalScrollBar(); bar.setPreferredSize(new Dimension(10, 0));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = LINE; trackColor = CREAM; }
            @Override protected JButton createDecreaseButton(int orientation) { return emptyButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return emptyButton(); }
            private JButton emptyButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        });
    }
    static final class Card extends JPanel {
        private final Color color;
        private final int radius;
        Card(LayoutManager layout, Color color, int radius) { super(layout); this.color = color; this.radius = radius; setOpaque(false); }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(31, 72, 52, 25)); g.fillRoundRect(0, 4, getWidth(), getHeight() - 4, radius * 2, radius * 2);
            g.setColor(color); g.fillRoundRect(0, 0, getWidth(), getHeight() - 4, radius * 2, radius * 2);
            g.dispose(); super.paintComponent(graphics);
        }
    }
    static void paintCover(Graphics2D g, Image image, int x, int y, int width, int height) {
        int iw = image.getWidth(null), ih = image.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) width / iw, (double) height / ih);
        int w = (int)Math.ceil(iw * scale), h = (int)Math.ceil(ih * scale);
        Graphics2D copy = (Graphics2D) g.create(); copy.clipRect(x, y, width, height);
        copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        copy.drawImage(image, x + (width - w) / 2, y + (height - h) / 2, w, h, null); copy.dispose();
    }
}
