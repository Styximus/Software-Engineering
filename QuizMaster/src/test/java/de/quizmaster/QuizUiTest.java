package de.quizmaster;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Dependency-free behavioral checks; run with run.ps1 -Test [-Screenshots]. */
public final class QuizUiTest {
    private static int assertions;
    private static Path screenshots;
    public static void main(String[] args) throws Exception {
        if (args.length > 0) { screenshots = Path.of(args[0]); Files.createDirectories(screenshots); }
        modelChecks();
        SwingUtilities.invokeAndWait(QuizUiTest::uiChecks);
        System.out.println("PASS: " + assertions + " assertions (models, session, Swing flow)." );
    }
    private static void check(boolean condition, String message) {
        assertions++; if (!condition) throw new AssertionError(message);
    }
    private static void rejects(Runnable action, String message) {
        try { action.run(); } catch (IllegalStateException | IllegalArgumentException expected) { check(true, message); return; }
        check(false, message);
    }
    private static void modelChecks() {
        MultipleChoiceQuestion mc = new MultipleChoiceQuestion(42, "Pick", new String[]{"A", "B", "C"}, new int[]{0, 2}, 3);
        check(mc.getId() == 42, "Question id");
        check(mc.checkAnswer("2, 0"), "Unordered multiple choice");
        check(!mc.checkAnswer("0,0,2") && !mc.checkAnswer("text") && !mc.checkAnswer(null) && !mc.checkAnswer("0,2,"), "Invalid multiple choice");
        mc.getAnswers()[0] = 1; mc.getOptions()[0] = "Changed";
        check(mc.checkAnswer("0,2") && mc.getOptions()[0].equals("A"), "Defensive answer copies");
        TrueFalseQuestion tf = new TrueFalseQuestion(2, "False?", false, 2);
        check(!tf.checkAnswer("garbage") && !tf.checkAnswer(null) && tf.checkAnswer(" FALSE "), "Boolean validation");
        TextQuestion text = new TextQuestion(3, "Text?", "Answer", 5);
        check(text.checkAnswer(" ANSWER ") && !text.checkAnswer(null), "Text normalization");
        ClozeTestQuestion gaps = new ClozeTestQuestion(4, "{1} and {2}", new String[]{"A", "B"}, 2);
        check(gaps.checkAnswer(" a ; B ") && !gaps.checkAnswer("A;") && !gaps.checkAnswer(null), "Cloze evaluation");
        Question[] source = {mc, tf, text}; Quiz quiz = new Quiz(1, source); source[0] = text;
        check(quiz.getQuestions()[0] == mc && quiz.getMaxPoints() == 10, "Quiz owns question array");
        QuizSession session = new QuizSession(quiz);
        rejects(() -> session.submit("0,2"), "Cannot answer before start"); session.start();
        rejects(session::next, "Cannot skip evaluation"); rejects(() -> session.submit(" "), "Cannot submit blank");
        session.submit("0,2"); check(session.points() == 3, "Weighted points");
        rejects(() -> session.submit("0,2"), "Cannot score twice"); session.next();
        session.submit("true"); check(session.points() == 3, "Wrong answer gives no points"); session.next();
        session.submit("answer"); session.next();
        check(session.state() == QuizSession.State.FINISHED && session.points() == 8 && session.stars() == 2, "Final score and stars");
        session.start(); check(session.points() == 0 && session.answered() == 0 && session.stars() == 0, "Restart resets session");
        for (int score : new int[]{0, 4, 5, 6, 7, 8, 9, 10}) {
            quiz.calculateStars(score); check(quiz.getStarsAchieved() == (score >= 9 ? 3 : score >= 7 ? 2 : score >= 5 ? 1 : 0), "Star threshold " + score);
        }
        QuizSession empty = new QuizSession(new Quiz(2, new Question[]{})); empty.start();
        check(empty.state() == QuizSession.State.FINISHED && empty.stars() == 0, "Empty quiz");
    }
    private static void uiChecks() {
        boolean[] closed = {false}; Npc npc = DemoQuizzes.biology();
        QuestionUserInterface ui = new QuestionUserInterface(1, npc, npc.getQuiz(), "Biologie entdecken", "Der Waldgarten", () -> closed[0] = true);
        JRootPane root = new JRootPane(); root.setContentPane(ui);
        capture(root, "01-dialog", 1440, 960); capture(root, "01-dialog-small", 1100, 800);
        button(ui, "primary").doClick(0);
        check(!button(ui, "primary").isEnabled(), "Blank choice disables submit");
        capture(root, "02-multiple-choice", 1440, 960); capture(root, "02-multiple-choice-small", 1100, 800);
        for (int i = 0; i < 3; i++) button(ui, "answer-" + i).doClick(0);
        check(button(ui, "primary").isEnabled(), "Multiple selections enable submit"); button(ui, "primary").doClick(0);
        check(ui.getReachedPoints() == 3 && !button(ui, "answer-0").isEnabled(), "Correct feedback locks inputs");
        ui.setNpc(npc);
        check(button(ui, "answer-0").isSelected() && !button(ui, "answer-0").isEnabled()
            && button(ui, "primary").getText().equals("Nächste Frage"), "Re-render preserves evaluated answer");
        capture(root, "03-correct", 1440, 960); button(ui, "primary").doClick(0);
        button(ui, "answer-0").doClick(0); button(ui, "answer-1").doClick(0);
        check(!button(ui, "answer-0").isSelected(), "True/false is exclusive");
        capture(root, "04-true-false", 1440, 960); button(ui, "primary").doClick(0);
        check(ui.getReachedPoints() == 3 && ((JTextArea)find(ui, "feedback")).getText().contains("Wahr"), "Wrong feedback shows solution");
        capture(root, "05-incorrect", 1440, 960); button(ui, "primary").doClick(0);
        JTextField text = (JTextField)find(ui, "text-answer"); text.setText("   ");
        check(!button(ui, "primary").isEnabled(), "Whitespace answer disabled"); text.setText(" chlorophyll ");
        capture(root, "06-text", 1440, 960); button(ui, "primary").doClick(0);
        check(ui.getReachedPoints() == 5 && !text.isEditable(), "Text uses model evaluator"); button(ui, "primary").doClick(0);
        JTextField gap0 = (JTextField)find(ui, "gap-0"), gap1 = (JTextField)find(ui, "gap-1");
        gap0.setText("Kohlenstoffdioxid"); check(!button(ui, "primary").isEnabled(), "All gaps required");
        gap1.setText("Sauer;stoff"); check(!button(ui, "primary").isEnabled(), "Delimiter cannot create extra answers");
        gap1.setText("Sauerstoff"); capture(root, "07-cloze", 1440, 960); capture(root, "07-cloze-small", 1100, 800);
        button(ui, "primary").doClick(0); button(ui, "primary").doClick(0);
        button(ui, "answer-1").doClick(0); button(ui, "primary").doClick(0);
        check(button(ui, "primary").getText().equals("Ergebnis ansehen"), "Last question action"); button(ui, "primary").doClick(0);
        check(ui.getReachedPoints() == 9 && ui.getSession().stars() == 3, "Full Swing flow score");
        capture(root, "08-result", 1440, 960); capture(root, "08-result-small", 1100, 800);
        button(ui, "back").doClick(0); check(closed[0], "Return callback");
        button(ui, "primary").doClick(0); check(ui.getReachedPoints() == 0 && !button(ui, "primary").isEnabled(), "UI restart");
        AnswerForm invalid = new AnswerForm(new ClozeTestQuestion(9, "No gaps", new String[]{"x"}, 1), () -> {});
        check(!invalid.ready(), "Malformed cloze safely blocked");
        AnswerForm zero = new AnswerForm(new ClozeTestQuestion(9, "{0} {1}", new String[]{"x", "y"}, 1), () -> {});
        ((JTextField)find(zero, "gap-0")).setText("x"); ((JTextField)find(zero, "gap-1")).setText("y");
        check(zero.ready() && zero.answer().equals("x;y"), "Zero based placeholders");
        Quiz longQuiz = new Quiz(9, new Question[]{new MultipleChoiceQuestion(9, "Eine längere Frage. ".repeat(30),
            new String[]{"Eine sehr ausführliche Antwort mit wichtigen Details. ".repeat(12), "<html>Dieser Text ist eine Antwort, kein Markup.</html>"}, new int[]{0}, 1)});
        QuestionUserInterface longUi = new QuestionUserInterface(9, npc, longQuiz, "Lange Inhalte", "Lesbarkeit", () -> {});
        root.setContentPane(longUi); longUi.startGame(); capture(root, "09-long-content", 1084, 761);
        check(button(longUi, "answer-0").getPreferredSize().height > 58, "Long answer wraps");
        button(longUi, "answer-1").doClick(0); button(longUi, "primary").doClick(0);
        capture(root, "10-long-feedback", 1084, 761);
    }
    private static AbstractButton button(Container parent, String name) { return (AbstractButton)find(parent, name); }
    private static Component find(Container parent, String name) {
        Component found = search(parent, name); if (found == null) throw new AssertionError("Missing component: " + name); return found;
    }
    private static Component search(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container container) { Component found = search(container, name); if (found != null) return found; }
        } return null;
    }
    private static void layout(Container parent) { parent.doLayout(); for (Component child : parent.getComponents()) if (child instanceof Container c) layout(c); }
    private static void capture(JRootPane root, String name, int width, int height) {
        root.setSize(width, height); for (int i = 0; i < 3; i++) layout(root);
        Component primary = find(root, "primary");
        Rectangle action = SwingUtilities.convertRectangle(primary.getParent(), primary.getBounds(), root);
        check(new Rectangle(0, 0, width, height).contains(action), "Primary action stays on screen: " + name);
        for (Container parent = primary.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof JViewport viewport) {
                Rectangle visible = SwingUtilities.convertRectangle(viewport, new Rectangle(0, 0, viewport.getWidth(), viewport.getHeight()), root);
                check(visible.contains(action), "Primary action stays in viewport: " + name);
            }
        }
        if (screenshots == null) return;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics(); root.printAll(graphics); graphics.dispose();
        try { ImageIO.write(image, "png", screenshots.resolve(name + ".png").toFile()); }
        catch (Exception error) { throw new AssertionError(error); }
    }
}
