
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private JButton primary;
    private JPanel answers;

    private String selectedAnswer;
    private final Set<Integer> selectedIndices = new LinkedHashSet<>();

    private JTextField textAnswer;
    private JTextField[] clozeAnswers;

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

        heading.add(
                label("<html>WORKSHOP / "
                        + this.category.toUpperCase(java.util.Locale.ROOT)
                        + "</html>", 12, true, MUTED),
                BorderLayout.WEST
        );

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

        main.add(
                label("Tab zum Navigieren, Leertaste zum Auswählen",
                        12, false, MUTED),
                BorderLayout.SOUTH
        );

        add(main, BorderLayout.CENTER);

        render();
    }

    // ---------------------------------------------------------
    // General helpers
    // ---------------------------------------------------------

    static Font font(int size, boolean bold) {
        return new Font(
                "SansSerif",
                bold ? Font.BOLD : Font.PLAIN,
                size
        );
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        super.paint(g);
        g.dispose();
    }

    private static JLabel label(
            String text, int size, boolean bold, Color color
    ) {
        JLabel label = new JLabel(text);
        label.setFont(font(size, bold));
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JLabel paragraph(
            String text, int size, Color color
    ) {
        return label("<html>" + text + "</html>", size, false, color);
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

    private void resetAnswer() {
        selectedAnswer = null;
        selectedIndices.clear();
        textAnswer = null;
        clozeAnswers = null;
    }

    private void render() {
        content.removeAll();

        progress.setValue(service.answered());
        progressText.setText(
                service.answered() + " / " + service.total() + " beantwortet"
        );

        if (service.state() == QuizService.State.READY) {
            renderWelcome();
        } else if (service.state() == QuizService.State.FINISHED) {
            renderResult();
        } else {
            renderQuestion();
        }

        revalidate();
        repaint();
    }

    // ---------------------------------------------------------
    // Sidebar
    // ---------------------------------------------------------

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 24));
        side.setBackground(INK);
        side.setPreferredSize(new Dimension(246, 0));
        side.setBorder(new EmptyBorder(32, 24, 26, 24));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        top.add(label("QUIZMASTER", 12, true,
                new Color(182, 207, 177)));
        top.add(Box.createVerticalStrut(10));
        top.add(label("Wissensinsel", 25, true, Color.WHITE));
        top.add(Box.createVerticalStrut(14));
        top.add(paragraph(
                "Kleine Fragen.<br>Große Entdeckungen.",
                15,
                new Color(214, 228, 211)
        ));

        side.add(top, BorderLayout.NORTH);
        side.add(new Island(), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(label("DEIN LERNPFAD", 11, true,
                new Color(182, 207, 177)));
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(label("<html>" + title + "</html>",
                16, true, Color.WHITE));
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(label(
                service.total() + " Fragen / Offline spielbar",
                13, false, new Color(214, 228, 211)
        ));
        bottom.add(Box.createVerticalStrut(34));
        bottom.add(label("COMMUNITY WORKSHOP", 11, true,
                new Color(182, 207, 177)));

        side.add(bottom, BorderLayout.SOUTH);

        return side;
    }

    // ---------------------------------------------------------
    // Welcome screen
    // ---------------------------------------------------------

    private void renderWelcome() {
        JPanel card = card();
        JPanel intro = stack();

        intro.add(label(
                "HALLO WELT. HALLO WISSENSINSEL.",
                12, true, GREEN
        ));
        intro.add(Box.createVerticalStrut(24));
        intro.add(label(
                "Bereit für eine Entdeckung?",
                27, true, INK
        ));
        intro.add(Box.createVerticalStrut(20));
        intro.add(paragraph(
                "Entdecke das Quiz dieser Community-Karte.<br>"
                        + "Beantworte die verschiedenen Fragen.",
                16, MUTED
        ));
        intro.add(Box.createVerticalStrut(32));
        intro.add(label("<html>" + title + "</html>",
                22, true, INK));
        intro.add(Box.createVerticalStrut(14));
        intro.add(paragraph(
                "01 &nbsp; Lies die Frage aufmerksam.<br><br>"
                        + "02 &nbsp; Gib deine Antwort ein.<br><br>"
                        + "03 &nbsp; Erfahre mehr und sammle Punkte.",
                16, MUTED
        ));

        card.add(intro, BorderLayout.NORTH);

        primary = button("Quiz starten", "primary", true);

        primary.addActionListener(e -> {
            service.start();
            resetAnswer();
            render();
            focusPrimary();
        });

        card.add(primary, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------
    // Question screen
    // ---------------------------------------------------------

    private void renderQuestion() {
        Question question = service.currentQuestion();
        boolean feedback = service.state() == QuizService.State.FEEDBACK;

        JPanel card = card();

        JPanel intro = stack();
        intro.add(label(
                "FRAGE " + (service.index() + 1)
                        + " / " + service.total(),
                12, true, GREEN
        ));
        intro.add(Box.createVerticalStrut(14));
        String displayText = question.getText();

        if (question.getType() == QuestionType.CLOZE_TEST) {
            displayText = displayText.replaceAll("\\{\\d+\\}", "_____");
        }

        intro.add(label(
                "<html>" + WorkshopStyle.escape(displayText) + "</html>",
                25, true, INK
        ));
        intro.add(Box.createVerticalStrut(10));
        intro.add(label(
                question.getPoints() + " Punkte",
                14, false, MUTED
        ));

        card.add(intro, BorderLayout.NORTH);

        answers = new JPanel(new BorderLayout());
        answers.setOpaque(false);

        switch (question.getType()) {
            case MULTIPLE_CHOICE ->
                    renderMultipleChoice(
                            (MultipleChoiceQuestion) question, feedback
                    );

            case TRUE_FALSE ->
                    renderTrueFalse(feedback);

            case TEXT ->
                    renderTextQuestion(feedback);

            case CLOZE_TEST ->
                    renderClozeTest(question, feedback);
        }

        card.add(answers, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(0, 12));
        actions.setOpaque(false);

        JLabel message;

        if (feedback) {
            String resultText = service.lastCorrect()
                    ? "Richtig! +" + question.getPoints() + " Punkte"
                    : "Diesmal leider nicht.";

            message = paragraph(
                    "<b>" + resultText + "</b><br>"
                            + WorkshopStyle.escape(question.getExplanation()),
                    14,
                    service.lastCorrect() ? GREEN : new Color(135, 72, 20)
            );
        } else {
            message = paragraph(
                    "Nimm dir Zeit und überprüfe deine Antwort, "
                            + "bevor du sie abschickst.",
                    13, MUTED
            );
        }

        message.setName("feedback");
        message.setPreferredSize(new Dimension(0, 64));
        actions.add(message, BorderLayout.CENTER);

        primary = button(
                feedback
                        ? (service.index() + 1 == service.total()
                           ? "Ergebnis ansehen"
                           : "Nächste Frage")
                        : "Antwort prüfen",
                "primary",
                true
        );

        primary.setEnabled(feedback || hasAnswer());

        primary.addActionListener(e -> {
            if (service.state() == QuizService.State.QUESTION) {
                service.submit(selectedAnswer);
            } else {
                service.next();
                resetAnswer();
            }

            render();
            focusPrimary();
        });

        actions.add(primary, BorderLayout.SOUTH);
        card.add(actions, BorderLayout.SOUTH);

        content.add(card, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------
    // Multiple Choice
    // ---------------------------------------------------------

    private void renderMultipleChoice(
            MultipleChoiceQuestion question,
            boolean feedback
    ) {
        List<String> options = question.getAnswers();

        answers = new JPanel(
                new GridLayout(options.size(), 1, 0, 10)
        );
        answers.setOpaque(false);

        for (int i = 0; i < options.size(); i++) {
            final int index = i;

            boolean chosen = selectedIndices.contains(index);

            JButton option = button(
                    (char) ('A' + i) + "    " + options.get(i)
                            + (chosen ? "   /   Ausgewählt" : ""),
                    "answer-" + i,
                    false
            );

            option.setHorizontalAlignment(SwingConstants.LEFT);
            option.setBackground(chosen ? SOFT : PAPER);
            option.setEnabled(!feedback);

            option.addActionListener(e -> {
                if (selectedIndices.contains(index)) {
                    selectedIndices.remove(index);
                } else {
                    selectedIndices.add(index);
                }

                updateMultipleChoiceAnswer();

                for (Component component : answers.getComponents()) {
                    JButton item = (JButton) component;
                    int itemIndex = answers.getComponentZOrder(item);
                    boolean isSelected =
                            selectedIndices.contains(itemIndex);

                    item.setBackground(isSelected ? SOFT : PAPER);
                    item.setText(
                            (char) ('A' + itemIndex) + "    "
                                    + options.get(itemIndex)
                                    + (isSelected ? "   /   Ausgewählt" : "")
                    );
                }

                updateSubmitButton();
            });

            answers.add(option);
        }
    }

    private void updateMultipleChoiceAnswer() {
        selectedAnswer = selectedIndices.stream()
                .sorted()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
    }

    // ---------------------------------------------------------
    // True / False
    // ---------------------------------------------------------

    private void renderTrueFalse(boolean feedback) {
        JPanel panel = new JPanel(
                new GridLayout(2, 1, 0, 12)
        );
        panel.setOpaque(false);

        addChoiceButton(panel, "Wahr", "true", feedback);
        addChoiceButton(panel, "Falsch", "false", feedback);

        answers.add(panel, BorderLayout.NORTH);
    }

    private void addChoiceButton(
            JPanel panel,
            String text,
            String value,
            boolean feedback
    ) {
        boolean chosen = value.equals(selectedAnswer);

        JButton option = button(
                text + (chosen ? "   /   Ausgewählt" : ""),
                "answer-" + value,
                false
        );

        option.setHorizontalAlignment(SwingConstants.LEFT);
        option.setBackground(chosen ? SOFT : PAPER);
        option.setEnabled(!feedback);

        option.addActionListener(e -> {
            selectedAnswer = value;

            for (Component component : panel.getComponents()) {
                JButton item = (JButton) component;
                boolean isSelected =
                        item.getName().equals("answer-" + value);

                item.setBackground(
                        isSelected ? SOFT : PAPER
                );

                String itemText = item.getName().equals("answer-true")
                        ? "Wahr" : "Falsch";

                item.setText(
                        itemText + (isSelected ? "   /   Ausgewählt" : "")
                );
            }

            updateSubmitButton();
        });

        panel.add(option);
    }

    // ---------------------------------------------------------
    // Text question
    // ---------------------------------------------------------

    private void renderTextQuestion(boolean feedback) {
        JPanel panel = stack();

        panel.add(label("Deine Antwort:", 14, true, INK));
        panel.add(Box.createVerticalStrut(10));

        textAnswer = new JTextField();
        textAnswer.setFont(font(16, false));
        textAnswer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1),
                new EmptyBorder(12, 12, 12, 12)
        ));
        textAnswer.setEnabled(!feedback);

        if (selectedAnswer != null) {
            textAnswer.setText(selectedAnswer);
        }

        textAnswer.getDocument().addDocumentListener(
                new DocumentListener() {
                    private void changed() {
                        selectedAnswer = textAnswer.getText();
                        updateSubmitButton();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        changed();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        changed();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        changed();
                    }
                }
        );

        panel.add(textAnswer);
        answers.add(panel, BorderLayout.NORTH);
    }

    // ---------------------------------------------------------
    // Cloze test
    // ---------------------------------------------------------

    private void renderClozeTest(
            Question question,
            boolean feedback
    ) {
        JPanel panel = stack();

        String questionText = question.getText();

        Matcher matcher = Pattern.compile("\\{\\d+\\}")
                .matcher(questionText);

        List<String> parts = new ArrayList<>();
        int lastPosition = 0;

        while (matcher.find()) {
            parts.add(questionText.substring(
                    lastPosition, matcher.start()
            ));
            lastPosition = matcher.end();
        }

        parts.add(questionText.substring(lastPosition));

        int numberOfGaps = parts.size() - 1;

        clozeAnswers = new JTextField[numberOfGaps];

        panel.add(label("Fülle die fehlenden Wörter aus:",
                14, true, INK));
        panel.add(Box.createVerticalStrut(12));

        for (int i = 0; i < numberOfGaps; i++) {
            panel.add(paragraph(
                    WorkshopStyle.escape(parts.get(i)),
                    15, INK
            ));

            JTextField field = new JTextField();
            field.setFont(font(15, false));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE, 1),
                    new EmptyBorder(9, 10, 9, 10)
            ));
            field.setEnabled(!feedback);

            clozeAnswers[i] = field;

            if (selectedAnswer != null) {
                String[] previous = selectedAnswer.split(";", -1);
                if (i < previous.length) {
                    field.setText(previous[i]);
                }
            }

            field.getDocument().addDocumentListener(
                    new DocumentListener() {
                        private void changed() {
                            updateClozeAnswer();
                            updateSubmitButton();
                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            changed();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            changed();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            changed();
                        }
                    }
            );

            panel.add(field);
            panel.add(Box.createVerticalStrut(8));
        }

        panel.add(paragraph(
                WorkshopStyle.escape(parts.get(parts.size() - 1)),
                15, INK
        ));

        answers.add(panel, BorderLayout.NORTH);
    }

    private void updateClozeAnswer() {
        if (clozeAnswers == null) {
            return;
        }

        List<String> values = new ArrayList<>();

        for (JTextField field : clozeAnswers) {
            values.add(field.getText().trim());
        }

        selectedAnswer = String.join(";", values);
    }

    // ---------------------------------------------------------
    // Answer state
    // ---------------------------------------------------------

    private boolean hasAnswer() {
        if (clozeAnswers != null) {
            for (JTextField field : clozeAnswers) {
                if (field.getText().isBlank()) {
                    return false;
                }
            }
            return clozeAnswers.length > 0;
        }

        return selectedAnswer != null && !selectedAnswer.isBlank();
    }

    private void updateSubmitButton() {
        if (primary != null
                && service.state() == QuizService.State.QUESTION) {
            primary.setEnabled(hasAnswer());
        }
    }

    // ---------------------------------------------------------
    // Result screen
    // ---------------------------------------------------------

    private void renderResult() {
        JPanel card = card();
        JPanel result = stack();

        result.add(label(
                "LERNPFAD ABGESCHLOSSEN",
                12, true, GREEN
        ));
        result.add(Box.createVerticalStrut(28));
        result.add(label("Gut entdeckt!", 32, true, INK));
        result.add(Box.createVerticalStrut(24));

        JLabel score = label(
                service.score() + " / " + service.maxScore(),
                68, true, GREEN
        );
        score.setName("score");

        result.add(score);
        result.add(label(
                "<html>Punkte in " + title + "</html>",
                17, false, MUTED
        ));
        result.add(Box.createVerticalStrut(30));

        result.add(paragraph(
                service.score() == service.maxScore()
                        ? "Alle Antworten richtig. Du kennst dich mit "
                          + "diesem Thema schon gut aus!"
                        : "Mit jeder Frage lernst du etwas dazu.<br>"
                          + "Versuche es noch einmal und entdecke "
                          + "dein neues Wissen.",
                16, MUTED
        ));

        result.add(Box.createVerticalStrut(24));
        result.add(label(
                "Dein Ergebnis gilt für diesen Durchlauf.",
                13, false, MUTED
        ));

        card.add(result, BorderLayout.NORTH);

        primary = button(
                "Noch einmal spielen",
                "primary",
                true
        );

        primary.addActionListener(e -> {
            service.start();
            resetAnswer();
            render();
            focusPrimary();
        });

        card.add(primary, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------
    // Focus handling
    // ---------------------------------------------------------

    private void focusPrimary() {
        if (primary != null && primary.isEnabled()) {
            primary.requestFocusInWindow();
        } else if (answers != null
                && answers.getComponentCount() > 0) {
            answers.getComponent(0).requestFocusInWindow();
        }
    }

    // ---------------------------------------------------------
    // Button styling
    // ---------------------------------------------------------

    private JButton button(
            String text,
            String name,
            boolean accent
    ) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color fill = getBackground();

                if (accent && !isEnabled()) {
                    fill = LINE;
                } else if (isEnabled()
                        && getModel().isRollover()) {
                    fill = fill.darker();
                }

                g.setColor(fill);
                g.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 14, 14
                );

                if (hasFocus()) {
                    g.setColor(new Color(201, 151, 47));
                    g.setStroke(new BasicStroke(3));
                    g.drawRoundRect(
                            3, 3,
                            getWidth() - 7,
                            getHeight() - 7,
                            12, 12
                    );
                }

                super.paintComponent(g);
                g.dispose();
            }
        };

        button.setUI(new BasicButtonUI() {
            @Override
            protected void paintText(
                    Graphics g,
                    AbstractButton item,
                    Rectangle bounds,
                    String value
            ) {
                g.setColor(
                        accent && !item.isEnabled()
                                ? MUTED
                                : item.getForeground()
                );

                BasicGraphicsUtils.drawStringUnderlineCharAt(
                        g,
                        value,
                        item.getDisplayedMnemonicIndex(),
                        bounds.x,
                        bounds.y + g.getFontMetrics().getAscent()
                );
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
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    // ---------------------------------------------------------
    // Rounded card
    // ---------------------------------------------------------

    private static final class RoundedPanel extends JPanel {

        RoundedPanel(Color background) {
            setBackground(background);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(getBackground());
            g.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 24, 24
            );

            g.dispose();
        }
    }

    // ---------------------------------------------------------
    // Island illustration
    // ---------------------------------------------------------

    /** Small original Java2D illustration; no external assets. */
    private static final class Island extends JPanel {

        Island() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.translate(
                    getWidth() / 2.0 - 95,
                    getHeight() / 2.0 - 105
            );

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
            g.fillPolygon(
                    new int[]{77, 100, 124},
                    new int[]{88, 65, 88},
                    3
            );

            g.setColor(INK);
            g.fillRoundRect(96, 103, 10, 20, 3, 3);

            g.dispose();
        }
    }
}
