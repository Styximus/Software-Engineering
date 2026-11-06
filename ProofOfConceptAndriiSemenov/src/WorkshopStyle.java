import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

final class WorkshopStyle {
    static final Color BG = new Color(17, 23, 33), PANEL = new Color(26, 34, 47), LINE = new Color(48, 61, 78);
    static final Color TEXT = new Color(237, 241, 246), MUTED = new Color(162, 178, 197), ACCENT = new Color(138, 222, 189);
    static Font font(int size, boolean bold) { return new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size); }
    static String escape(String text) { return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
    static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text); label.setForeground(color); label.setFont(font(size, false)); return label;
    }
    static JLabel html(String text, int size, Color color) { return label("<html>" + text + "</html>", size, color); }
    static JPanel panel(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    static void scrollStyle(JScrollPane scroll) {
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(9, 0));
        bar.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = LINE.brighter(); trackColor = PANEL; }
            protected JButton createDecreaseButton(int direction) { return blank(); }
            protected JButton createIncreaseButton(int direction) { return blank(); }
            private JButton blank() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        });
    }
    static JButton button(String text, String name, boolean accent) {
        JButton b = new JButton(text);
        b.setName(name); b.setFont(font(13, true));
        b.setBackground(accent ? ACCENT : LINE); b.setForeground(accent ? BG : TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent ? ACCENT : LINE), new EmptyBorder(11, 15, 11, 15)));
        b.setFocusPainted(true); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    static Color tint(String category) {
        return switch (category) {
            case "Natur" -> new Color(54, 111, 92);
            case "Weltraum" -> new Color(76, 70, 132);
            case "Technik" -> new Color(47, 104, 135);
            default -> new Color(137, 88, 60);
        };
    }
    static void cover(Graphics2D g, String category, int width, int height) {
        g.setPaint(new GradientPaint(0, 0, tint(category).brighter(), width, height, tint(category).darker()));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(255, 255, 255, 12));
        for (int x = 0; x < width; x += 24) g.drawLine(x, 0, x, height);
        for (int y = 0; y < height; y += 24) g.drawLine(0, y, width, y);
        g.setColor(new Color(245, 218, 160)); g.fillOval(width - 88, 24, 45, 45);
        if (category.equals("Weltraum")) {
            g.setColor(new Color(220, 203, 253)); g.drawOval(width - 110, 33, 90, 25);
            for (int i = 0; i < 20; i++) g.fillOval((i * 47 + 13) % width, (i * 29 + 8) % height, 2, 2);
        } else {
            g.setColor(new Color(0, 0, 0, 35));
            g.fillPolygon(new int[]{0, width / 3, width * 2 / 3, width}, new int[]{height, height / 3, height, height}, 4);
            g.setColor(new Color(0, 0, 0, 50));
            g.fillPolygon(new int[]{width / 3, width * 3 / 4, width, width}, new int[]{height, height / 2, height * 3 / 4, height}, 4);
            if (category.equals("Technik")) {
                g.setFont(font(42, true)); g.setColor(new Color(218, 246, 248)); g.drawString("{ }", 27, 82);
            }
        }
    }
}
