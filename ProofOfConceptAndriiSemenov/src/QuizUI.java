import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/** Presentation layer. All scoring and transitions belong to QuizService. */
public final class QuizUI extends JPanel {
    private static final Color INK = new Color(24, 59, 53);
    private static final Color MUTED = new Color(82, 105, 95);
    private static final Color PAPER = new Color(245, 246, 239);
    private static final Color GREEN = new Color(29, 103, 78);
    private static final Color LINE = new Color(211, 222, 211);
    private static final Color SOFT = new Color(233, 242, 228);
    private final QuizService service;
    private final String title;
    private final String category;
    private final JPanel content = new JPanel(new BorderLayout(0, 22));
    private final JLabel progressText = label("", 13, true, MUTED);
    private final JProgressBar progress = new JProgressBar();
    private int selected = -1;
    private JButton primary;
    private JPanel answers;

    public QuizUI(QuizService service) {
        this(service, "Milas Naturquiz", "Natur & Umwelt");
    }

    public QuizUI(QuizService service, String title, String category) {
        this.service = service;
        this.title = WorkshopStyle.escape(title);
        this.category = WorkshopStyle.escape(category);
        setLayout(new BorderLayout());
        setBackground(PAPER);
        add(sidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout(0, 24));
        main.setBackground(PAPER);
        main.setBorder(new EmptyBorder(30, 32, 22, 32));
        JPanel heading = new JPanel(new BorderLayout(0, 12));
        heading.setOpaque(false);
        heading.add(label("<html>WORKSHOP / " + this.category.toUpperCase(java.util.Locale.ROOT) + "</html>", 12, true, MUTED), BorderLayout.WEST);
        heading.add(progressText, BorderLayout.EAST);
        progress.setMaximum(service.total());
        progress.setForeground(GREEN);
        progress.setBackground(LINE);
        progress.setBorderPainted(false);
        progress.setPreferredSize(new Dimension(100, 5));
        heading.add(progress, BorderLayout.SOUTH);
        main.add(heading, BorderLayout.NORTH);
        content.setOpaque(false);
        main.add(content, BorderLayout.CENTER);
        main.add(label("Eine Antwort pro Frage  ·  Tab zum Navigieren, Leertaste zum Auswählen", 12, false, MUTED), BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);
        render();
    }

    static Font font(int size, boolean bold) {
        return new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size);
    }

    @Override public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paint(g);
        g.dispose();
    }

    private static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(size, bold));
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JLabel paragraph(String text, int size, Color color) {
        return label("<html>" + text + "</html>", size, false, color);
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 24));
        side.setBackground(INK);
        side.setPreferredSize(new Dimension(246, 0));
        side.setBorder(new EmptyBorder(32, 24, 26, 24));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(label("QUIZMASTER", 12, true, new Color(182, 207, 177)));
        top.add(Box.createVerticalStrut(10));
        top.add(label("Wissensinsel", 25, true, Color.WHITE));
        top.add(Box.createVerticalStrut(14));
        top.add(paragraph("Kleine Fragen.<br>Große Entdeckungen.", 15, new Color(214, 228, 211)));
        side.add(top, BorderLayout.NORTH);
        side.add(new Island(), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(label("DEIN LERNPFAD", 11, true, new Color(182, 207, 177)));
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(label("<html>" + title + "</html>", 16, true, Color.WHITE));
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(label(service.total() + " Fragen / Offline spielbar", 13, false, new Color(214, 228, 211)));
        bottom.add(Box.createVerticalStrut(34));
        bottom.add(label("COMMUNITY WORKSHOP", 11, true, new Color(182, 207, 177)));
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private void render() {
        content.removeAll();
        progress.setValue(service.answered());
        progressText.setText(service.answered() + " / " + service.total() + " beantwortet");
        if (service.state() == QuizService.State.READY) renderWelcome();
        else if (service.state() == QuizService.State.FINISHED) renderResult();
        else renderQuestion();
        revalidate();
        repaint();
    }

    private JPanel card() {
        JPanel card = new RoundedPanel(Color.WHITE);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(30, 30, 28, 30));
        return card;
    }

    private JPanel stack() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private void renderWelcome() {
        JPanel card = card();
        JPanel intro = stack();
        intro.add(label("HALLO WELT. HALLO WISSENSINSEL.", 12, true, GREEN));
        intro.add(Box.createVerticalStrut(24));
        intro.add(label("Bereit für eine Entdeckung?", 27, true, INK));
        intro.add(Box.createVerticalStrut(20));
        intro.add(paragraph("Entdecke das Quiz dieser Community-Karte.<br>Wähle zu jeder Frage genau eine Antwort.", 16, MUTED));
        intro.add(Box.createVerticalStrut(32));
        intro.add(label("<html>" + title + "</html>", 22, true, INK));
        intro.add(Box.createVerticalStrut(14));
        intro.add(paragraph("01 &nbsp; Wähle eine Antwort aus.<br><br>02 &nbsp; Prüfe sie und erfahre mehr.<br><br>03 &nbsp; Sammle bis zu " + service.total() + " Punkte.", 16, MUTED));
        card.add(intro, BorderLayout.NORTH);
        primary = button("Quiz starten", "primary", true);
        primary.addActionListener(e -> { service.start(); selected = -1; render(); focusPrimary(); });
        card.add(primary, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
    }

    private void renderQuestion() {
        Question question = service.currentQuestion();
        boolean feedback = service.state() == QuizService.State.FEEDBACK;
        JPanel card = card();
        JPanel intro = stack();
        intro.add(label("FRAGE 0" + (service.index() + 1) + "  /  0" + service.total(), 12, true, GREEN));
        intro.add(Box.createVerticalStrut(14));
        intro.add(label("<html>" + WorkshopStyle.escape(question.text()) + "</html>", 25, true, INK));
        intro.add(Box.createVerticalStrut(10));
        intro.add(label("Wähle genau eine Antwort.", 14, false, MUTED));
        card.add(intro, BorderLayout.NORTH);
        answers = new JPanel(new GridLayout(question.answers().size(), 1, 0, 10));
        answers.setOpaque(false);
        for (int i = 0; i < question.answers().size(); i++) {
            int answer = i;
            String suffix = feedback && i == question.correctIndex() ? "   /   Richtig"
                : feedback && i == selected ? "   /   Deine Antwort" : "";
            JButton option = button((char) ('A' + i) + "    " + question.answers().get(i) + suffix, "answer-" + i, false);
            option.setHorizontalAlignment(SwingConstants.LEFT);
            option.getAccessibleContext().setAccessibleDescription("Antwort " + (i + 1));
            if (feedback) {
                option.setEnabled(false);
                if (i == question.correctIndex()) option.setBackground(SOFT);
                else if (i == selected) option.setBackground(new Color(252, 235, 214));
            }
            option.addActionListener(e -> {
                selected = answer;
                for (Component component : answers.getComponents()) {
                    JButton item = (JButton) component;
                    boolean chosen = item == option;
                    item.setBackground(chosen ? SOFT : PAPER);
                    int itemIndex = answers.getComponentZOrder(item);
                    item.setText((char) ('A' + itemIndex) + "    " + question.answers().get(itemIndex)
                        + (chosen ? "   /   Ausgewählt" : ""));
                    item.getAccessibleContext().setAccessibleDescription(chosen ? "Ausgewählt" : "Nicht ausgewählt");
                }
                primary.setEnabled(true);
            });
            answers.add(option);
        }
        card.add(answers, BorderLayout.CENTER);
        JPanel actions = new JPanel(new BorderLayout(0, 12));
        actions.setOpaque(false);
        JLabel message;
        if (feedback) {
            message = paragraph("<b>" + (service.lastCorrect() ? "Richtig! +1 Punkt" : "Diesmal leider nicht.")
                + "</b><br>" + WorkshopStyle.escape(question.explanation()), 14, service.lastCorrect() ? GREEN : new Color(135, 72, 20));
        } else {
            message = paragraph("Nimm dir Zeit. Du kannst deine Auswahl vor dem Prüfen ändern.", 13, MUTED);
        }
        message.setName("feedback");
        message.setPreferredSize(new Dimension(0, 64));
        actions.add(message, BorderLayout.CENTER);
        primary = button(feedback ? (service.index() + 1 == service.total() ? "Ergebnis ansehen" : "Nächste Frage") : "Antwort prüfen", "primary", true);
        primary.setEnabled(feedback || selected >= 0);
        primary.addActionListener(e -> {
            if (service.state() == QuizService.State.QUESTION) service.submit(selected);
            else { service.next(); selected = -1; }
            render();
            focusPrimary();
        });
        actions.add(primary, BorderLayout.SOUTH);
        card.add(actions, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
    }

    private void renderResult() {
        JPanel card = card();
        JPanel result = stack();
        result.add(label("LERNPFAD ABGESCHLOSSEN", 12, true, GREEN));
        result.add(Box.createVerticalStrut(28));
        result.add(label("Gut entdeckt!", 32, true, INK));
        result.add(Box.createVerticalStrut(24));
        JLabel score = label(service.score() + " / " + service.total(), 68, true, GREEN);
        score.setName("score");
        result.add(score);
        result.add(label("<html>Punkte in " + title + "</html>", 17, false, MUTED));
        result.add(Box.createVerticalStrut(30));
        result.add(paragraph(service.score() == service.total()
            ? "Alle Antworten richtig. Du kennst dich mit diesem Thema schon gut aus!"
            : "Mit jeder Frage lernst du etwas dazu.<br>Versuche es noch einmal und entdecke dein neues Wissen.", 16, MUTED));
        result.add(Box.createVerticalStrut(24));
        result.add(label("Dein Ergebnis gilt für diesen Durchlauf.", 13, false, MUTED));
        card.add(result, BorderLayout.NORTH);
        primary = button("Noch einmal spielen", "primary", true);
        primary.addActionListener(e -> { service.start(); selected = -1; render(); focusPrimary(); });
        card.add(primary, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
    }

    private void focusPrimary() {
        if (primary.isEnabled()) primary.requestFocusInWindow();
        else if (answers != null) answers.getComponent(0).requestFocusInWindow();
    }

    private JButton button(String text, String name, boolean accent) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getBackground();
                if (accent && !isEnabled()) fill = LINE;
                else if (isEnabled() && getModel().isRollover()) fill = fill.darker();
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (hasFocus()) {
                    g.setColor(new Color(201, 151, 47));
                    g.setStroke(new BasicStroke(3));
                    g.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 12, 12);
                }
                super.paintComponent(g);
                g.dispose();
            }
        };
        // Keep feedback answers legible even while editing is locked.
        button.setUI(new BasicButtonUI() {
            @Override protected void paintText(Graphics g, AbstractButton item, Rectangle bounds, String value) {
                g.setColor(accent && !item.isEnabled() ? MUTED : item.getForeground());
                BasicGraphicsUtils.drawStringUnderlineCharAt(g, value, item.getDisplayedMnemonicIndex(),
                    bounds.x, bounds.y + g.getFontMetrics().getAscent());
            }
        });
        button.setName(name);
        button.setFont(font(15, true));
        button.setBackground(accent ? GREEN : PAPER);
        button.setForeground(accent ? Color.WHITE : INK);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(14, 18, 14, 18));
        button.setPreferredSize(new Dimension(100, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static final class RoundedPanel extends JPanel {
        RoundedPanel(Color background) { setBackground(background); setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g.dispose();
        }
    }

    /** Small original Java2D illustration; no image downloads or runtime assets. */
    private static final class Island extends JPanel {
        Island() { setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(getWidth() / 2.0 - 95, getHeight() / 2.0 - 105);
            g.setColor(new Color(239, 199, 107));
            g.fillOval(115, 2, 48, 48);
            g.setColor(new Color(58, 100, 86));
            g.fillOval(0, 125, 190, 63);
            g.setColor(new Color(148, 179, 119));
            g.fillOval(8, 103, 174, 65);
            g.setColor(new Color(224, 207, 158));
            g.fillRoundRect(85, 126, 24, 34, 14, 14);
            g.setColor(new Color(117, 94, 66));
            g.fillRect(46, 69, 10, 64);
            g.fillRect(136, 80, 8, 56);
            g.setColor(new Color(84, 139, 98));
            g.fillOval(21, 40, 61, 66);
            g.setColor(new Color(111, 157, 104));
            g.fillOval(116, 60, 48, 52);
            g.setColor(new Color(250, 235, 195));
            g.fillRoundRect(83, 83, 35, 40, 6, 6);
            g.setColor(new Color(202, 126, 78));
            g.fillPolygon(new int[]{77, 100, 124}, new int[]{88, 65, 88}, 3);
            g.setColor(INK);
            g.fillRoundRect(96, 103, 10, 20, 3, 3);
            g.dispose();
        }
    }
}
