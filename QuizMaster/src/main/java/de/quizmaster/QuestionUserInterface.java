package de.quizmaster;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** SCRUM-53: reusable Swing view, backed by the team's four Question subclasses. */
public class QuestionUserInterface extends JPanel {
    private final int id;
    private Npc npc;
    private Quiz quiz;
    private QuizSession session;
    private final int initialPoints;
    private String quizTitle = "Dein Quiz", topic = "Gemeinsam lernen";
    private Runnable onClose;
    private final JPanel stage = new ViewportStage();
    private final JLabel points = QuizTheme.label("", QuizTheme.bold(14), QuizTheme.INK);
    private final JLabel stars = QuizTheme.label("", QuizTheme.bold(14), QuizTheme.INK);
    private JButton primary;
    private AnswerForm form;
    private JTextArea feedback;
    private JProgressBar progress;
    private JLabel progressLabel;

    /** Fits the card to the viewport; only long answer lists scroll inside it. */
    private final class ViewportStage extends JPanel implements Scrollable {
        ViewportStage() { super(null); setOpaque(false); }
        @Override public void doLayout() {
            if (getComponentCount() == 0) return;
            Component card = getComponent(0);
            int width = Math.min(1002, getWidth());
            int height = Math.min(card.getPreferredSize().height, Math.max(0, getHeight() - 24));
            int y = session.state() == QuizSession.State.READY ? getHeight() - height - 12 : (getHeight() - height) / 2;
            card.setBounds((getWidth() - width) / 2, y, width, height);
        }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 20; }
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return r.height - 20; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return getParent() == null || getParent().getHeight() >= 480; }
    }

    public QuestionUserInterface(int id, Npc npc, Quiz quiz, int reachedPoints) {
        this.id = id; this.npc = npc; this.quiz = quiz; session = new QuizSession(quiz);
        if (reachedPoints < 0 || reachedPoints > quiz.getMaxPoints()) throw new IllegalArgumentException("Ungültiger Punktestand.");
        initialPoints = reachedPoints;
        setLayout(new BorderLayout(0, 22)); setBorder(new EmptyBorder(28, 32, 28, 32)); setOpaque(false);
        add(header(), BorderLayout.NORTH);
        JScrollPane viewport = new JScrollPane(stage); viewport.setBorder(null); viewport.setOpaque(false);
        viewport.getViewport().setOpaque(false); viewport.getVerticalScrollBar().setUnitIncrement(20);
        viewport.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(viewport, BorderLayout.CENTER); add(footer(), BorderLayout.SOUTH);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "close-quiz");
        getActionMap().put("close-quiz", new AbstractAction() { public void actionPerformed(ActionEvent e) { requestClose(); } });
        render();
    }
    public QuestionUserInterface(int id, Npc npc, Quiz quiz, String title, String topic, Runnable onClose) {
        this(id, npc, quiz, 0); quizTitle = title; this.topic = topic; this.onClose = onClose; render();
    }
    @Override public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g); g.dispose();
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        QuizTheme.paintCover(g, QuizTheme.image("island"), 0, 0, getWidth(), getHeight());
        g.setColor(new Color(37, 61, 52, 42)); g.fillRect(0, 0, getWidth(), getHeight()); g.dispose();
        super.paintComponent(graphics);
    }
    private JPanel header() {
        JPanel row = QuizTheme.clear(new BorderLayout());
        JPanel brand = new QuizTheme.Card(new FlowLayout(FlowLayout.LEFT, 12, 0), QuizTheme.CREAM, 18);
        brand.setBorder(new EmptyBorder(12, 6, 14, 12)); brand.add(new JLabel(QuizTheme.icon("compass", 30, 30)));
        JPanel name = QuizTheme.clear(new GridLayout(2, 1));
        name.add(QuizTheme.label("Wissensinsel", QuizTheme.font("Fraunces-Bold", 25), QuizTheme.INK));
        name.add(QuizTheme.label("DEIN QUIZ-ABENTEUER", QuizTheme.bold(10), QuizTheme.GREEN)); brand.add(name);
        row.add(brand, BorderLayout.WEST);
        JPanel status = new QuizTheme.Card(new FlowLayout(FlowLayout.CENTER, 12, 12), QuizTheme.CREAM, 18);
        status.add(new JLabel(QuizTheme.icon("star", 16, 16))); status.add(stars);
        status.add(QuizTheme.label("|", QuizTheme.body(20), QuizTheme.LINE)); status.add(points);
        JPanel right = QuizTheme.clear(new GridBagLayout()); right.add(status); row.add(right, BorderLayout.EAST); return row;
    }
    private JPanel footer() {
        JPanel row = new QuizTheme.Card(new BorderLayout(18, 0), QuizTheme.CREAM, 18);
        row.setBorder(new EmptyBorder(14, 18, 18, 18));
        row.add(QuizTheme.label("Tab  Auswählen     Leertaste  Markieren     Enter  Bestätigen     Esc  Zurück", QuizTheme.body(13), QuizTheme.INK), BorderLayout.WEST);
        row.add(QuizTheme.label("Dein Quiz · in deinem Tempo", QuizTheme.body(13), QuizTheme.MUTED), BorderLayout.EAST); return row;
    }
    private void render() {
        stage.removeAll(); primary = null; form = null;
        JPanel card = switch (session.state()) {
            case READY -> intro(); case QUESTION, FEEDBACK -> question(); case FINISHED -> result();
        };
        stage.add(card);
        stage.setPreferredSize(new Dimension(1002, card.getPreferredSize().height + 36));
        if (session.state() == QuizSession.State.FEEDBACK) {
            form.restore(session.submittedAnswer()); showFeedback();
        }
        updateStatus(); revalidate(); repaint();
        SwingUtilities.invokeLater(() -> {
            JRootPane root = SwingUtilities.getRootPane(this); if (root != null) root.setDefaultButton(primary);
            if (form != null && session.state() == QuizSession.State.QUESTION) form.focusFirst();
            else if (primary != null) primary.requestFocusInWindow();
        });
    }
    /** Original 1002 x 261 dialog geometry from Figma 3:2570, with runtime content. */
    private JPanel intro() {
        JPanel host = QuizTheme.clear(null); host.setPreferredSize(new Dimension(1002, 261));
        JPanel card = new QuizTheme.Card(null, QuizTheme.CREAM, 26); card.setBounds(0, 20, 1002, 241); host.add(card);
        JLabel speaker = QuizTheme.label(npcName(), QuizTheme.heading(28), QuizTheme.INK); speaker.setBounds(194, 23, 100, 38); card.add(speaker);
        JPanel badge = badge(npc == null || npc.getRole() == null ? "Deine Quizbegleitung" : npc.getRole()); badge.setBounds(304, 26, 225, 32); card.add(badge);
        JTextArea greeting = QuizTheme.text(npc == null || npc.getGreeting() == null ? "Schön, dass du da bist. Bereit für ein Quiz?" : npc.getGreeting(), QuizTheme.body(17), QuizTheme.INK);
        greeting.setBounds(194, 72, 450, 78); card.add(greeting);
        JTextArea invitation = QuizTheme.text("Lass uns gemeinsam Neues entdecken!", QuizTheme.bold(14), QuizTheme.GREEN);
        invitation.setBounds(194, 158, 450, 44); card.add(invitation);
        JPanel separator = new JPanel(); separator.setBackground(QuizTheme.LINE); separator.setBounds(669, 23, 1, 190); card.add(separator);
        JLabel title = QuizTheme.label(quizTitle, QuizTheme.bold(17), QuizTheme.INK); title.setBounds(695, 23, 278, 26); title.setToolTipText(quizTitle); card.add(title);
        JLabel meta = QuizTheme.label(session.total() + " Fragen · " + session.maxPoints() + " Punkte", QuizTheme.body(13), QuizTheme.MUTED); meta.setBounds(695, 55, 278, 22); card.add(meta);
        JLabel reward = QuizTheme.label("Bis zu 3 Sterne sammeln", QuizTheme.bold(12), QuizTheme.GREEN); reward.setIcon(QuizTheme.icon("star", 16, 16)); reward.setIconTextGap(6);
        reward.setBounds(695, 82, 278, 24); card.add(reward);
        primary = QuizTheme.button(session.total() == 0 ? "Keine Fragen vorhanden" : "Quiz starten", "primary", true);
        primary.setEnabled(session.total() > 0); primary.setHorizontalTextPosition(SwingConstants.LEFT);
        primary.setIcon(QuizTheme.icon("play", 20, 20)); primary.setIconTextGap(12);
        primary.setBounds(695, 117, 278, 52); primary.addActionListener(e -> startGame()); card.add(primary);
        JButton later = QuizTheme.button("Vielleicht später", "later", false); later.setFont(QuizTheme.body(13));
        later.setBounds(695, 178, 278, 36); later.addActionListener(e -> closeWindows()); card.add(later);
        JLabel portrait = portrait(); portrait.setBounds(12, 0, 165, 244); host.add(portrait); host.setComponentZOrder(portrait, 0);
        JButton close = closeButton(); close.setBounds(970, 5, 32, 32); host.add(close); host.setComponentZOrder(close, 0); return host;
    }
    private JPanel badge(String text) {
        JPanel badge = new QuizTheme.Card(new FlowLayout(FlowLayout.LEFT, 7, 7), QuizTheme.SOFT, 16);
        badge.add(new JLabel(QuizTheme.icon("leaf", 15, 15))); badge.add(QuizTheme.label(text, QuizTheme.bold(12), QuizTheme.GREEN)); return badge;
    }
    private JLabel portrait() {
        Image image = npc == null ? null : npc.getSkin();
        JLabel portrait = new JLabel() {
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (image != null) QuizTheme.paintCover((Graphics2D) graphics, image, 0, 0, getWidth(), getHeight());
            }
        };
        portrait.setName("npc-portrait"); portrait.setPreferredSize(new Dimension(165, 244));
        portrait.getAccessibleContext().setAccessibleName("Porträt von " + npcName()); return portrait;
    }
    private JButton closeButton() {
        JButton close = QuizTheme.button("", "close", false); close.setBorder(new EmptyBorder(7, 7, 7, 7));
        close.setPreferredSize(new Dimension(32, 32)); close.setIcon(QuizTheme.icon("close", 17, 17));
        close.getAccessibleContext().setAccessibleName("Zurück zum Modulmenü"); close.setToolTipText("Zurück (Esc)");
        close.addActionListener(e -> requestClose()); return close;
    }
    private JPanel contentCard() {
        JPanel card = new QuizTheme.Card(new BorderLayout(26, 20), QuizTheme.CREAM, 26);
        card.setBorder(new EmptyBorder(28, 30, 30, 30)); card.setPreferredSize(new Dimension(1002, 650)); return card;
    }
    private JPanel companion() {
        JPanel companion = QuizTheme.clear(new BorderLayout(0, 12)); companion.setPreferredSize(new Dimension(165, 0));
        JPanel top = QuizTheme.clear(new BorderLayout(0, 12)); top.add(portrait(), BorderLayout.NORTH);
        top.add(QuizTheme.label(npcName(), QuizTheme.heading(28), QuizTheme.INK), BorderLayout.CENTER);
        JTextArea tip = QuizTheme.text("Nimm dir Zeit.\nDu schaffst das!", QuizTheme.body(15), QuizTheme.MUTED); tip.setRows(2); top.add(tip, BorderLayout.SOUTH);
        companion.add(top, BorderLayout.NORTH);
        JTextArea context = QuizTheme.text(topic + "\n" + quizTitle, QuizTheme.bold(13), QuizTheme.GREEN); context.setRows(2);
        companion.add(context, BorderLayout.SOUTH); return companion;
    }
    private JPanel question() {
        JPanel card = contentCard(); card.add(companion(), BorderLayout.WEST);
        JPanel right = QuizTheme.clear(new BorderLayout(0, 18)); card.add(right, BorderLayout.CENTER); Question q = session.current();
        JPanel heading = QuizTheme.clear(new BorderLayout(0, 10)); JPanel meta = QuizTheme.clear(new BorderLayout());
        progressLabel = QuizTheme.label("", QuizTheme.bold(12), QuizTheme.GREEN); meta.add(progressLabel, BorderLayout.WEST); meta.add(closeButton(), BorderLayout.EAST);
        heading.add(meta, BorderLayout.NORTH);
        JTextArea text = QuizTheme.text(q.getQuestionText(), QuizTheme.heading(28), QuizTheme.INK);
        text.setRows(Math.max(2, Math.min(3, (q.getQuestionText().length() + 42) / 43)));
        text.setName("question-text"); heading.add(scrollText(text), BorderLayout.CENTER);
        progress = new JProgressBar(0, session.total()); progress.setBorderPainted(false); progress.setForeground(QuizTheme.GREEN); progress.setBackground(QuizTheme.SOFT);
        progress.setPreferredSize(new Dimension(100, 5)); heading.add(progress, BorderLayout.SOUTH); right.add(heading, BorderLayout.NORTH);
        form = new AnswerForm(q, () -> { if (primary != null) primary.setEnabled(form.ready()); });
        JPanel answerArea = QuizTheme.clear(new BorderLayout(0, 10));
        answerArea.add(QuizTheme.label(form.instructions(), QuizTheme.body(14), QuizTheme.MUTED), BorderLayout.NORTH);
        JScrollPane answers = new JScrollPane(form); answers.setBorder(null); answers.setOpaque(false); answers.getViewport().setOpaque(false);
        QuizTheme.styleScrollPane(answers);
        answers.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); answers.getVerticalScrollBar().setUnitIncrement(18);
        answerArea.add(answers, BorderLayout.CENTER); right.add(answerArea, BorderLayout.CENTER);
        JPanel actions = QuizTheme.clear(new BorderLayout(14, 12));
        feedback = QuizTheme.text("Wähle deine Antwort und klicke auf „Antwort prüfen“.", QuizTheme.body(14), QuizTheme.MUTED);
        feedback.setName("feedback"); feedback.setRows(2); actions.add(scrollText(feedback), BorderLayout.NORTH);
        primary = QuizTheme.button("Antwort prüfen", "primary", true); primary.setEnabled(false);
        primary.addActionListener(e -> {
            if (session.state() == QuizSession.State.QUESTION && form.ready()) evaluateAnswers(form.answer());
            else if (session.state() == QuizSession.State.FEEDBACK) { session.next(); render(); }
        });
        actions.add(QuizTheme.label(q.getPoints() + (q.getPoints() == 1 ? " Punkt möglich" : " Punkte möglich"), QuizTheme.bold(13), QuizTheme.GREEN), BorderLayout.WEST);
        actions.add(primary, BorderLayout.EAST); right.add(actions, BorderLayout.SOUTH); return card;
    }
    private JScrollPane scrollText(JTextArea text) {
        JScrollPane pane = new JScrollPane(text); pane.setBorder(null); pane.setOpaque(false); pane.getViewport().setOpaque(false);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); QuizTheme.styleScrollPane(pane);
        return pane;
    }
    private JPanel result() {
        JPanel card = contentCard(); card.add(companion(), BorderLayout.WEST);
        JPanel result = QuizTheme.clear(new BorderLayout(0, 24)); card.add(result, BorderLayout.CENTER);
        JPanel heading = QuizTheme.clear(new BorderLayout(0, 14));
        heading.add(QuizTheme.label("DEIN QUIZ IST ABGESCHLOSSEN", QuizTheme.bold(12), QuizTheme.GREEN), BorderLayout.NORTH);
        heading.add(QuizTheme.label(session.total() == 0 ? "Noch keine Fragen" : "Gut entdeckt!", QuizTheme.heading(38), QuizTheme.INK), BorderLayout.CENTER); result.add(heading, BorderLayout.NORTH);
        JPanel score = QuizTheme.clear(new GridLayout(4, 1, 0, 12));
        JLabel total = QuizTheme.label(session.points() + " / " + session.maxPoints(), QuizTheme.heading(64), QuizTheme.GREEN); total.setName("score"); score.add(total);
        score.add(QuizTheme.label("Punkte in „" + quizTitle + "“", QuizTheme.body(17), QuizTheme.MUTED));
        JLabel earned = QuizTheme.label(session.stars() + " / 3 Sterne erreicht", QuizTheme.bold(22), QuizTheme.GREEN); earned.setName("earned-stars");
        earned.setIcon(QuizTheme.icon("star", 16, 16)); earned.setIconTextGap(12); score.add(earned);
        score.add(QuizTheme.text("Mit jeder Frage lernst du dazu. Du kannst das Quiz noch einmal spielen oder zum Modulmenü zurückkehren.", QuizTheme.body(16), QuizTheme.MUTED)); result.add(score, BorderLayout.CENTER);
        JPanel actions = QuizTheme.clear(new GridLayout(1, 2, 12, 0));
        JButton back = QuizTheme.button("Zum Modulmenü", "back", false); back.addActionListener(e -> closeWindows()); actions.add(back);
        primary = QuizTheme.button("Noch einmal spielen", "primary", true); primary.setEnabled(session.total() > 0); primary.addActionListener(e -> startGame()); actions.add(primary);
        result.add(actions, BorderLayout.SOUTH); return card;
    }
    private String solution(Question q) {
        return switch (q.getType()) {
            case MULTIPLE_CHOICE -> { MultipleChoiceQuestion mc = (MultipleChoiceQuestion) q; String[] options = mc.getOptions();
                yield Arrays.stream(mc.getAnswers()).filter(i -> i >= 0 && i < options.length).mapToObj(i -> options[i]).collect(Collectors.joining(", ")); }
            case TRUE_FALSE -> ((TrueFalseQuestion) q).isAnswer() ? "Wahr" : "Falsch";
            case TEXT -> ((TextQuestion) q).getAnswer(); case CLOZE_TEST -> String.join("; ", ((ClozeTestQuestion) q).getAnswer());
        };
    }
    private void updateStatus() {
        points.setText(getReachedPoints() + " / " + session.maxPoints() + " Punkte");
        stars.setText(session.state() == QuizSession.State.FINISHED ? session.stars() + " / 3 Sterne" : "Bis zu 3 Sterne");
        if (session.state() == QuizSession.State.QUESTION || session.state() == QuizSession.State.FEEDBACK) {
            progressLabel.setText("FRAGE " + (session.index() + 1) + " / " + session.total() + "  ·  " + typeName(session.current().getType())); progress.setValue(session.answered());
        }
    }
    private String typeName(QuestionType type) {
        return switch (type) { case MULTIPLE_CHOICE -> "MEHRFACHAUSWAHL"; case TRUE_FALSE -> "WAHR ODER FALSCH"; case TEXT -> "FREITEXT"; case CLOZE_TEST -> "LÜCKENTEXT"; };
    }
    private String npcName() { return npc == null || npc.getName() == null ? "Quizbegleitung" : npc.getName(); }
    private void requestClose() {
        if ((session.state() == QuizSession.State.QUESTION || session.state() == QuizSession.State.FEEDBACK)
            && JOptionPane.showConfirmDialog(this, "Diesen Durchlauf verlassen? Die bisherigen Antworten werden nicht gespeichert.", "Quiz verlassen", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        closeWindows();
    }
    public void setNpc(Npc npc) { this.npc = npc; render(); }
    public int getId() { return id; }
    public Npc getNpc() { return npc; }
    public int getReachedPoints() { return session.state() == QuizSession.State.READY ? initialPoints : session.points(); }
    public QuizSession getSession() { return session; }
    public void getQuizFromNpc() {
        if (npc == null || npc.getQuiz() == null) throw new IllegalStateException("Dieser NPC hat kein Quiz.");
        quiz = npc.getQuiz(); session = new QuizSession(quiz); render();
    }
    public void randomizeQuestions() { session.setRandomized(true); }
    public void initializeUi() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::initializeUi); return; }
        JFrame frame = new JFrame("Wissensinsel · " + quizTitle); frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this); frame.setSize(1440, 960); frame.setMinimumSize(new Dimension(1100, 800)); frame.setLocationRelativeTo(null); frame.setVisible(true);
    }
    public void startGame() { session.start(); render(); }
    public void evaluateAnswers(String answer) {
        session.submit(answer); showFeedback(); updateStatus(); primary.requestFocusInWindow(); repaint();
    }
    private void showFeedback() {
        form.lock(); feedback.setForeground(session.correct() ? QuizTheme.GREEN : QuizTheme.ERROR);
        feedback.setText(session.correct() ? "Richtig! +" + session.current().getPoints() + " Punkte.\nGut gemacht, weiter zur nächsten Entdeckung."
            : "Diesmal leider nicht.\nRichtige Antwort: " + solution(session.current()));
        primary.setText(session.index() + 1 == session.total() ? "Ergebnis ansehen" : "Nächste Frage"); primary.setEnabled(true);
    }
    public void endGame() { session.finish(); render(); }
    public void evaluateStars() { quiz.calculateStars(session.points()); updateStatus(); }
    public void closeWindows() {
        if (onClose != null) onClose.run(); else { Window window = SwingUtilities.getWindowAncestor(this); if (window != null) window.dispose(); }
    }
}
